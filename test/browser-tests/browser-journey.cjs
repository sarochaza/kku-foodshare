// Run only against an isolated test/staging database. Creates clearly labelled test records.
const { chromium } = require(process.env.PLAYWRIGHT_MODULE || 'playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const base = process.env.TEST_BASE_URL || 'http://localhost:8081';
const out = path.resolve(process.env.BROWSER_SCREENSHOT_DIR || path.join(__dirname, '../reports/browser/screenshots/browser-journey'));
const reportOut = path.resolve(process.env.BROWSER_REPORT_DIR || path.join(__dirname, '../reports/browser/reports/browser-journey'));
fs.mkdirSync(out,{recursive:true});fs.mkdirSync(reportOut,{recursive:true});
const suffix=Date.now().toString(36),password='Foodshare-Test-2026!';
const report={journey:[],viewports:[],javascriptErrors:[]};report.journey.push=function(...items){console.log('PASS:',...items);return Array.prototype.push.apply(this,items);};let current;
const timeout = 30000;
const receiverLocation = {latitude:16.4745,longitude:102.8237,accuracy:15};
const time=(minutes)=>new Date(Date.now()+7*3600000+minutes*60000).toISOString().slice(0,16);
async function api(page,url,method='GET',body){return page.evaluate(async({url,method,body})=>{const headers={};if(method!=='GET'){headers['Content-Type']='application/json';headers[document.querySelector('meta[name="_csrf_header"]').content]=document.querySelector('meta[name="_csrf"]').content;}const r=await fetch(url,{method,headers,body:body?JSON.stringify(body):undefined});return {status:r.status,data:await r.json().catch(()=>null)};},{url,method,body});}
async function screen(page,name){await page.evaluate(()=>window.scrollTo(0,0));await page.screenshot({path:path.join(out,name+'.png'),fullPage:true});}
async function account(browser,name){const context=await browser.newContext({viewport:{width:1366,height:900}});const page=await context.newPage();page.setDefaultTimeout(timeout);page.setDefaultNavigationTimeout(timeout);current=page;await page.route('https://tile.openstreetmap.org/**',r=>r.fulfill({contentType:'image/png',body:Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aDWkAAAAASUVORK5CYII=','base64')}));page.on('pageerror',e=>report.javascriptErrors.push(e.message));
 const email=name+'-'+suffix+'@example.test';await page.goto(base+'/register');await page.locator('[name=displayName]').fill(name==='owner'?'ผู้แบ่งปันทดสอบ':'ผู้รับทดสอบ');await page.locator('[name=email]').fill(email);await page.locator('[name=password]').fill(password);await page.locator('form.auth-form button[type=submit]').click();await page.waitForURL('**/login');await page.locator('[name=email]').fill(email);await page.locator('[name=password]').fill(password);await page.locator('form.auth-form button[type=submit]').click();await page.waitForURL('**/home');
 await dismissGuide(page);return page;
}
async function waitForApi(page, method, pathname, action, status, query = null) {
 const pending=page.waitForResponse(r=>{
  const url=new URL(r.url());
  return r.request().method()===method && url.pathname===pathname && (!query || query(url));
 });
 const [response]=await Promise.all([pending,action()]);
 assert.equal(response.status(),status,method+' '+pathname+' returned HTTP '+response.status());
 return status===204?null:response.json();
}
async function dismissGuide(page) {
 const needed=await page.locator('meta[name="onboarding-needed"]').getAttribute('content');
 if(needed!=='true')return;
 await page.locator('#home-guide[open]').waitFor();
 await waitForApi(page,'POST','/api/v1/me/onboarding',()=>page.locator('#guide-skip').click(),204);
 await page.locator('#home-guide').waitFor({state:'hidden'});
}
async function postData(page,id) {
 const response=await api(page,'/api/v1/food-posts/'+id);
 assert.equal(response.status,200);return response.data;
}

(async()=>{
 const browser=await chromium.launch({headless:true,executablePath:process.env.BROWSER_EXECUTABLE || undefined,args:['--no-sandbox']});
 try{
  const owner=await account(browser,'owner');report.journey.push('Register and login owner');
  await owner.goto(base+'/posts/new');await owner.locator('#editor-map .leaflet-pane').first().waitFor({state:'attached'});
  // Deterministic geocoder contract fixture; does not claim availability of the external service.
  await owner.route('https://photon.komoot.io/**', route=>route.fulfill({json:{features:[{geometry:{coordinates:[102.8237,16.4745]},properties:{name:'หอสมุด มข.',city:'ขอนแก่น',countrycode:'TH'}}]}}));
  await owner.locator('#place-search').fill('หอสมุด');await owner.locator('.place-result').filter({has: owner.getByText('หอสมุด มข.', {exact:true})}).waitFor();await owner.locator('.place-result').filter({has: owner.getByText('หอสมุด มข.', {exact:true})}).click();
  assert.equal(await owner.locator('[name=latitude]').getAttribute('type'),'hidden');assert.equal(Number(await owner.locator('[name=latitude]').inputValue()),16.4745);
  assert.equal(await owner.locator('[name=pickupLocationName]').inputValue(),'หอสมุด มข.');
  const beforeDrag=await owner.locator('[name=longitude]').inputValue();const pin=await owner.locator('#editor-map .leaflet-marker-icon').boundingBox();
  await owner.mouse.move(pin.x+pin.width/2,pin.y+pin.height/2);await owner.mouse.down();await owner.mouse.move(pin.x+pin.width/2+45,pin.y+pin.height/2+25,{steps:10});await owner.mouse.up();
  assert.notEqual(await owner.locator('[name=longitude]').inputValue(),beforeDrag);
  await owner.context().grantPermissions(['geolocation'],{origin:base});await owner.context().setGeolocation({latitude:16.48,longitude:102.83,accuracy:15});await owner.locator('#use-location').click();
  await owner.waitForFunction(()=>document.querySelector('[name=latitude]').value==='16.4800000');
  report.journey.push('Place suggestions (fixture), selection, hidden coordinates, real marker drag and current device location');
  await owner.locator('[name=title]').fill('[ทดสอบ] ข้าวกะเพราไข่ดาว '+suffix);await owner.locator('[name=description]').fill('ข้อมูลสำหรับตรวจระบบ ไม่ใช่รายการอาหารที่เปิดรับจริง');await owner.locator('[name=quantity]').fill('5');await owner.locator('[name=allergens]').fill('ไข่');await owner.locator('[name=availableFrom]').fill(time(-5));await owner.locator('[name=availableUntil]').fill(time(120));await owner.locator('[name=pickupLocationName]').fill('หอสมุด มข. จุดทดสอบ');
  await owner.locator('#editor-map').click({position:{x:170,y:150}});const lat=Number(await owner.locator('[name=latitude]').inputValue());assert(lat>16&&lat<17);
  await owner.locator('#food-photo').setInputFiles(path.resolve(__dirname,'../../code/src/main/resources/static/images/food-basil-rice.png'));await screen(owner,'editor-desktop');
  await owner.locator('#save-post').click();await owner.waitForURL(/\/posts\/\d+$/,{timeout:20000});const id=Number(owner.url().split('/').pop());await owner.locator('.detail-title').waitFor();
  let post=(await api(owner,'/api/v1/food-posts/'+id)).data;assert.equal(post.quantity,5);assert.equal(post.latitude,lat);assert.match(post.imageUrl,/\/media\//);assert.equal((await owner.request.get(base+post.imageUrl)).status(),200);await screen(owner,'detail-desktop');report.journey.push('Create post, click real Leaflet coordinate, upload image and read persisted data');
  await owner.goto(base+'/posts/'+id+'/edit');await owner.waitForFunction(()=>!!document.querySelector('[name=latitude]').value);
  assert.equal(Number(await owner.locator('[name=latitude]').inputValue()),lat);assert.equal(await owner.locator('#place-search').inputValue(),'หอสมุด มข. จุดทดสอบ');
  await owner.setViewportSize({width:390,height:844});await owner.locator('#place-search').fill('หอสมุด');await owner.locator('.place-result').filter({has: owner.getByText('หอสมุด มข.', {exact:true})}).waitFor();
  assert.equal(await owner.evaluate(()=>document.documentElement.scrollWidth>innerWidth+1),false);
  await owner.locator('.place-result').filter({has: owner.getByText('หอสมุด มข.', {exact:true})}).click();await owner.locator('[name=pickupLocationName]').fill('หน้าประตูฝั่งหอสมุด');await owner.locator('#save-post').click();await owner.waitForURL(/\/posts\/\d+$/);
  const edited=(await api(owner,'/api/v1/food-posts/'+id)).data;assert.equal(edited.latitude,16.4745);assert.equal(edited.longitude,102.8237);assert.equal(edited.pickupLocationName,'หน้าประตูฝั่งหอสมุด');
  await owner.setViewportSize({width:1366,height:900});report.journey.push('Edit restores prior coordinates and name; mobile suggestions fit; edited position persists');
  const receiver=await account(browser,'receiver');current=receiver;
  await receiver.context().grantPermissions(['geolocation'],{origin:base});
  await receiver.context().setGeolocation(receiverLocation);
  await receiver.goto(base+'/explore');
  await receiver.locator('#search').fill(suffix);
  const search=await waitForApi(receiver,'GET','/api/v1/food-posts',
   ()=>receiver.locator('#search-form').evaluate(f=>f.requestSubmit()),200,
   url=>url.searchParams.get('q')===suffix && url.searchParams.get('size')!=='200');
  assert.equal(search.totalElements,1);assert.equal(search.items[0].id,id);
  await receiver.locator(`#food-results [data-feed-post="${id}"]`).waitFor();
  await receiver.waitForFunction(postId=>{
   const cards=document.querySelectorAll('#food-results [data-feed-post]');
   return cards.length===1 && cards[0].dataset.feedPost===String(postId);
  },id);
  await receiver.locator('#map-view').click();
  await receiver.locator('#explore-map .food-map-marker').waitFor();
  assert.equal(await receiver.locator('#explore-map .food-map-marker').count(),1);
  report.journey.push('Search returns this run\'s post and one matching map pin');
  await receiver.goto(base+'/?guide');await receiver.locator('#home-guide:not([hidden])').waitFor();await receiver.locator('#guide-next').click();assert.match(await receiver.locator('#guide-title').innerText(),/เลือกสิ่งที่อยากรับ/);await waitForApi(receiver,'POST','/api/v1/me/onboarding',()=>receiver.locator('#guide-skip').click(),204);await receiver.locator('#home-guide').waitFor({state:'hidden'});report.journey.push('Interactive home guide replay and account completion');
  await receiver.locator('#home-live-map .leaflet-pane').first().waitFor({state:'attached'});await receiver.locator('.food-map-marker').first().waitFor();const homePin = receiver.locator('#home-live-map .food-map-marker').first();
await homePin.scrollIntoViewIfNeeded();
const homePinBox = await homePin.boundingBox();
assert.ok(homePinBox, 'Home map marker is not visible');
await receiver.mouse.click(
    homePinBox.x + homePinBox.width / 2,
    homePinBox.y + homePinBox.height / 2
);await receiver.locator('#home-map-preview:not([hidden])').waitFor();
  await receiver.context().grantPermissions(['geolocation'],{origin:base});await receiver.context().setGeolocation({latitude:16.4745,longitude:102.8237});await receiver.locator('#home-locate').click();await receiver.waitForFunction(()=>document.querySelector('#home-sort').value==='nearby');await receiver.locator('#home-live-map .current-location-marker').waitFor();
  await receiver.locator('[data-home-category="FOOD"]').click();await receiver.locator('[data-home-category="FOOD"][aria-pressed="true"]').waitFor();report.journey.push('Live home map, food-pin preview, category filter and current location');
  await receiver.route('https://routing.openstreetmap.de/**',route=>route.fulfill({json:{code:'Ok',routes:[{distance:950,duration:750,geometry:{type:'LineString',coordinates:[[102.8237,16.4745],[102.8237,16.4745]]}}]}}));
  await receiver.goto(base+'/posts/'+id);
  assert.equal(await receiver.locator('[data-trip-mode]').inputValue(),'driving');
  // The current UI automatically confirms the device origin; locate/confirm buttons were removed.
  await receiver.locator('[data-trip-summary]').filter({hasText:'ขับรถ'}).waitFor();
  await receiver.locator('[data-trip-directions]:not([hidden])').waitFor();
  await receiver.locator('[data-trip-mode]').selectOption('walking');
  await receiver.locator('[data-trip-summary]').filter({hasText:'เดินเท้า'}).waitFor();
  assert.match(await receiver.locator('[data-trip-summary]').innerText(),/13 นาที/);
  await receiver.locator('[data-trip-mode]').selectOption('driving');
  await receiver.locator('[data-trip-summary]').filter({hasText:'ขับรถ'}).waitFor();
  const navigation=new URL(await receiver.locator('[data-trip-directions]').getAttribute('href'));
  assert.equal(navigation.searchParams.get('origin'),'16.4745,102.8237');
  assert.equal(navigation.searchParams.get('destination'),edited.latitude+','+edited.longitude);
  assert.equal(navigation.searchParams.get('travelmode'),'driving');
  report.journey.push('Automatic confirmed origin, walking/driving preview and Google Maps handoff (routing fixture)');

  await receiver.locator('[data-booking-form] [data-quantity]').fill('2');
  await receiver.locator('[data-booking-form] [data-save]').click();
  const reservation=await waitForApi(receiver,'POST',`/api/v1/food-posts/${id}/reservations`,
   ()=>receiver.locator('#dialog-confirm').click(),201);
  const reservationId=reservation.id;
  assert.ok(Number.isSafeInteger(reservationId));
  await receiver.locator('[data-qr]').waitFor();
  await receiver.goto(base+`/reservations#reservation-${reservationId}`);
  const ticket=receiver.locator(`#reservation-${reservationId}`);
  await ticket.locator('.pickup-code strong').waitFor();
  const code=(await ticket.locator('.pickup-code strong').innerText()).trim();
  assert.match(code,/^\d{6}$/);
  assert.equal((await postData(receiver,id)).availableQuantity,3);
  report.journey.push('Reserve through UI; scoped pickup code and stock deduction');

  await ticket.locator(`[data-change="${reservationId}"]`).click();
  await receiver.locator('#dialog-input').fill('3');
  const changed=await waitForApi(receiver,'PUT',`/api/v1/reservations/${reservationId}`,
   ()=>receiver.locator('#dialog-confirm').click(),200);
  assert.equal(changed.quantity,3);
  await receiver.waitForFunction(rid=>document.querySelector(`#reservation-${rid} .reservation-quantity strong`)?.textContent==='3',reservationId);
  assert.equal((await postData(receiver,id)).availableQuantity,2);
  report.journey.push('Change reservation quantity and verify persisted stock');
  await receiver.setViewportSize({width:390,height:844});await screen(receiver,'reservations-mobile');
  current=owner;await owner.goto(base+'/account/posts');
  await owner.locator(`[data-bookings="${id}"]`).click();
  await owner.locator(`#bookings-${id} [data-collect="${reservationId}"]`).click();
  await owner.locator('#dialog-input').fill(code);
  const collected=await waitForApi(owner,'POST',`/api/v1/reservations/${reservationId}/collection`,
   ()=>owner.locator('#dialog-confirm').click(),200);
  assert.equal(collected.status,'COLLECTED');
  await owner.waitForFunction(postId=>{
   const button=document.querySelector(`[data-bookings="${postId}"]`);
   const counts=button?.closest('[data-owner-card]')?.querySelectorAll('.managed-counts strong');
   return counts?.[1]?.textContent==='0' && counts?.[2]?.textContent==='3';
  },id);
  post=await postData(owner,id);
  assert.equal(post.collectedQuantity,3);assert.equal(post.reservedQuantity,0);
  await screen(owner,'my-posts-desktop');report.journey.push('Owner confirms this reservation using receiver code');

  current=receiver;await receiver.reload();
  await ticket.locator('.badge').waitFor();
  await receiver.waitForFunction(rid=>document.querySelector(`#reservation-${rid}`)?.dataset.reservationStatus==='COLLECTED',reservationId);
  assert.equal(await ticket.locator('.pickup-code').count(),0);

  current=owner;await owner.goto(base+'/account');
  await owner.locator('#profile-form [name=name]').fill('ผู้แบ่งปันที่แก้ไขชื่อ');
  await waitForApi(owner,'PATCH','/api/v1/me/profile',()=>owner.locator('#profile-form button[type=submit]').click(),204);
  await owner.locator('.profile-body h2').filter({hasText:'ผู้แบ่งปันที่แก้ไขชื่อ'}).waitFor();
  report.journey.push('Edit profile and wait for the saved name after reload');

  current=receiver;
  await receiver.goto(base+'/notifications');await receiver.locator('.notification-card').first().waitFor();report.journey.push('Persistent notifications');
  // Guaranteed external tile failure exercises honest recovery UI; no fake tiles supplied.
  await owner.route('https://tile.openstreetmap.org/**',route=>route.abort());await owner.goto(base+'/posts/new');await owner.locator('.map-load-error:not([hidden])').waitFor();await owner.locator('#manual-latitude').fill('16.4745');await owner.locator('#manual-longitude').fill('102.8237');await owner.locator('#apply-coordinates').click();assert.equal(await owner.locator('[name=latitude]').inputValue(),'16.4745000');report.journey.push('Tile failure notice and manual coordinate fallback');
  await owner.unroute('https://photon.komoot.io/**');await owner.route('https://photon.komoot.io/**',route=>route.abort());await owner.locator('#place-search').fill('ค้นหาไม่ได้');await owner.locator('#place-search-status').filter({hasText:'ค้นหาเพิ่มไม่ได้'}).waitFor();assert.equal(await owner.locator('.manual-coordinates').getAttribute('open'),'');report.journey.push('Offline geocoder exposes manual fallback');
  await owner.context().clearPermissions();await owner.locator('#use-location').click();await owner.locator('#pin-status').filter({hasText:/ยังไม่ได้อนุญาตตำแหน่ง|เข้าถึงตำแหน่งไม่ได้/}).waitFor();report.journey.push('Denied geolocation recovery');
  // Required field dialog can always be cancelled.
  await receiver.goto(base+'/posts/'+id);await receiver.locator('#report-post').click();await receiver.locator('#action-dialog[open] .dialog-close').click();assert.equal(await receiver.locator('#action-dialog[open]').count(),0);report.journey.push('Accessible dialog cancellation with empty required field');
  // Return external-service fixtures and permissions to normal before checking normal layouts.
  await owner.unroute('https://tile.openstreetmap.org/**');
  await owner.route('https://tile.openstreetmap.org/**',r=>r.fulfill({contentType:'image/png',body:Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aDWkAAAAASUVORK5CYII=','base64')}));
  await owner.unroute('https://photon.komoot.io/**');
  await owner.context().grantPermissions(['geolocation'],{origin:base});
  await owner.context().setGeolocation({latitude:16.48,longitude:102.83,accuracy:15});
  current=owner;
  for(const width of [360,390,768,1366]){
    await owner.setViewportSize({width,height:900});
    for(const route of ['/','/explore','/posts/'+id,'/account','/account/posts']){
      await owner.goto(base+route);
      if(route==='/')await owner.locator('#home-live-map .leaflet-pane').first().waitFor({state:'attached'});
      else if(route==='/explore')await owner.locator('#food-results [data-feed-post]').first().waitFor();
      else if(route==='/posts/'+id)await owner.locator('.detail-title').waitFor();
      else if(route==='/account')await owner.locator('#profile-form').waitFor();
      else await owner.locator(`[data-bookings="${id}"]`).waitFor();
      await owner.evaluate(async()=>{await document.fonts.ready;await new Promise(r=>requestAnimationFrame(()=>requestAnimationFrame(r)));});
      const overflow=await owner.evaluate(()=>document.documentElement.scrollWidth>innerWidth+1);assert.equal(overflow,false,`${width}px ${route} overflow`);report.viewports.push({width,route,overflow});
    }
    await owner.goto(base+'/');await owner.locator('#home-live-map .leaflet-pane').first().waitFor({state:'attached'});await screen(owner,'home-'+width);
  }
  // Close the test food post so no test food remains available to book.
  const closed=await api(owner,'/api/v1/food-posts/'+id,'DELETE');assert.equal(closed.status,204);
  assert.deepEqual(report.javascriptErrors,[]);report.result='PASS';console.log(JSON.stringify(report,null,2));
 }catch(e){report.result='FAIL';report.error=e.stack||e.message;if(current){try{await screen(current,'failure');fs.writeFileSync(path.join(reportOut,'failure-body.txt'),await current.locator('body').innerText());}catch(captureError){report.captureError=captureError.message;}}console.error(e);process.exitCode=1;}
 finally{fs.writeFileSync(path.join(reportOut,'browser-result.json'),JSON.stringify(report,null,2));await browser.close();}
})().catch(e=>{console.error(e);process.exitCode=1});
