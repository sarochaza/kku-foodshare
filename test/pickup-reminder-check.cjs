const { chromium } = require('playwright');
const fs = require('node:fs'), path = require('node:path'), http = require('node:http');
const assert = require('node:assert/strict');

(async () => {
  const root = path.resolve('code/src/main/resources/static');
  const requests = []; let unread = 2;
  const post = {id: 11, title: 'ข้าวกล่อง <script>bad</script>', category: 'FOOD', quantity: 5,
    availableQuantity: 3, maxPerPerson: 3, unit: 'กล่อง', pickupLocationName: 'หอสมุด มข.',
    availableFrom: '2026-10-08T16:00:00', availableUntil: '2026-10-08T17:30:00',
    ownerName: 'ผู้แบ่งปัน', latitude: 16.474, longitude: 102.823, imageUrl: null};
  const pass = id => ({id, status: 'RESERVED', quantity: 2, pickupCode: '123456', post});
  function template(pageName, signedIn = true) {
    return fs.readFileSync(`code/src/main/resources/templates/${pageName}.html`, 'utf8')
      .replace('</head>', `<meta name="signed-in" content="${signedIn}"><meta name="_csrf" content="test-token"><meta name="_csrf_header" content="X-CSRF-TOKEN"><link rel="stylesheet" href="/css/app.css"><link rel="stylesheet" href="/css/theme.css"></head>`)
      .replace(/<header[^>]*><\/header>/, signedIn ? '<header><a class="notification-bell" href="/notifications" aria-label="การแจ้งเตือน">กระดิ่ง</a></header>' : '<header></header>')
      .replace('</body>', '<div id="toast" hidden></div><script type="module" src="/js/app.js"></script></body>');
  }
  const server = http.createServer((req, res) => {
    const url = new URL(req.url, 'http://localhost');
    if (url.pathname === '/reservations' || url.pathname === '/notifications') {
      res.setHeader('Content-Type', 'text/html; charset=utf-8'); res.end(template(url.pathname.slice(1), url.searchParams.get('guest') !== 'true')); return;
    }
    if (url.pathname.startsWith('/api/')) {
      requests.push({path: url.pathname, method: req.method}); res.setHeader('Content-Type', 'application/json');
      if (url.pathname.endsWith('/unread')) return res.end(JSON.stringify({count: unread}));
      if (url.pathname.endsWith('/notification-preferences')) return res.end('{"categories":[],"keywords":""}');
      if (url.pathname.endsWith('/notifications/9/read')) {res.statusCode = 204; return res.end();}
      if (url.pathname.endsWith('/me/notifications')) return res.end(JSON.stringify({items: [{id: 9, title: 'ใกล้หมดเวลารับอาหารที่จอง',
        message: 'รับภายใน 17:30 น. · ข้าวกล่อง <script>bad</script> · 2 กล่อง', type: 'RESERVATION',
        actorName: 'ผู้แบ่งปัน', actorId: 13, read: false, createdAt: '2026-10-08T17:00:00', href: '/reservations#reservation-7'}], page: 0, totalPages: 1, totalElements: 1}));
      if (url.pathname.endsWith('/me/reservations')) return res.end(JSON.stringify({items: [pass(41), pass(42)], page: 0, totalPages: 2, totalElements: 21}));
      if (url.pathname === '/api/v1/reservations/7') return res.end(JSON.stringify(pass(7)));
      res.statusCode = 404; return res.end('{"message":"ไม่พบรายการ"}');
    }
    const target = path.resolve(root, '.' + url.pathname);
    if (!target.startsWith(root + path.sep) || !fs.existsSync(target)) {res.statusCode = 404; return res.end();}
    res.setHeader('Content-Type', target.endsWith('.css') ? 'text/css' : target.endsWith('.js') || target.endsWith('.mjs') ? 'text/javascript' : 'application/octet-stream');
    res.end(fs.readFileSync(target));
  });
  await new Promise(r => server.listen(0, '127.0.0.1', r));
  const browser = await chromium.launch({headless: true, executablePath: process.env.CHROMIUM_PATH || undefined, args: ['--no-sandbox']});
  try {
    const base = 'http://127.0.0.1:' + server.address().port;
    for (const width of [360, 390, 768, 1440]) {
      const page = await browser.newPage({viewport: {width, height: 900}}); const errors = [];
      page.on('pageerror', e => errors.push(e.message));
      await page.goto(base + '/notifications');
      await page.locator('.notification-card').waitFor();
      const link = page.locator('.notification-card a');
      assert.equal(await link.innerText(), 'ดูการจอง');
      assert.equal(await page.locator('.notification-card script').count(), 0);
      await link.click();
      await page.locator('#reservation-7').waitFor();
      assert.ok(requests.some(r => r.path === '/api/v1/reservations/7'), 'A target not on page one must still open');
      assert.equal(await page.locator('#reservation-7 .pickup-code strong').innerText(), '123456');
      assert.equal(await page.locator('#reservation-7 .pass-qr').count(), 1);
      assert.equal(await page.locator('#reservation-7 script').count(), 0);
      assert.ok(await page.locator('#reservation-7').evaluate(e => document.activeElement === e));
      assert.equal(await page.locator('#reservation-7 a').filter({hasText: 'เส้นทาง / เวลาเดินทาง'}).getAttribute('href'), '/posts/11');
      assert.ok(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth),
        JSON.stringify(await page.evaluate(() => ({width: innerWidth, scroll: document.documentElement.scrollWidth,
          overflow: [...document.querySelectorAll('body *')].filter(e => e.getBoundingClientRect().right > innerWidth + 1)
            .map(e => ({tag: e.tagName, class: e.className?.baseVal ?? e.className, right: e.getBoundingClientRect().right, text: e.textContent.slice(0, 100)}))}))));
      assert.deepEqual(errors, []); await page.close();
    }
    const page = await browser.newPage(); await page.clock.install();
    await page.goto(base + '/reservations');
    await page.locator('.notification-badge').waitFor();
    assert.equal(await page.locator('.notification-badge').innerText(), '2');
    unread = 3; await page.clock.fastForward(60000);
    await page.waitForFunction(() => document.querySelector('.notification-badge')?.textContent === '3');
    unread = 0; await page.clock.fastForward(60000);
    await page.waitForFunction(() => !document.querySelector('.notification-badge'));
    assert.ok(requests.some(r => r.path === '/api/v1/me/notifications/9/read' && r.method === 'POST'));
    const before = requests.filter(r => r.path.endsWith('/unread')).length;
    await page.goto(base + '/reservations?guest=true'); await page.clock.fastForward(60000);
    assert.equal(requests.filter(r => r.path.endsWith('/unread')).length, before, 'Guests must not poll private notifications');
    await page.close();
    console.log('Pickup reminder deep link, target QR, badge updates, escaping and mobile/desktop layout passed');
  } finally {await browser.close(); await new Promise(r => server.close(r));}
})().catch(e => {console.error(e); process.exitCode = 1;});
