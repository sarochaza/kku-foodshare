// Regression: the offscreen filter checkbox must stay inside its horizontal scroller.
const {chromium}=require(process.env.PLAYWRIGHT_MODULE||'playwright');
const assert=require('node:assert/strict');
(async()=>{
 const b=await chromium.launch({executablePath:process.env.BROWSER_EXECUTABLE||undefined,args:['--no-sandbox']});
 try {const p=await b.newPage({viewport:{width:360,height:900}});
 await p.route('https://tile.openstreetmap.org/**',r=>r.abort());
 await p.goto((process.env.TEST_BASE_URL||'http://localhost:8081')+'/?guide');
 console.log(await p.evaluate(()=>({width:innerWidth,scroll:document.documentElement.scrollWidth,checkbox:document.querySelector('.home-now input').getBoundingClientRect().toJSON(),offsetParent:document.querySelector('.home-now input').offsetParent.tagName})));
 assert.equal(await p.evaluate(()=>document.documentElement.scrollWidth>innerWidth+1),false);
 await p.locator('#guide-skip').click();assert.equal(await p.locator('#home-guide').isVisible(),false);
 await p.locator('#home-category-filters').evaluate(e=>e.scrollLeft=e.scrollWidth);
 await p.locator('.home-now').click();assert.equal(await p.locator('#home-available-now').isChecked(),true);
 assert.equal(await p.evaluate(()=>document.documentElement.scrollWidth>innerWidth+1),false);
 console.log('PASS: homepage scrolled filter and onboarding fit 360px');
 }finally{await b.close();}
})().catch(e=>{console.error(e);process.exitCode=1});
