import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import {runInNewContext} from 'node:vm';
const source=readFileSync(new URL('../../main/resources/static/js/maps.js',import.meta.url),'utf8').replace(/^import .*;\n/,'').replaceAll('export function','function');
test('Leaflet click and drag callbacks report the chosen coordinates',()=>{
 const events={},markerEvents={};let point,selected;
 const map={setView(){return this;},on:(e,f)=>events[e]=f,invalidateSize(){}};
 const marker={addTo(){return this;},on:(e,f)=>markerEvents[e]=f,setLatLng:p=>point=p,getLatLng:()=>({lat:16.49,lng:102.85})};
 const sandbox={window:{L:{}},L:{map:()=>map,tileLayer:()=>({addTo(){return this;},on(){}}),marker:p=>{point=p;return marker;}},$:()=>({content:'tiles'}),document:{createElement:()=>({setAttribute(){},querySelector:()=>({})}),getElementById:()=>({after(){}})},setTimeout(){}};
 runInNewContext(source+'\nglobalThis.createMap=createMap;',sandbox);
 const picker=sandbox.createMap('editor-map',(lat,lng)=>selected=[lat,lng]);
 events.click({latlng:{lat:16.47,lng:102.82}});assert.deepEqual(selected,[16.47,102.82]);assert.deepEqual(Array.from(point),selected);
 markerEvents.dragend();assert.deepEqual(selected,[16.49,102.85]);picker.setPin(16.5,102.9);assert.deepEqual(Array.from(point),[16.5,102.9]);
});
test('geolocation explicitly requests a fresh high accuracy position',async()=>{
 let options;
 const sandbox={navigator:{geolocation:{getCurrentPosition(success,fail,o){options=o;success({coords:{latitude:16.5,longitude:102.9,accuracy:12}});}}}};
 runInNewContext(source+'\nglobalThis.locate=locate;',sandbox);
 const p=await sandbox.locate();assert.equal(options.maximumAge,0);assert.equal(options.enableHighAccuracy,true);assert.equal(p.lat,16.5);assert.equal(p.accuracy,12);
});
