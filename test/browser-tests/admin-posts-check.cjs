const {chromium}=require('playwright');
const fs=require('node:fs'),path=require('node:path'),http=require('node:http');
const assert=require('node:assert/strict');
(async()=>{
  const requests=[];
  const root=path.resolve('code/src/main/resources/static');
  const template=fs.readFileSync('code/src/main/resources/templates/admin.html','utf8')
    .replace('</head>','<link rel="stylesheet" href="/css/app.css"><link rel="stylesheet" href="/css/theme.css"></head>')
    .replace('</body>','<div id="toast" hidden></div><script type="module" src="/js/app.js"></script></body>');
  const server=http.createServer((req,res)=>{
    const url=new URL(req.url,'http://localhost');
    if(url.pathname==='/') {res.setHeader('Content-Type','text/html');res.end(template);return;}
    if(url.pathname.startsWith('/api/')) {
      requests.push(url);res.setHeader('Content-Type','application/json');
      if(url.pathname.endsWith('pending-reports')) return res.end('{"count":2}');
      if(url.pathname.endsWith('/posts')) return res.end(JSON.stringify({items:[{id:7,title:'อาหารทดสอบ <script>bad</script>',owner:'ผู้แบ่งปัน',status:'EXPIRED',availableQuantity:5,reservedQuantity:2,unit:'กล่อง',pickupLocationName:'หอสมุด',availableUntil:'2026-10-08T12:00:00'}],totalElements:1,totalPages:1,page:0}));
      return res.end('{"items":[],"totalElements":0,"totalPages":0,"page":0}');
    }
    const target=path.resolve(root,'.'+url.pathname);
    if(!target.startsWith(root+path.sep)||!fs.existsSync(target)){res.statusCode=404;res.end();return;}
    res.setHeader('Content-Type',target.endsWith('.css')?'text/css':'text/javascript');res.end(fs.readFileSync(target));
  });
  await new Promise(r=>server.listen(0,'127.0.0.1',r));
  const browser=await chromium.launch({headless:true,executablePath:process.env.CHROMIUM_PATH||undefined,args:['--no-sandbox']});
  try {
    const page=await browser.newPage(); const errors=[];page.on('pageerror',e=>errors.push(e.message));
    await page.goto('http://127.0.0.1:'+server.address().port);
    await page.locator('#admin-content article').waitFor();
    assert.ok((await page.locator('#admin-content').innerText()).includes('คงเหลือ 5 กล่อง'));
    assert.equal(await page.locator('#admin-content script').count(),0);
    assert.equal(await page.locator('#admin-content a').getAttribute('href'),'/posts/7');
    assert.equal(await page.locator('#admin-pending-count').innerText(),'2');
    await page.locator('#admin-post-query').fill('หอสมุด');
    await page.locator('#admin-post-status').selectOption('EXPIRED');
    await page.waitForTimeout(100);
    assert.ok(requests.some(u=>u.searchParams.get('q')==='หอสมุด'&&u.searchParams.get('status')==='EXPIRED'));
    await page.locator('[data-admin-tab="reports"]').click();
    await page.locator('#admin-content').getByText('ไม่มีรายงานที่ต้องจัดการ').waitFor();
    assert.equal(await page.locator('#admin-post-filters').isVisible(),false);
    assert.deepEqual(errors,[]);
    for(const width of [390,1440]) {
      await page.setViewportSize({width,height:900});await page.locator('[data-admin-tab="posts"]').click(); await page.waitForTimeout(300);
      assert.ok(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth), JSON.stringify(await page.evaluate(()=>[document.documentElement.scrollWidth,innerWidth,...[...document.querySelectorAll("body *")].filter(e=>e.scrollWidth>e.clientWidth+2).map(e=>({tag:e.tagName,cls:e.className,right:e.getBoundingClientRect().right,scroll:e.scrollWidth,client:e.clientWidth}))])));
    }
    console.log('Admin listing, filters, escaping, report tab and responsive layout passed');
  } finally {await browser.close();await new Promise(r=>server.close(r));}
})().catch(e=>{console.error(e);process.exitCode=1;});
