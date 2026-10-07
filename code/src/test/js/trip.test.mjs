import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import {runInNewContext} from 'node:vm';
import {originState,straightDistance,formatMetres,directionsUrl,setDefaultOrigin} from '../../main/resources/static/js/routes.mjs';
const source=readFileSync(new URL('../../main/resources/static/js/trip.js',import.meta.url),'utf8').replace(/^import .*;\n/gm,'').replace('export function','function');
const tick=()=>new Promise(r=>setTimeout(r,0));
function setup(route=async()=>({distance:950,minutes:13,coordinates:[[16.47,102.82],[16.48,102.83]]}),geolocation=async()=>({lat:16.47,lng:102.82,accuracy:40}),onOriginChange=()=>{}) {
 const selectors=['mode','status','summary','directions','retry','confirm','locate','skip'];
 const nodes=Object.fromEntries(selectors.map(k=>[`[data-trip-${k}]`,{value:k==='mode'?'walking':'',hidden:false,disabled:false,focus(){},textContent:''}]));
 const root={classList:{add(){}},querySelector:s=>nodes[s],scrollIntoView(){this.scrolled=true;},innerHTML:''};
 const events={},map={on:(e,f)=>events[e]=f,fitBounds(){},removeLayer(){},remove(){}};
 const marker={addTo(){return this;},on(){},setLatLng(){}};
 const sandbox={originState,straightDistance,formatMetres,directionsUrl,setDefaultOrigin,fetchRoute:route,locate:geolocation,asDate:v=>new Date(v),createMap:()=>({map,setPin(){}}),L:{marker:()=>marker,divIcon:()=>({}),circle:()=>marker,polyline:()=>({addTo(){return this;},getBounds(){return [];}})},Date,AbortController,setTimeout,clearTimeout};
 runInNewContext(source+'\nglobalThis.mountTrip=mountTrip;',sandbox);
 const widget=sandbox.mountTrip(root,{latitude:16.48,longitude:102.83,availableUntil:'2099-01-01T10:00:00+07:00'},{onOriginChange});
 return {widget,nodes,events,root};
}
test('trip UI removes redundant locate and confirm buttons and accepts the automatic current position',async()=>{
 const changes=[],s=setup(undefined,undefined,(point,mode)=>changes.push({point,mode}));
 assert.doesNotMatch(s.root.innerHTML,/data-trip-locate|data-trip-confirm/);
 await s.widget.refresh();await tick();
 assert.equal(await s.widget.beforeReserve(),true);
 assert.equal(s.nodes['[data-trip-directions]'].hidden,false);
 assert.equal(changes[0].point.lat,16.47);assert.equal(changes[0].point.lng,102.82);assert.equal(changes[0].mode,'driving');
 assert.match(s.nodes['[data-trip-summary]'].textContent,/ขับรถ 950 เมตร.*13 นาที/);
 s.nodes['[data-trip-mode]'].value='walking';
 s.nodes['[data-trip-mode]'].value='driving';s.nodes['[data-trip-mode]'].onchange();await tick();
 assert.match(s.nodes['[data-trip-summary]'].textContent,/ขับรถ/);assert.equal(new URL(s.nodes['[data-trip-directions]'].href).searchParams.get('travelmode'),'driving');
 s.events.click({latlng:{lat:16.46,lng:102.81}});assert.equal(s.nodes['[data-trip-directions]'].hidden,false);assert.equal(await s.widget.beforeReserve(),true);
});
test('no route shows labelled straight-line distance; denied GPS leaves manual map selection available',async()=>{
 const s=setup(async()=>{throw Error('offline');});await s.widget.refresh();await tick();assert.match(s.nodes['[data-trip-summary]'].textContent,/ระยะทางเส้นตรง.*ยังไม่มีเวลาเดินทาง/);
 const denied=setup(undefined,async()=>{throw Error('ไม่อนุญาตตำแหน่ง');});assert.equal(await denied.widget.beforeReserve(),false);assert.match(denied.nodes['[data-trip-status]'].textContent,/ไม่อนุญาต/);assert.equal(denied.nodes['[data-trip-retry]'].hidden,false);denied.events.click({latlng:{lat:16.46,lng:102.81}});assert.equal(await denied.widget.beforeReserve(),true);
});
test('old route response cannot overwrite a newly selected start',async()=>{
 const pending=[];const s=setup(()=>new Promise(r=>pending.push(r)));await s.widget.refresh();s.events.click({latlng:{lat:16.46,lng:102.81}});pending[0]({distance:100,minutes:1,coordinates:[]});await tick();for(const resolve of pending.slice(1))resolve({distance:950,minutes:13,coordinates:[]});await tick();assert.equal(s.nodes['[data-trip-directions]'].hidden,false);assert.equal(new URL(s.nodes['[data-trip-directions]'].href).searchParams.get('origin'),'16.46,102.81');assert.doesNotMatch(s.nodes['[data-trip-summary]'].textContent,/100 เมตร/);
});
test('GPS automatically creates a usable route and directions link without a second confirmation',async()=>{
 const s=setup();await s.widget.refresh();await tick();
 assert.equal(s.nodes['[data-trip-mode]'].value,'driving');assert.match(s.nodes['[data-trip-summary]'].textContent,/ขับรถ.*13 นาที/);assert.doesNotMatch(s.nodes['[data-trip-summary]'].textContent,/ยังไม่ได้ยืนยัน/);assert.equal(s.nodes['[data-trip-directions]'].hidden,false);
});
