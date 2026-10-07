import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import * as placeModule from '../../main/resources/static/js/places.mjs';
import { localPlaces, placePicker, searchRemotePlaces } from '../../main/resources/static/js/places.mjs';
// Minimal DOM adapter: execute real picker handlers; mock only browser/remote boundaries.
class Element {
  constructor() {this.value='';this.hidden=false;this.children=[];this.listeners={};this.attrs={};this.textContent='';}
  addEventListener(type,fn) {this.listeners[type]=fn;}
  setAttribute(k,v) {this.attrs[k]=v;}
  replaceChildren(){this.children=[];}
  append(el){this.children.push(el);}
  focus() {this.focused=true;}
  blur(){}
  scrollIntoView(){this.scrolled=true;}
  get firstElementChild(){return this.children[0];}
}
function setup() {
  const nodes=Object.fromEntries(['place-search','place-search-button','place-results','place-search-status','pin-status','manual-latitude','manual-longitude','apply-coordinates','use-location','maps-link-input','apply-maps-link','maps-link-status','advanced','panel'].map(k=>[k,new Element()]));
  globalThis.document={getElementById:id=>nodes[id],querySelector:s=>nodes[s==='.manual-coordinates'?'advanced':'panel'],createElement:()=>new Element(),addEventListener(){}};
  globalThis.matchMedia=()=>({matches:false});
  const form={elements:{latitude:new Element(),longitude:new Element(),pickupLocationName:new Element()}};
  let point;
  const picker=placePicker(form,{setPin:(lat,lng)=>point=[lat,lng]},async()=>({lat:16.48,lng:102.83}));
  return {nodes,form,picker,get point(){return point;}};
}
const answer=(name='หอสมุด',lat=16.47,lng=102.82)=>({ok:true,json:async()=>({features:[{geometry:{coordinates:[lng,lat]},properties:{name,countrycode:'TH',city:'ขอนแก่น'}}]})});
test('home controls stay aligned and the comments feature remains without its header strip',()=>{
  const root=new URL('../../main/resources/',import.meta.url);
  const home=readFileSync(new URL('templates/home.html',root),'utf8');
  const fragments=readFileSync(new URL('templates/fragments.html',root),'utf8');
  const css=readFileSync(new URL('static/css/app.css',root),'utf8');
  assert.match(home,/class="home-sort"/);assert.match(home,/class="home-now-compact"/);assert.match(home,/class="home-sharing-map-icon"/);
  assert.match(css,/\.home-discovery-toolbar > \.home-sharing-map-icon/);assert.match(css,/grid-template-columns: minmax\(0,1fr\) auto 50px/);
  assert.doesNotMatch(fragments,/<header class="comments-heading"/);
  assert.match(fragments,/id="comments-dialog"[\s\S]*class="comments-close"/);
  assert.match(fragments,/data-comments-form/);assert.match(fragments,/data-comments-list/);
});
test('About page shows clearly labeled demo contact channels',()=>{
  const root=new URL('../../main/resources/',import.meta.url);
  const about=readFileSync(new URL('templates/about.html',root),'utf8');
  const css=readFileSync(new URL('static/css/about.css',root),'utf8');
  assert.match(about,/ช่องทางตัวอย่างสำหรับเดโม/);assert.match(about,/foodshare@example\.com/);assert.match(about,/FoodShare · KKU/);
  assert.match(css,/\.about-contact-grid/);assert.match(css,/\.about-contact-grid \{ grid-template-columns: minmax\(0,1fr\); \}/);
});
test('local KKU aliases return instant KKU-first suggestions without a network request',()=>{
  for(const query of ['หอสมุด','library','complex']) {
    const places=localPlaces(query);
    assert.ok(places.length>0, query);
    assert.match(places[0].label,/มหาวิทยาลัยขอนแก่น|มข\./);
  }
  assert.deepEqual(localPlaces('ตลาดที่ไม่มีอยู่จริง'),[]);
});
test('remote search is Thailand-limited, restricted to nearby KKU, and cached',async()=>{
  let calls=0, url='';
  const fetcher=async requested=>{calls++;url=requested;return answer('สถานที่ขอนแก่น',16.47,102.82);};
  const first=await searchRemotePlaces('สถานที่ทดสอบ',undefined,fetcher);
  const second=await searchRemotePlaces('สถานที่ทดสอบ',undefined,fetcher);
  assert.equal(calls,1);
  assert.match(url,/countrycode=TH/);
  assert.equal(first[0].name,'สถานที่ขอนแก่น');
  assert.deepEqual(second,first);
});
test('remote search can return up to five Khon Kaen places while ranking the closest KKU result first',async()=>{
  const coords=[[16.48,102.83],[16.50,102.84],[16.52,102.86],[16.43,102.80],[16.55,102.88],[16.40,102.79]];
  const fetcher=async()=>({ok:true,json:async()=>({features:coords.map(([lat,lng],i)=>({geometry:{coordinates:[lng,lat]},properties:{name:`สถานที่ ${i+1}`,countrycode:'TH',city:'ขอนแก่น'}}))})});
  const places=await searchRemotePlaces('สถานที่สำหรับทดสอบขอนแก่น',undefined,fetcher);
  assert.equal(places.length,5);
  assert.equal(places[0].name,'สถานที่ 1');
  assert.ok(places.every(p=>p.label.includes('ขอนแก่น')));
});
test('remote suggestions automatically join local matches after the search debounce',async()=>{
  const s=setup();let calls=0;globalThis.fetch=async()=>{calls++;return answer('Library Annex',16.475,102.824);};
  s.nodes['place-search'].value='library';s.nodes['place-search'].listeners.input();
  await new Promise(r=>setTimeout(r,750));
  assert.equal(calls,1);assert.equal(s.nodes['place-results'].children.length,2);
  assert.equal(s.nodes['place-results'].children[0].children[0].textContent,'หอสมุดกลาง มหาวิทยาลัยขอนแก่น');
  assert.equal(s.nodes['place-results'].children[1].children[0].textContent,'Library Annex');
});
test('picker interactions preserve the submitted pin, allow adjustment, and load existing posts',async()=>{
  const s=setup();globalThis.fetch=async()=>answer();
  assert.equal(s.picker.validate(),false);assert.equal(s.nodes.panel.scrolled,true);
  s.nodes['place-search'].value='หอสมุด';s.nodes['place-search'].listeners.input();
  await new Promise(r=>setTimeout(r,700));
  assert.ok(s.nodes['place-results'].children.length>=1);
  s.nodes['place-results'].firstElementChild.onclick();
  assert.deepEqual(s.point,[16.4768,102.82325]);
  assert.equal(s.form.elements.latitude.value,'16.4768000');assert.equal(s.form.elements.longitude.value,'102.8232500');
  assert.equal(s.form.elements.pickupLocationName.value,'หอสมุดกลาง มหาวิทยาลัยขอนแก่น');assert.equal(s.picker.validate(),true);
  s.form.elements.pickupLocationName.value='หน้าประตูฝั่งหอสมุด';
  await s.picker.pick(16.49,102.84); // same callback used by map click and marker drag
  assert.equal(s.form.elements.latitude.value,'16.4900000');assert.equal(s.form.elements.pickupLocationName.value,'หน้าประตูฝั่งหอสมุด');
  await s.nodes['use-location'].onclick();assert.deepEqual(s.point,[16.48,102.83]);assert.equal(s.nodes['use-location'].disabled,false);
  s.picker.load({latitude:16.45,longitude:102.8,pickupLocationName:'จุดรับเดิม'});
  assert.deepEqual(s.point,[16.45,102.8]);assert.equal(s.nodes['place-search'].value,'จุดรับเดิม');
  globalThis.fetch=async()=>{throw Error('offline');};
  s.nodes['manual-latitude'].value='16.46';s.nodes['manual-longitude'].value='102.81';
  s.nodes['apply-coordinates'].onclick();await new Promise(r=>setTimeout(r,0));
  assert.equal(s.form.elements.latitude.value,'16.4600000');assert.deepEqual(s.point,[16.46,102.81]);assert.equal(s.picker.validate(),true);
  s.nodes['manual-latitude'].value='invalid';s.nodes['apply-coordinates'].onclick();assert.equal(s.form.elements.latitude.value,'16.4600000');
});
test('late reverse response cannot overwrite a newly selected place or user edited instructions',async()=>{
  const s=setup();let resolve;globalThis.fetch=()=>new Promise(r=>resolve=r);
  const pending=s.picker.pick(16.4,102.8);
  s.picker.load({latitude:16.5,longitude:102.9,pickupLocationName:'จุดใหม่'});
  resolve(answer('ชื่อเก่า'));await pending;
  assert.match(s.nodes['pin-status'].textContent,/จุดใหม่/);assert.equal(s.form.elements.latitude.value,'16.5000000');
});
test('offline search exposes advanced coordinates without clearing an existing selection',async()=>{
  const s=setup();s.picker.load({latitude:16.5,longitude:102.9,pickupLocationName:'เดิม'});s.form.elements.pickupLocationName.value='เดิม';
  globalThis.fetch=async()=>{throw Error('offline');};
  s.nodes['place-search'].value='ทดสอบ';s.nodes['place-search'].listeners.input();await new Promise(r=>setTimeout(r,300));
  s.nodes['place-search-button'].listeners.click();await new Promise(r=>setTimeout(r,0));
  assert.equal(s.nodes.advanced.open,true);assert.equal(s.picker.validate(),true);
});
test('Google Maps URL map-center coordinates are recognized and marked approximate',()=>{
  const parsed=placeModule.parseGoogleMapsLocationUrl('https://www.google.com/maps/@16.4796266,102.8140024,16z?entry=ttu');
  assert.deepEqual(parsed,{lat:16.4796266,lng:102.8140024,source:'map-center'});
});
test('Google Maps query and place-pin coordinates are recognized as selected locations',()=>{
  assert.deepEqual(placeModule.parseGoogleMapsLocationUrl('https://www.google.com/maps/search/?api=1&query=16.4796266%2C102.8140024'),{lat:16.4796266,lng:102.8140024,source:'query'});
  assert.deepEqual(placeModule.parseGoogleMapsLocationUrl('https://www.google.com/maps/place/Library/data=!3m1!4b1!4m6!3m5!1s0x0!8m2!3d16.4768!4d102.82325'),{lat:16.4768,lng:102.82325,source:'place-pin'});
});
test('unsupported, off-site, and out-of-range map links are rejected',()=>{
  assert.equal(placeModule.parseGoogleMapsLocationUrl('https://example.com/maps/@16.47,102.82,16z'),null);
  assert.equal(placeModule.parseGoogleMapsLocationUrl('https://www.google.com/maps/@91,102.82,16z'),null);
  assert.equal(placeModule.parseGoogleMapsLocationUrl('https://share.google/njsA4kRwpDSkAH5Kq'),null);
  assert.equal(placeModule.parseGoogleMapsLocationUrl('not a URL'),null);
});
test('applying a Google Maps center URL moves the saved pin and warns that it may be approximate',async()=>{
  const s=setup();globalThis.fetch=async()=>answer();
  s.nodes['maps-link-input'].value='https://www.google.com/maps/@16.4796266,102.8140024,16z?entry=ttu';
  s.nodes['apply-maps-link'].listeners.click();
  await new Promise(r=>setTimeout(r,0));
  assert.deepEqual(s.point,[16.4796266,102.8140024]);
  assert.equal(s.form.elements.latitude.value,'16.4796266');
  assert.equal(s.form.elements.longitude.value,'102.8140024');
  assert.match(s.nodes['maps-link-status'].textContent,/จุดกึ่งกลางแผนที่.*ตรวจหมุด/);
});
test('an unreadable or short Google Maps link warns without changing the selected point',()=>{
  const s=setup();s.picker.load({latitude:16.48,longitude:102.82,pickupLocationName:'จุดเดิม'});
  s.nodes['maps-link-input'].value='https://share.google/njsA4kRwpDSkAH5Kq';
  s.nodes['apply-maps-link'].listeners.click();
  assert.deepEqual(s.point,[16.48,102.82]);
  assert.equal(s.form.elements.latitude.value,'16.4800000');
  assert.match(s.nodes['maps-link-status'].textContent,/แบบย่อ.*URL แบบเต็ม/);
});
