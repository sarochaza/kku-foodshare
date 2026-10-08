// Real template/CSS regression checks; email delivery is verified by the Java journey.
const { chromium } = require(process.env.PLAYWRIGHT_MODULE || 'playwright');
const fs = require('node:fs');
const path = require('node:path');
const http = require('node:http');
const assert = require('node:assert/strict');
const root = path.resolve(__dirname, '../code/src/main/resources');
const fragments = fs.readFileSync(path.join(root, 'templates/fragments.html'), 'utf8');
const styles = [...fragments.matchAll(/<link rel="stylesheet" href="([^"]+)"/g)].map(m => `<link rel="stylesheet" href="${m[1]}">`).join('');
const symbols = fragments.match(/<svg\s+class="symbol-bank"[\s\S]*?<\/svg>/)[0];
const failures = [];
function check(name, action) {
  try { action(); } catch (error) { failures.push(name + ': ' + error.message); }
}
function rgb(color) { return color.match(/[\d.]+/g).slice(0, 3).map(Number); }
function luminance(color) { return rgb(color).map(v => v / 255).map(v => v <= .04045 ? v / 12.92 : ((v + .055) / 1.055) ** 2.4).reduce((sum, v, i) => sum + v * [.2126,.7152,.0722][i], 0); }
function contrast(fg, bg) { const a=luminance(fg), b=luminance(bg); return (Math.max(a,b)+.05)/(Math.min(a,b)+.05); }
function html(name) {
  if (process.env.RENDERED_TEMPLATES_DIR) {
    return fs.readFileSync(path.join(process.env.RENDERED_TEMPLATES_DIR,name+'.html'),'utf8')
      .replace(/<script\b[\s\S]*?<\/script>/g, '')
      .replace('<html', '<html data-theme="dark"');
  }
  const template = fs.readFileSync(path.join(root, 'templates', name + '.html'), 'utf8');
  const bodyAttrs = template.match(/<body([^>]*)>/)[1];
  const main = template.match(/<main[\s\S]*?<\/main>/)[0];
  return `<!doctype html><html lang="th" data-theme="dark"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">${styles}</head><body${bodyAttrs}>${symbols}${main}</body></html>`;
}
const server = http.createServer((req, res) => {
  const url = new URL(req.url, 'http://localhost');
  if (/^\/(home|login|register|forgot-password|reset-password)$/.test(url.pathname)) {
    res.setHeader('Content-Type', 'text/html; charset=utf-8'); return res.end(html(url.pathname.slice(1)));
  }
  const file = path.resolve(root, 'static', '.' + decodeURIComponent(url.pathname));
  if (!file.startsWith(path.join(root, 'static') + path.sep) || !fs.existsSync(file) || !fs.statSync(file).isFile()) { res.statusCode=404; return res.end(); }
  const ext=path.extname(file);
  res.setHeader('Content-Type', ({'.css':'text/css','.webp':'image/webp','.png':'image/png','.woff2':'font/woff2'})[ext] || 'application/octet-stream');
  fs.createReadStream(file).pipe(res);
});
(async () => {
  await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
  const base = 'http://127.0.0.1:' + server.address().port;
  const browser = await chromium.launch({headless:true, executablePath:process.env.CHROMIUM_PATH || undefined, args:['--no-sandbox','--disable-dev-shm-usage']});
  try {
    const page = await browser.newPage();
    await page.emulateMedia({reducedMotion:'reduce'});
    for (const width of [360,390,768,1440]) {
      await page.setViewportSize({width,height:900});
      await page.goto(base + '/home');
      await page.evaluate(() => document.fonts.ready);
      const controls = await page.evaluate(() => ['#home-sort','.home-now-compact','.home-sharing-map-icon'].map(s => {
        const e=document.querySelector(s), b=e.getBoundingClientRect(), c=getComputedStyle(e);
        return {top:b.top,bottom:b.bottom,height:b.height,display:c.display,direction:c.flexDirection,background:c.backgroundColor};
      }));
      check('Toolbar alignment at ' + width, () => {
        for (const control of controls.slice(1)) {
          assert.ok(Math.abs(control.top-controls[0].top) <= 1, JSON.stringify(controls));
          assert.ok(Math.abs(control.bottom-controls[0].bottom) <= 1, JSON.stringify(controls));
        }
        assert.equal(controls[1].direction, 'row', 'checkbox and text stay on one line');
      });
      for (const theme of ['dark','color','monochrome']) {
        await page.evaluate(t => document.documentElement.dataset.theme=t, theme);
        const faq = await page.locator('.welcome-faq details').first().evaluate(e => ({bg:getComputedStyle(e).backgroundColor,fg:getComputedStyle(e.querySelector('summary')).color}));
        check('FAQ readable ' + theme + ' ' + width, () => assert.ok(contrast(faq.fg,faq.bg) >= 4.5, JSON.stringify(faq)));
        await page.locator('.welcome-faq summary').first().click();
        assert.equal(await page.locator('.welcome-faq details').first().getAttribute('open'), '');
        await page.locator('.welcome-faq summary').first().click();
        assert.ok(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth + 1), 'no page overflow ' + width);
      }
      await page.locator('.home-now-compact').click();
      assert.equal(await page.locator('#home-available-now').isChecked(), true);
      const banner = await page.locator('.share-banner > img').evaluate(e => ({src:e.getAttribute('src'),loaded:e.complete&&e.naturalWidth>0,width:e.clientWidth,height:e.clientHeight,naturalWidth:e.naturalWidth,naturalHeight:e.naturalHeight,fit:getComputedStyle(e).objectFit}));
      check('Requested image at ' + width, () => {
        assert.equal(banner.src, '/images/foodshare-share-banner.png');
        assert.ok(banner.loaded, 'new image loads');
        assert.ok(Math.abs(banner.width / banner.height - banner.naturalWidth / banner.naturalHeight) < .03, 'full image without clipping faces');
      });
      for (const name of ['login','register','forgot-password','reset-password']) {
        await page.goto(base + '/' + name);
        const art=await page.locator('.auth-art').evaluate(e => ({bg:getComputedStyle(e).backgroundColor,image:getComputedStyle(e).backgroundImage,fg:getComputedStyle(e.querySelector('h2')).color,blend:getComputedStyle(e.querySelector('img')).mixBlendMode}));
        check('Dark auth art ' + name + ' ' + width, () => {
          assert.ok(Math.max(...rgb(art.bg)) < 100, JSON.stringify(art));
          for (const color of art.image.match(/rgba?\([^)]+\)/g) || []) assert.ok(Math.max(...rgb(color)) < 100, 'dark gradient: ' + art.image);
          assert.ok(contrast(art.fg,art.bg) >= 4.5, JSON.stringify(art));
          assert.equal(art.blend, 'normal', 'mascot stays visible');
        });
        assert.ok(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth + 1), name + ' fits ' + width);
      }
    }
    if (process.env.PREVIEW_DIR) {
      fs.mkdirSync(process.env.PREVIEW_DIR,{recursive:true});
      for(const [name,width] of [['login',1440],['forgot-password',390],['home',1440]]) {
        await page.setViewportSize({width,height:900}); await page.goto(base + '/' + name);
        const target=name==='home'?page.locator('#share'):page.locator('main');
        await target.screenshot({path:path.join(process.env.PREVIEW_DIR,name+'-'+width+'.png')});
      }
    }
    assert.equal(failures.length,0,failures.join('\n'));
    console.log('PASS: actual templates, dark/light/monochrome, aligned controls, image and auth layouts at 360/390/768/1440px');
  } finally { await browser.close(); await new Promise(resolve=>server.close(resolve)); }
})().catch(error=>{console.error(error);server.close();process.exitCode=1;});
