// Creates clearly labelled test data. Use an isolated test/staging database only.
const {chromium} = require(process.env.PLAYWRIGHT_MODULE || 'playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const base = process.env.TEST_BASE_URL || 'http://localhost:8081';
const out = path.resolve(__dirname, '../img/phase5'); fs.mkdirSync(out, {recursive:true});
const report = {checks:[],viewports:[],javascriptErrors:[]};
const suffix = Date.now().toString(36), password = 'Foodshare-Test-2026!';
let current, owner, postId;
const pass = (label) => {report.checks.push(label); console.log('PASS:',label);};
async function api(page, url, method='GET', body) {
  return page.evaluate(async ({url,method,body}) => {
    const headers={};
    if (method !== 'GET') {headers['Content-Type']='application/json';headers[document.querySelector('meta[name="_csrf_header"]').content]=document.querySelector('meta[name="_csrf"]').content;}
    const r=await fetch(url,{method,headers,body:body?JSON.stringify(body):undefined});
    return {status:r.status,data:await r.json().catch(()=>null)};
  },{url,method,body});
}
async function account(browser, name) {
  const context=await browser.newContext({viewport:{width:1366,height:900},permissions:['geolocation'],geolocation:{latitude:16.4745,longitude:102.8237,accuracy:20}});
  await context.addInitScript(() => localStorage.setItem("foodshare-guide-v2", "done"));
  const page=await context.newPage();current=page; page.setDefaultTimeout(15000);
  page.on('pageerror',e=>report.javascriptErrors.push(e.message));
  // External-service fixtures exercise the UI contract, not live provider availability.
  await page.route('https://tile.openstreetmap.org/**',r=>r.fulfill({contentType:'image/png',body:Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+aDWkAAAAASUVORK5CYII=','base64')}));
  await page.route('https://routing.openstreetmap.de/**',r=>r.fulfill({json:{code:'Ok',routes:[{distance:1100,duration:180,geometry:{type:'LineString',coordinates:[[102.8237,16.4745],[102.83,16.48]]}}]}}));
  const email=`phase5-${name}-${suffix}@example.test`;
  await page.goto(base+'/register');await page.locator('[name=displayName]').fill('ทดสอบ '+name);
  await page.locator('[name=email]').fill(email);await page.locator('[name=password]').fill(password);await page.locator('form button[type=submit]').click();await page.waitForURL('**/login');
  await page.locator('[name=email]').fill(email);await page.locator('[name=password]').fill(password);await page.locator('form button[type=submit]').click();await page.waitForURL('**/home');
  return page;
}
async function confirm(page) {await page.locator('#dialog-confirm').click();}
async function snapshot(page,id) {const r=await api(page,`/api/v1/food-posts/${id}`);assert.equal(r.status,200);return r.data;}
async function openStock(page) {
  const b=page.locator(`[data-stock="${postId}"]`);
  if (await b.getAttribute('aria-expanded') !== 'true') await b.click();
  await page.locator('[data-stock-action]').waitFor();
}
async function stockAction(page,action,amount) {
  await openStock(page);await page.locator('[data-stock-action]').selectOption(action);await page.locator('.owner-stock [data-quantity]').fill(String(amount));
  await page.locator('.owner-stock [data-save]').click();await confirm(page);
  await page.locator('.owner-stock [data-stock-action]').waitFor({state:'detached'});
}
async function overflow(page,width,label) {
  assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth+1),false,`${width}px ${label} overflow`);
  report.viewports.push({width,label,overflow:false});
}
(async()=>{
 const browser=await chromium.launch({headless:true,executablePath:process.env.BROWSER_EXECUTABLE||undefined,args:['--no-sandbox']});
 try {
  owner=await account(browser,'owner');const receiver=await account(browser,'receiver');
  const time=minutes=>new Date(Date.now()+7*3600000+minutes*60000).toISOString().slice(0,19);
  const created=await api(owner,'/api/v1/food-posts','POST',{title:'[ทดสอบ Phase5] อาหาร '+suffix,description:'ข้อมูลทดสอบ ไม่ใช่อาหารจริง',category:'FOOD',quantity:5,unit:'กล่อง',pickupLocationName:'หอสมุด มข. จุดทดสอบ',latitude:16.48,longitude:102.83,availableFrom:time(-10),availableUntil:time(120),allergens:''});
  assert.equal(created.status,201);postId=created.data.id;
  await owner.goto(base+'/');await owner.locator('.owner-shortcut').click();await owner.waitForURL('**/account/posts');await owner.locator(`[data-stock="${postId}"]`).waitFor();pass('Top My posts shortcut opens owner workspace');
  await receiver.goto(base+'/posts/'+postId);await receiver.locator('[data-booking-form]').waitFor();
  await receiver.locator('[data-trip-confirm]:not([disabled])').waitFor();await receiver.locator('[data-trip-confirm]').click();
  await receiver.locator('[data-plus]').click();await receiver.locator('[data-plus]').click();assert.equal(await receiver.locator('[data-quantity]').inputValue(),'3');
  await receiver.locator('[data-save]').click();await confirm(receiver);await receiver.locator('[data-qr]').waitFor();
  let mine=(await api(receiver,`/api/v1/food-posts/${postId}/my-reservation`)).data;const rid=mine.id,code=mine.pickupCode;
  assert.equal(mine.quantity,3);assert.equal((await snapshot(receiver,postId)).availableQuantity,2);pass('New reservation stays on detail and reveals inline edit and QR controls');
  await receiver.locator('[data-quantity]').fill('5');await receiver.locator('[data-save]').click();await confirm(receiver);
  await receiver.waitForFunction(()=>document.querySelector('.my-booking-label strong')?.textContent==='5 กล่อง');
  await receiver.reload();await receiver.locator('[data-booking-form]').waitFor();assert.equal(await receiver.locator('[data-quantity]').inputValue(),'5');
  await receiver.locator('[data-minus]').click();await receiver.locator('[data-save]').click();await confirm(receiver);
  await receiver.waitForFunction(()=>document.querySelector('.my-booking-label strong')?.textContent==='4 กล่อง');
  mine=(await api(receiver,`/api/v1/food-posts/${postId}/my-reservation`)).data;
  assert.equal(mine.id,rid);assert.equal(mine.pickupCode,code);pass('Full post reloads original booking; +/- edit preserves reservation id and pickup code');
  await receiver.locator('[data-qr]').click();await receiver.locator('[data-quick-pass] path').waitFor();
  let qr=await receiver.locator('[data-quick-pass]').screenshot();assert.match(await receiver.locator('.quick-pickup .pickup-code strong').innerText(),/^\d{6}$/);pass('QR opens directly under the same post');
  await owner.reload();await openStock(owner);await owner.locator('[data-stock-action]').selectOption('OFFLINE');await owner.locator('.owner-stock [data-quantity]').fill('2');
  assert.equal(await owner.locator('.owner-stock [data-save]').isDisabled(),true);
  assert.match(await owner.locator('[data-stock-preview]').innerText(),/กันของให้ผู้จอง/);
  await stockAction(owner,'OFFLINE',1);let p=await snapshot(owner,postId);assert.equal(p.offlineQuantity,1);assert.equal(p.reservedQuantity,4);assert.equal(p.availableQuantity,0);pass('Offline distribution consumes only unreserved stock; booked quantity stays protected');
  await stockAction(owner,'UNDO_OFFLINE',1);assert.equal((await snapshot(owner,postId)).availableQuantity,1);pass('Mistaken offline count can be corrected');
  // Snapshot changes while the owner is reviewing: confirmation must not overwrite new bookings.
  await openStock(owner);await owner.locator('[data-stock-action]').selectOption('OFFLINE');
  await receiver.locator('[data-quantity]').fill('5');await receiver.locator('[data-save]').click();await confirm(receiver);
  await receiver.waitForFunction(()=>document.querySelector('.my-booking-label strong')?.textContent==='5 กล่อง');
  await owner.locator('.owner-stock [data-save]').click();await confirm(owner);
  await owner.locator('.owner-stock [role=alert]').filter({hasText:'จำนวนเปลี่ยนไปแล้ว'}).waitFor();
  p=await snapshot(owner,postId);assert.equal(p.offlineQuantity,0);assert.equal(p.reservedQuantity,5);pass('Stale owner snapshot is rejected and refreshed without applying the offline count');
  await receiver.locator('[data-cancel]').click();await confirm(receiver);await receiver.locator('[data-booking-form]').waitFor();
  await receiver.waitForFunction(()=>!document.querySelector('[data-qr]'));assert.equal((await snapshot(receiver,postId)).availableQuantity,5);pass('Cancel directly from a full post restores stock');
  await receiver.locator('[data-trip-locate]').click();await receiver.locator('[data-trip-confirm]:not([disabled])').waitFor();await receiver.locator('[data-trip-confirm]').click();
  await receiver.locator('[data-quantity]').fill('4');await receiver.locator('[data-save]').click();await confirm(receiver);await receiver.locator('[data-qr]').waitFor();
  await receiver.locator('[data-qr]').click();await receiver.locator('[data-quick-pass] path').waitFor();qr=await receiver.locator('[data-quick-pass]').screenshot();
  await owner.reload();await stockAction(owner,'OFFLINE',1);
  await owner.locator(`[data-bookings="${postId}"]`).click();await owner.locator('[data-scan]').click();await owner.locator('#scan-image').setInputFiles({name:'pickup.png',mimeType:'image/png',buffer:qr});
  await owner.locator('#scan-review:not([hidden])').waitFor();assert.match(await owner.locator('#scan-quantity').innerText(),/4 กล่อง/);
  await owner.locator('#scan-confirm').click();await owner.locator('#scan-success:not([hidden])').waitFor();await owner.locator('#scan-done').click();
  p=await snapshot(owner,postId);assert.equal(p.status,'CLAIMED');assert.equal(p.collectedQuantity,4);assert.equal(p.offlineQuantity,1);pass('Existing QR image scanner and handover still work alongside offline distribution');
  await stockAction(owner,'ADD',2);assert.equal((await snapshot(owner,postId)).availableQuantity,2);pass('Adding food reopens a completed post before expiry');
  await stockAction(owner,'REMOVE',1);assert.equal((await snapshot(owner,postId)).quantity,6);pass('Removing unused food does not count as a donation');
  // Test mobile owner panel and new/existing reservation layouts.
  await receiver.goto(base+'/posts/'+postId);await receiver.locator('[data-booking-form]').waitFor();
  for (const width of [320,360,390,768,1366]) {
    await owner.setViewportSize({width,height:900});await owner.reload();await openStock(owner);await overflow(owner,width,'owner stock panel');
    await receiver.setViewportSize({width,height:900});await overflow(receiver,width,'detail new reservation');
    const touch=await owner.locator('.owner-stock [data-plus]').boundingBox();assert(touch.height>=44);
    const shortcut=await owner.locator('.owner-shortcut').boundingBox();assert(shortcut.height>=44);
    if (width===390) {await owner.evaluate(()=>scrollTo(0,0));await receiver.evaluate(()=>scrollTo(0,0));await owner.screenshot({path:path.join(out,'owner-stock-mobile.png'),fullPage:true});await receiver.screenshot({path:path.join(out,'detail-mobile.png'),fullPage:true});}
  }
  await receiver.locator('[data-trip-locate]').click();await receiver.locator('[data-trip-confirm]:not([disabled])').waitFor();await receiver.locator('[data-trip-confirm]').click();
  await receiver.locator('[data-quantity]').fill('1');await receiver.locator('[data-save]').click();await confirm(receiver);await receiver.locator('[data-qr]').waitFor();await receiver.locator('[data-qr]').click();
  for (const width of [320,390,1366]) {await receiver.setViewportSize({width,height:900});await overflow(receiver,width,'detail existing reservation with QR');}
  await receiver.setViewportSize({width:390,height:844});await receiver.evaluate(()=>scrollTo(0,0));await receiver.screenshot({path:path.join(out,'inline-reservation-mobile.png'),fullPage:true});
  // Commit on the server, then lose the creation response before it reaches the browser.
  await receiver.locator('[data-cancel]').click();await confirm(receiver);
  await receiver.waitForFunction(()=>!document.querySelector('[data-qr]') && document.querySelector('[data-booking-form]'));
  await receiver.locator('[data-trip-locate]').click();await receiver.locator('[data-trip-confirm]:not([disabled])').waitFor();await receiver.locator('[data-trip-confirm]').click();
  let loseResponse=true;
  const dropCreation=async route=>{if(loseResponse){loseResponse=false;await route.fetch();await route.abort();}else await route.continue();};
  await receiver.route(`**/api/v1/food-posts/${postId}/reservations`,dropCreation);
  await receiver.locator('[data-save]').click();await confirm(receiver);await receiver.locator('[data-qr]').waitFor();
  const recovered=(await api(receiver,`/api/v1/food-posts/${postId}/my-reservation`)).data;
  await receiver.locator('[data-cancel]').click();await confirm(receiver);
  await receiver.waitForFunction(()=>!document.querySelector('[data-qr]') && document.querySelector('[data-booking-form]'));
  await receiver.locator('[data-save]').click();await confirm(receiver);await receiver.locator('[data-qr]').waitFor();
  const rebooked=(await api(receiver,`/api/v1/food-posts/${postId}/my-reservation`)).data;
  assert(rebooked && rebooked.id!==recovered.id);assert.equal(rebooked.quantity,1);
  pass('Lost committed booking response recovers; cancel and rebook creates a fresh active reservation');
  // Lookup failure must not offer a new reservation, which could create confusion/duplicates.
  await receiver.route(`**/api/v1/food-posts/${postId}/my-reservation`,r=>r.abort());await receiver.reload();await receiver.locator('#detail-booking [data-retry]').waitFor();
  assert.equal(await receiver.locator('[data-booking-form]').count(),0);pass('Reservation lookup outage offers retry, not another booking');
  assert.deepEqual(report.javascriptErrors,[]);pass('No browser JavaScript errors; all tested mobile layouts fit');report.result='PASS';
 } catch(e) {report.result='FAIL';report.error=e.message;console.error(e);process.exitCode=1;if(current) {await current.screenshot({path:path.join(out,'failure.png'),fullPage:true});fs.writeFileSync(path.join(out,'failure-body.txt'),await current.locator('body').innerText());}}
 finally {
  if (owner && postId) await api(owner,`/api/v1/food-posts/${postId}`,'DELETE').catch(()=>{});
  fs.writeFileSync(path.join(out,'browser-result.json'),JSON.stringify(report,null,2));await browser.close();
 }
})().catch(e=>{console.error(e);process.exitCode=1});
