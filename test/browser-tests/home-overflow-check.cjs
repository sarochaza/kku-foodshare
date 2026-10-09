// Live regression: current homepage filters and guide fit mobile and desktop.
const {chromium}=require(process.env.PLAYWRIGHT_MODULE||'playwright');
const assert=require('node:assert/strict');
const fs=require('node:fs'),path=require('node:path');
const base=process.env.TEST_BASE_URL||'http://localhost:8081';
const out=path.resolve(process.env.BROWSER_SCREENSHOT_DIR||path.join(__dirname,'../reports/browser/screenshots/home-overflow-check'));
const reportOut=path.resolve(process.env.BROWSER_REPORT_DIR||path.join(__dirname,'../reports/browser/reports/home-overflow-check'));
fs.mkdirSync(out,{recursive:true});fs.mkdirSync(reportOut,{recursive:true});
const report={viewports:[],javascriptErrors:[]};let current;
async function checkWidth(page,width,stage) {
 await page.evaluate(async()=>{await document.fonts.ready;await new Promise(r=>requestAnimationFrame(()=>requestAnimationFrame(r)));});
 const metrics=await page.evaluate(()=>{
  const checkbox=document.querySelector('#home-available-now');
  if(!checkbox)throw Error('Current availability checkbox is missing');
  return {width:innerWidth,scroll:document.documentElement.scrollWidth,
   checkbox:checkbox.getBoundingClientRect().toJSON(),offsetParent:checkbox.offsetParent?.tagName||null};
 });
 assert.equal(metrics.scroll>metrics.width+1,false,width+'px '+stage+' overflow');
 assert.ok(metrics.checkbox.width>0 && metrics.checkbox.left>=-1 && metrics.checkbox.right<=metrics.width+1,'Availability checkbox must stay within the viewport');
 report.viewports.push({width,stage,...metrics,overflow:false});
}
(async()=>{
 const browser=await chromium.launch({headless:true,executablePath:process.env.BROWSER_EXECUTABLE||undefined,args:['--no-sandbox']});
 try {
  for(const width of [360,390,768,1366]) {
   const context=await browser.newContext({viewport:{width,height:900}});
   const page=await context.newPage();current=page;
   page.setDefaultTimeout(30000);page.setDefaultNavigationTimeout(30000);
   page.on('pageerror',e=>report.javascriptErrors.push(e.message));
   // Exercise map failure UI; external tile-provider availability is outside this check.
   await page.route('https://tile.openstreetmap.org/**',r=>r.abort());
   await page.goto(base+'/?guide');
   await page.locator('#home-guide[open]').waitFor();
   await checkWidth(page,width,'guide open');
   await page.locator('#guide-skip').click();
   await page.locator('#home-guide').waitFor({state:'hidden'});
   await page.locator('#home-category-filters').evaluate(e=>e.scrollLeft=e.scrollWidth);
   // Availability sits in its own label outside the category scroller.
   const checkbox=page.locator('#home-available-now');
   assert.equal(await checkbox.isChecked(),false);
   await page.locator('.home-now-compact').click();
   assert.equal(await checkbox.isChecked(),true);
   await page.waitForFunction(()=>document.querySelector('.home-now-compact')?.classList.contains('active'));
   await checkWidth(page,width,'filters scrolled and availability enabled');
   await checkbox.uncheck();
   assert.equal(await checkbox.isChecked(),false);
   await page.waitForFunction(()=>!document.querySelector('.home-now-compact')?.classList.contains('active'));
   await checkWidth(page,width,'availability disabled');
   await page.screenshot({path:path.join(out,'home-filters-'+width+'.png'),fullPage:true});
   console.log('PASS: homepage guide, scrolled categories and compact availability filter fit '+width+'px');
   await context.close();current=null;
  }
  assert.deepEqual(report.javascriptErrors,[]);report.result='PASS';
 }catch(error){
  report.result='FAIL';report.error=error.stack||error.message;process.exitCode=1;console.error(error);
  if(current)try{await current.screenshot({path:path.join(out,'failure.png'),fullPage:true});fs.writeFileSync(path.join(reportOut,'failure-body.txt'),await current.locator('body').innerText());}catch(captureError){report.captureError=captureError.message;}
 }finally{
  fs.writeFileSync(path.join(reportOut,'home-result.json'),JSON.stringify(report,null,2));await browser.close();
 }
})().catch(error=>{console.error(error);process.exitCode=1});
