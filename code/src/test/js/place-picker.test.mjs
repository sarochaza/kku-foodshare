import test from 'node:test';
import assert from 'node:assert/strict';
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
  const nodes=Object.fromEntries(['place-search','place-search-button','place-results','place-search-status','pin-status','manual-latitude','manual-longitude','apply-coordinates','use-location','advanced','panel'].map(k=>[k,new Element()]));
  globalThis.document={getElementById:id=>nodes[id],querySelector:s=>nodes[s==='.manual-coordinates'?'advanced':'panel'],createElement:()=>new Element(),addEventListener(){}};
  globalThis.matchMedia=()=>({matches:false});
  const form={elements:{latitude:new Element(),longitude:new Element(),pickupLocationName:new Element()}};
  let point;
  const picker=placePicker(form,{setPin:(lat,lng)=>point=[lat,lng]},async()=>({lat:16.48,lng:102.83}));
  return {nodes,form,picker,get point(){return point;}};
}
const answer=(name='หอสมุด',lat=16.47,lng=102.82)=>({ok:true,json:async()=>({features:[{geometry:{coordinates:[lng,lat]},properties:{name,countrycode:'TH',city:'ขอนแก่น'}}]})});
test('local KKU aliases return instant KKU-first suggestions without a network request',()=>{
  for(const query of ['หอสมุด','library','complex']) {
    const places=localPlaces(query);
    assert.ok(places.length>0, query);
    assert.match(places[0].label,/มหาวิทยาลัยขอนแก่น|มข\./);
  }
  assert.deepEqual(localPlaces('ตลาดที่ไม่มีอยู่จริง'),[]);
});
test('remote fallback is explicit, Thailand-limited, KKU-biased, and cached',async()=>{
  let calls=0, url='';
  const fetcher=async requested=>{calls++;url=requested;return answer('สถานที่ขอนแก่น',16.47,102.82);};
  const first=await searchRemotePlaces('สถานที่ทดสอบ',undefined,fetcher);
  const second=await searchRemotePlaces('สถานที่ทดสอบ',undefined,fetcher);
  assert.equal(calls,1);
  assert.match(url,/countrycode=TH/);
  assert.equal(first[0].name,'สถานที่ขอนแก่น');
  assert.deepEqual(second,first);
});
test('picker interactions preserve the submitted pin, allow adjustment, and load existing posts',async()=>{
  const s=setup();globalThis.fetch=async()=>answer();
  assert.equal(s.picker.validate(),false);assert.equal(s.nodes.panel.scrolled,true);
  s.nodes['place-search'].value='หอสมุด';s.nodes['place-search'].listeners.input();
  await new Promise(r=>setTimeout(r,700));
  assert.equal(s.nodes['place-results'].children.length,1);
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
