import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import {runInNewContext} from 'node:vm';
import {originState,straightDistance,formatMetres,directionsUrl} from '../../main/resources/static/js/routes.mjs';
const source=readFileSync(new URL('../../main/resources/static/js/trip.js',import.meta.url),'utf8').replace(/^import .*;\n/gm,'').replace('export function','function');
const tick=()=>new Promise(r=>setTimeout(r,0));
function setup(route=async()=>({distance:950,minutes:13,coordinates:[[16.47,102.82],[16.48,102.83]]}),geolocation=async()=>({lat:16.47,lng:102.82,accuracy:40})) {
 const selectors=['mode','status','summary','directions','confirm','locate','skip'];
 const nodes=Object.fromEntries(selectors.map(k=>[`[data-trip-${k}]`,{value:k==='mode'?'walking':'',hidden:false,disabled:false,focus(){},textContent:''}]));
 const root={classList:{add(){}},querySelector:s=>nodes[s],scrollIntoView(){this.scrolled=true;}};
 const events={},map={on:(e,f)=>events[e]=f,fitBounds(){},removeLayer(){},remove(){}};
 const marker={addTo(){return this;},on(){},setLatLng(){}};
 const sandbox={originState,straightDistance,formatMetres,directionsUrl,fetchRoute:route,locate:geolocation,asDate:v=>new Date(v),createMap:()=>({map,setPin(){}}),L:{marker:()=>marker,divIcon:()=>({}),circle:()=>marker,polyline:()=>({addTo(){return this;},getBounds(){return [];}})},Date,AbortController,setTimeout,clearTimeout};
 runInNewContext(source+'\nglobalThis.mountTrip=mountTrip;',sandbox);
 const widget=sandbox.mountTrip(root,{latitude:16.48,longitude:102.83,availableUntil:'2099-01-01T10:00:00+07:00'});
 return {widget,nodes,events,root};
}
test('booking checks position, waits for confirmation, renders route, and changes travel mode',async()=>{
 const s=setup();s.nodes['[data-trip-mode]'].value='walking';assert.equal(await s.widget.beforeReserve(),false);assert.equal(s.root.scrolled,true);
 s.nodes['[data-trip-confirm]'].onclick();await tick();
 assert.match(s.nodes['[data-trip-summary]'].textContent,/เดินเท้า 950 เมตร.*13 นาที/);assert.equal(await s.widget.beforeReserve(),true);
 s.nodes['[data-trip-mode]'].value='driving';s.nodes['[data-trip-mode]'].onchange();await tick();
 assert.match(s.nodes['[data-trip-summary]'].textContent,/ขับรถ/);assert.equal(new URL(s.nodes['[data-trip-directions]'].href).searchParams.get('travelmode'),'driving');
 s.events.click({latlng:{lat:16.46,lng:102.81}});assert.equal(s.nodes['[data-trip-directions]'].hidden,true);assert.equal(await s.widget.beforeReserve(),false);
});
test('no route shows labelled straight-line distance without invented time; denied GPS can be skipped',async()=>{
 const s=setup(async()=>{throw Error('offline');});await s.widget.refresh();s.nodes['[data-trip-confirm]'].onclick();await tick();assert.match(s.nodes['[data-trip-summary]'].textContent,/ระยะทางเส้นตรง.*ยังไม่มีเวลาเดินทาง/);
 const denied=setup(undefined,async()=>{throw Error('ไม่อนุญาตตำแหน่ง');});assert.equal(await denied.widget.beforeReserve(),false);assert.match(denied.nodes['[data-trip-status]'].textContent,/ไม่อนุญาต/);denied.nodes['[data-trip-skip]'].onclick();assert.equal(await denied.widget.beforeReserve(),true);
});
test('old route response cannot overwrite a newly selected start',async()=>{
 const pending=[];const s=setup(()=>new Promise(r=>pending.push(r)));await s.widget.refresh();s.nodes['[data-trip-confirm]'].onclick();s.events.click({latlng:{lat:16.46,lng:102.81}});pending[0]({distance:100,minutes:1,coordinates:[]});await tick();for(const resolve of pending.slice(1))resolve({distance:950,minutes:13,coordinates:[]});await tick();assert.equal(s.nodes['[data-trip-directions]'].hidden,true);assert.doesNotMatch(s.nodes['[data-trip-summary]'].textContent,/100 เมตร/);
});
test('GPS refresh automatically previews driving distance before origin confirmation',async()=>{
 const s=setup();await s.widget.refresh();await tick();
 assert.equal(s.nodes['[data-trip-mode]'].value,'driving');assert.match(s.nodes['[data-trip-summary]'].textContent,/ขับรถ.*13 นาที/);assert.match(s.nodes['[data-trip-summary]'].textContent,/ยังไม่ได้ยืนยัน/);assert.equal(s.nodes['[data-trip-directions]'].hidden,true);
});
