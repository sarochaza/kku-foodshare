const { chromium } = require('playwright');
const fs = require('node:fs'), path = require('node:path'), http = require('node:http');
const assert = require('node:assert/strict');

(async () => {
  const resources = path.resolve('code/src/main/resources');
  const done = new Set(); const writes = []; let failNext = false;
  function guide() {
    const fragments = fs.readFileSync(path.join(resources, 'templates/fragments.html'), 'utf8');
    const shared = fragments.match(/<dialog\b[^>]*th:fragment="onboardingGuide"[\s\S]*?<\/dialog>/);
    if (shared) return shared[0];
    const home = fs.readFileSync(path.join(resources, 'templates/home.html'), 'utf8');
    return '<div id="home-guide"' + home.split('<div id="home-guide"')[1].split('</body>')[0];
  }
  function html(name, account) {
    let source = fs.readFileSync(path.join(resources, 'templates', name + '.html'), 'utf8');
    // Keep the real onboarding markup/CSS/app entry point; suppress unrelated page loaders.
    source = source.replace(/data-page="[^"]+"/, 'data-page="fixture"')
      .replace(/<head\b[\s\S]*?<\/head>/, `<head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><meta name="signed-in" content="${account !== 'guest'}"><meta name="onboarding-needed" content="${account !== 'guest' && !done.has(account)}"><meta name="_csrf" content="guide-token"><meta name="_csrf_header" content="X-CSRF-TOKEN"><link rel="stylesheet" href="/css/app.css"><link rel="stylesheet" href="/css/theme.css"><script type="module" src="/js/app.js"></script></head>`)
      .replace(/<dialog[^>]*th:replace="~\{fragments :: onboardingGuide\}"[^>]*><\/dialog>/, guide());
    if (!source.includes('id="home-guide"')) source = source.replace('</body>', guide() + '</body>');
    return source.replace('</body>', '<div id="toast" hidden></div><div id="layer-conflict" style="position:fixed;inset:0;z-index:2147483647;background:#8299b933"></div><a class="header-post" href="/posts/new">แบ่งปันอาหาร</a></body>');
  }
  const server = http.createServer((req, res) => {
    const url = new URL(req.url, 'http://localhost');
    if (url.pathname === '/api/v1/me/onboarding') {
      const account = req.headers.cookie?.match(/guide-account=([^;]+)/)?.[1];
      if (req.method !== 'POST' || !account || account === 'guest' || req.headers['x-csrf-token'] !== 'guide-token') {
        res.statusCode = 403; return res.end('{}');
      }
      writes.push(account);
      if (failNext) {failNext = false; res.statusCode = 503; return res.end('{"message":"Unavailable"}');}
      done.add(account); res.statusCode = 204; return res.end();
    }
    if (/^\/(home|dashboard|explore)$/.test(url.pathname)) {
      const account = url.searchParams.get('account') || 'guest';
      res.setHeader('Content-Type', 'text/html; charset=utf-8');
      res.setHeader('Set-Cookie', 'guide-account=' + account + '; Path=/; SameSite=Lax');
      return res.end(html(url.pathname.slice(1), account));
    }
    const file = path.resolve(resources, 'static', '.' + url.pathname);
    if (!file.startsWith(path.join(resources, 'static') + path.sep) || !fs.existsSync(file)) {res.statusCode = 404; return res.end();}
    res.setHeader('Content-Type', file.endsWith('.css') ? 'text/css' : /\.(mjs|js)$/.test(file) ? 'text/javascript' : 'application/octet-stream');
    res.end(fs.readFileSync(file));
  });
  await new Promise(r => server.listen(0, '127.0.0.1', r));
  let browser;
  try {
    browser = await chromium.launch({headless: true, executablePath: process.env.CHROMIUM_PATH || undefined, args: ['--no-sandbox']});
    const base = 'http://127.0.0.1:' + server.address().port;
    for (const width of [360, 390, 768, 1440]) {
      const context = await browser.newContext({viewport: {width, height: 900}});
      await context.addInitScript(() => localStorage.setItem('foodshare-guide-v2', 'done'));
      const page = await context.newPage(); const errors = [];
      page.on('pageerror', e => errors.push(e.message));
      const account = 'first-' + width;
      await page.goto(base + '/dashboard?account=' + account);
      await page.locator('#home-guide:modal').waitFor({timeout: 3000});
      const backdrop = await page.locator('#home-guide').evaluate(e => {
        const style = getComputedStyle(e, '::backdrop');
        return [style.backdropFilter, style.webkitBackdropFilter];
      });
      assert.ok(backdrop.every(value => !value || value === 'none'), 'The guide must leave the page and highlighted controls sharp');
      const shadeAlpha = await page.locator('#home-guide .guide-shade').evaluate(e => {
        const color = getComputedStyle(e).backgroundColor;
        return color.startsWith('rgba(') ? Number(color.match(/,\s*([\d.]+)\s*\)$/)[1]) : 1;
      });
      assert.ok(shadeAlpha <= 0.25, 'The guide shade must keep the controls visible');
      assert.equal(await page.locator('#guide-progress').innerText(), '1 / 6');
      assert.ok(await page.locator('#guide-next').evaluate(e => document.activeElement === e));
      assert.ok(await page.locator('#guide-next').evaluate(e => {
        const r = e.getBoundingClientRect();
        return document.elementFromPoint(r.left + r.width / 2, r.top + r.height / 2)?.closest('#home-guide') === e.closest('#home-guide');
      }), 'The card must receive taps above even the highest map/header layer');
      assert.ok(await page.locator('.guide-card').evaluate(e => {
        const r = e.getBoundingClientRect(); return r.left >= 0 && r.right <= innerWidth + 1 && r.top >= 0 && r.bottom <= innerHeight;
      }));
      await page.locator('#guide-next').click(); await page.locator('#guide-back').click();
      assert.equal(await page.locator('#guide-progress').innerText(), '1 / 6');
      assert.ok(!done.has(account), 'Opening, next and back do not complete the guide');
      if (process.env.PREVIEW_DIR && [390, 1440].includes(width)) {
        fs.mkdirSync(process.env.PREVIEW_DIR, {recursive: true});
        await page.screenshot({path: path.join(process.env.PREVIEW_DIR, 'guide-' + width + '.png')});
      }
      const saved = page.waitForResponse(r => r.url().endsWith('/api/v1/me/onboarding'));
      for (let step = 0; step < 6; step++) await page.locator('#guide-next').click();
      assert.equal((await saved).status(), 204);
      assert.ok(done.has(account)); assert.equal(writes.filter(id => id === account).length, 1);
      assert.equal(await page.locator('#home-guide:modal').count(), 0);
      await page.reload(); assert.equal(await page.locator('#home-guide:modal').count(), 0);
      await page.goto(base + '/explore?account=second-' + width);
      await page.locator('#home-guide:modal').waitFor();
      const skipped = page.waitForResponse(r => r.url().endsWith('/api/v1/me/onboarding'));
      await page.locator('#guide-skip').click(); assert.equal((await skipped).status(), 204);
      assert.ok(done.has('second-' + width));
      await page.goto(base + '/home?account=' + account + '&guide=1');
      await page.locator('#home-guide:modal').waitFor();
      const escaped = page.waitForResponse(r => r.url().endsWith('/api/v1/me/onboarding'));
      await page.keyboard.press('Escape'); await escaped;
      assert.equal(await page.locator('#home-guide:modal').count(), 0);
      assert.deepEqual(errors, []);
      await context.close();
      const otherBrowser = await browser.newContext(); const other = await otherBrowser.newPage();
      await other.goto(base + '/dashboard?account=' + account);
      assert.equal(await other.locator('#home-guide:modal').count(), 0, 'Completed account stays completed in another browser');
      await otherBrowser.close();
    }
    const page = await browser.newPage();
    await page.goto(base + '/explore?account=guest');
    assert.equal(await page.locator('#home-guide:modal').count(), 0);
    const count = writes.length;
    await page.goto(base + '/home?account=guest&guide=1');
    await page.locator('#home-guide:modal').waitFor(); await page.locator('#guide-skip').click();
    assert.equal(writes.length, count, 'Manual guest replay cannot write account state');
    failNext = true;
    await page.goto(base + '/explore?account=offline'); await page.locator('#home-guide:modal').waitFor();
    await page.locator('#guide-skip').click();
    await page.locator('#toast:not([hidden])').waitFor();
    assert.equal(await page.locator('#home-guide:modal').count(), 0);
    assert.ok(!done.has('offline'));
    await page.reload(); await page.locator('#home-guide:modal').waitFor();
    await page.close();
    console.log('PASS: top-layer guide, unchanged six steps, per-account completion/replay, offline fallback and 360/390/768/1440px');
  } finally {try {if (browser) await browser.close();} finally {await new Promise(r => server.close(r));}}
})().catch(e => {console.error(e); process.exitCode = 1;});
