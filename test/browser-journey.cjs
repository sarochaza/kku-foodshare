// Run only against an isolated test/staging database. Creates clearly labelled test records.
const { chromium } = require(process.env.PLAYWRIGHT_MODULE || 'playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const base = process.env.TEST_BASE_URL || 'http://127.0.0.1:8080';
const out = path.resolve(__dirname, '../img');fs.mkdirSync(out,{recursive:true});
const suffix=Date.now().toString(36),password='Foodshare-Test-2026!';
const report={journey:[],viewports:[],javascriptErrors:[]};report.journey.push=function(...items){console.log('PASS:',...items);return Array.prototype.push.apply(this,items);};let current;
const time=(minutes)=>new Date(Date.now()+7*3600000+minutes*60000).toISOString().slice(0,16);
async function api(page,url,method='GET',body){return page.evaluate(async({url,method,body})=>{const headers={};if(method!=='GET'){headers['Content-Type']='application/json';headers[document.querySelector('meta[name="_csrf_header"]').content]=document.querySelector('meta[name="_csrf"]').content;}const r=await fetch(url,{method,headers,body:body?JSON.stringify(body):undefined});return {status:r.status,data:await r.json().catch(()=>null)};},{url,method,body});}
async function screen(page,name){await page.evaluate(()=>window.scrollTo(0,0));await page.screenshot({path:path.join(out,name+'.png'),fullPage:true});}
async function account(browser,name){const context=await browser.newContext({viewport:{width:1366,height:900}});const page=await context.newPage();page.setDefaultTimeout(15000);page.setDefaultNavigationTimeout(15000);current=page;page.on('pageerror',e=>report.javascriptErrors.push(e.message));
 const email=name+'-'+suffix+'@example.test';await page.goto(base+'/register');await page.locator('[name=displayName]').fill(name==='owner'?'ผู้แบ่งปันทดสอบ':'ผู้รับทดสอบ');await page.locator('[name=email]').fill(email);await page.locator('[name=password]').fill(password);await page.locator('form button[type=submit]').click();await page.waitForURL('**/login');await page.locator('[name=email]').fill(email);await page.locator('[name=password]').fill(password);await page.locator('form button[type=submit]').click();await page.waitForURL('**/home');return page;
}
(async()=>{
 const browser=await chromium.launch({headless:true,executablePath:process.env.BROWSER_EXECUTABLE || undefined,args:['--no-sandbox']});
 try{
  const owner=await account(browser,'owner');report.journey.push('Register and login owner');
  await owner.goto(base+'/posts/new');await owner.locator('#editor-map .leaflet-pane').first().waitFor({state:'attached'});
  await owner.locator('[name=title]').fill('[ทดสอบ] ข้าวกะเพราไข่ดาว '+suffix);await owner.locator('[name=description]').fill('ข้อมูลสำหรับตรวจระบบ ไม่ใช่รายการอาหารที่เปิดรับจริง');await owner.locator('[name=quantity]').fill('5');await owner.locator('[name=allergens]').fill('ไข่');await owner.locator('[name=availableFrom]').fill(time(-5));await owner.locator('[name=availableUntil]').fill(time(120));await owner.locator('[name=pickupLocationName]').fill('หอสมุด มข. จุดทดสอบ');
  await owner.locator('#editor-map').click({position:{x:170,y:150}});const lat=Number(await owner.locator('[name=latitude]').inputValue());assert(lat>16&&lat<17);
  await owner.locator('#food-photo').setInputFiles(path.resolve(__dirname,'../code/src/main/resources/static/images/food-basil-rice.png'));await screen(owner,'editor-desktop');
  await owner.locator('#save-post').click();await owner.waitForURL(/\/posts\/\d+$/,{timeout:20000});const id=Number(owner.url().split('/').pop());await owner.locator('.detail-title').waitFor();
  let post=(await api(owner,'/api/v1/food-posts/'+id)).data;assert.equal(post.quantity,5);assert.equal(post.latitude,lat);assert.match(post.imageUrl,/\/media\//);assert.equal((await owner.request.get(base+post.imageUrl)).status(),200);await screen(owner,'detail-desktop');report.journey.push('Create post, click real Leaflet coordinate, upload image and read persisted data');
  const receiver=await account(browser,'receiver');current=receiver;await receiver.goto(base+'/explore');await receiver.locator('#search').fill(suffix);await receiver.locator('#search-form').evaluate(f=>f.requestSubmit());await receiver.locator('.food-card').waitFor();assert.equal(await receiver.locator('.food-card').count(),1);await receiver.locator('#map-view').click();await receiver.locator('.food-map-marker').waitFor();report.journey.push('Search and matching map pin');
  await receiver.goto(base+'/posts/'+id);await receiver.locator('#reserve-form [name=quantity]').fill('2');await receiver.locator('#reserve-form button').click();await receiver.locator('#dialog-confirm').click();await receiver.waitForURL('**/reservations');await receiver.locator('.pickup-code strong').waitFor();const code=(await receiver.locator('.pickup-code strong').first().innerText()).trim();assert.match(code,/^\d{6}$/);assert.equal((await api(receiver,'/api/v1/food-posts/'+id)).data.availableQuantity,3);report.journey.push('Reserve through UI; private pickup code and stock deduction');
  await receiver.locator('[data-change]').first().click();await receiver.locator('#dialog-input').fill('3');await receiver.locator('#dialog-confirm').click();await receiver.waitForFunction(()=>document.querySelector('.reservation-quantity strong')?.textContent==='3');assert.equal((await api(receiver,'/api/v1/food-posts/'+id)).data.availableQuantity,2);report.journey.push('Change reservation quantity');
  await receiver.setViewportSize({width:390,height:844});await screen(receiver,'reservations-mobile');
  current=owner;await owner.goto(base+'/account/posts');await owner.locator(`[data-bookings="${id}"]`).click();await owner.locator('[data-collect]').first().click();await owner.locator('#dialog-input').fill(code);await owner.locator('#dialog-confirm').click();await owner.waitForFunction(()=>document.querySelector('.managed-counts')?.textContent.includes('3'));await owner.waitForTimeout(300);post=(await api(owner,'/api/v1/food-posts/'+id)).data;assert.equal(post.collectedQuantity,3);assert.equal(post.reservedQuantity,0);await screen(owner,'my-posts-desktop');report.journey.push('Owner confirms collection using receiver code');
  await receiver.reload();await receiver.locator('.badge').first().waitFor();assert.equal(await receiver.locator('.pickup-code').count(),0);
  await owner.goto(base+'/account');await owner.locator('#profile-form [name=name]').fill('ผู้แบ่งปันที่แก้ไขชื่อ');await owner.locator('#profile-form button').click();await owner.waitForTimeout(1000);assert.match(await owner.locator('.profile-body h2').innerText(),/แก้ไขชื่อ/);report.journey.push('Edit profile');
  await receiver.goto(base+'/notifications');await receiver.locator('.notification-card').first().waitFor();report.journey.push('Persistent notifications');
  // Guaranteed external tile failure exercises honest recovery UI; no fake tiles supplied.
  await owner.route('https://tile.openstreetmap.org/**',route=>route.abort());await owner.goto(base+'/posts/new');await owner.locator('.map-load-error:not([hidden])').waitFor();await owner.locator('[name=latitude]').fill('16.4745');await owner.locator('[name=longitude]').fill('102.8237');await owner.locator('#apply-coordinates').click();assert.match(await owner.locator('#pin-status').innerText(),/16.47450/);report.journey.push('Tile failure notice and manual coordinate fallback');
  await owner.locator('#use-location').click();await owner.locator('#toast').filter({hasText:'เข้าถึงตำแหน่งไม่ได้'}).waitFor({timeout:16000});report.journey.push('Denied geolocation recovery');
  // Required field dialog can always be cancelled.
  await receiver.goto(base+'/posts/'+id);await receiver.locator('#report-post').click();await receiver.locator('dialog button[value=cancel]').last().click();assert.equal(await receiver.locator('dialog[open]').count(),0);report.journey.push('Accessible dialog cancellation with empty required field');
  for(const width of [360,390,768,1366]){
    await owner.setViewportSize({width,height:900});
    for(const route of ['/','/explore','/posts/'+id,'/account','/account/posts']){
      await owner.goto(base+route);await owner.waitForLoadState('networkidle');
      const overflow=await owner.evaluate(()=>document.documentElement.scrollWidth>innerWidth+1);assert.equal(overflow,false,`${width}px ${route} overflow`);report.viewports.push({width,route,overflow});
    }
    await owner.goto(base+'/');await owner.waitForLoadState('networkidle');await screen(owner,'home-'+width);
  }
  // Close the test food post so no test food remains available to book.
  const closed=await api(owner,'/api/v1/food-posts/'+id,'DELETE');assert.equal(closed.status,204);
  assert.deepEqual(report.javascriptErrors,[]);report.result='PASS';console.log(JSON.stringify(report,null,2));
 }catch(e){report.result='FAIL';report.error=e.message;if(current){await screen(current,'failure');fs.writeFileSync(path.join(out,'failure-body.txt'),await current.locator('body').innerText());}console.error(e);process.exitCode=1;}
 finally{fs.writeFileSync(path.join(out,'browser-result.json'),JSON.stringify(report,null,2));await browser.close();}
})().catch(e=>{console.error(e);process.exitCode=1});
