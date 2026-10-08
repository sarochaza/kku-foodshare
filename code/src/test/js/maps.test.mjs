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
test('location locks the best stable high-accuracy fix and reuses it for this session',async()=>{
 let options,cleared=0,watchSuccess;
 const saved=new Map();
 const sandbox={Date,setTimeout,clearTimeout,sessionStorage:{getItem:k=>saved.get(k)||null,setItem:(k,v)=>saved.set(k,v)},navigator:{geolocation:{watchPosition(success,fail,o){watchSuccess=success;options=o;return 17;},clearWatch(id){assert.equal(id,17);cleared++;}}}};
 runInNewContext(source+'\nglobalThis.locate=locate;',sandbox);
 const pending=sandbox.locate();
 const position=(lat,lng,accuracy)=>({coords:{latitude:lat,longitude:lng,accuracy}});
 watchSuccess(position(16.5,102.9,22));watchSuccess(position(16.50001,102.90001,14));
 const p=await pending;
 assert.equal(options.maximumAge,0);assert.equal(options.enableHighAccuracy,true);assert.equal(p.lat,16.50001);assert.equal(p.accuracy,14);assert.equal(cleared,1);
 sandbox.navigator.geolocation.watchPosition=()=>{throw Error('should reuse the locked session fix');};
 assert.deepEqual({...await sandbox.locate()},{...p});
});
test('location keeps one coordinate lock for the browser tab until explicitly refreshed',async()=>{
 const fixed={lat:16.4928,lng:102.8241,accuracy:12,timestamp:1};
 const saved=new Map([['kku-foodshare-location-v1',JSON.stringify(fixed)]]);
 const sandbox={Date,sessionStorage:{getItem:k=>saved.get(k)||null,setItem:(k,v)=>saved.set(k,v)},navigator:{geolocation:{watchPosition(){throw Error('must keep the session origin');}}}};
 runInNewContext(source+'\nglobalThis.locate=locate;',sandbox);
 assert.deepEqual({...await sandbox.locate()},{...fixed});
});
test('explicit location refresh bypasses the locked session fix',async()=>{
 const saved=new Map([['kku-foodshare-location-v1',JSON.stringify({lat:16.4,lng:102.8,accuracy:10,timestamp:Date.now()})]]);let watchSuccess;
 const sandbox={Date,setTimeout,clearTimeout,sessionStorage:{getItem:k=>saved.get(k)||null,setItem:(k,v)=>saved.set(k,v)},navigator:{geolocation:{watchPosition(success,fail){watchSuccess=success;return 3;},clearWatch(){}}}};
 runInNewContext(source+'\nglobalThis.locate=locate;',sandbox);
 const pending=sandbox.locate({fresh:true});watchSuccess({coords:{latitude:16.41,longitude:102.81,accuracy:12}});watchSuccess({coords:{latitude:16.41001,longitude:102.81001,accuracy:9}});
 const p=await pending;assert.equal(p.lat,16.41001);
});
test('a late older location request cannot replace a newer confirmed location',async()=>{
 const saved=new Map(),callbacks=[];let id=0;
 const sandbox={Date,setTimeout,clearTimeout,sessionStorage:{getItem:k=>saved.get(k)||null,setItem:(k,v)=>saved.set(k,v)},navigator:{geolocation:{watchPosition(success){callbacks.push(success);return ++id;},clearWatch(){}}}};
 runInNewContext(source+'\nglobalThis.locate=locate;',sandbox);
 const older=sandbox.locate({fresh:true}),newer=sandbox.locate({fresh:true});
 const position=(lat,lng,accuracy)=>({coords:{latitude:lat,longitude:lng,accuracy}});
 callbacks[1](position(16.6,102.9,20));callbacks[1](position(16.60001,102.90001,10));await newer;
 callbacks[0](position(16.5,102.8,20));callbacks[0](position(16.50001,102.80001,10));await older;
 assert.equal(JSON.parse(saved.get('kku-foodshare-location-v1')).lat,16.60001);
});
test('denied permission and GPS timeout produce actionable errors',async()=>{
 for(const [code,expected] of [[1,/อนุญาต/],[2,/บริการตำแหน่ง/],[3,/ไม่ทันเวลา/]]) {
  const sandbox={Date,setTimeout,clearTimeout,sessionStorage:{getItem:()=>null},navigator:{geolocation:{watchPosition(_ok,fail){fail({code});return 1;},clearWatch(){}}}};
  runInNewContext(source+'\nglobalThis.locate=locate;',sandbox);
  await assert.rejects(sandbox.locate(),expected);
 }
});
test('null cached coordinates cannot silently turn into a false origin at zero zero',async()=>{
 let asked=0;
 const sandbox={Date,sessionStorage:{getItem:()=>JSON.stringify({lat:null,lng:null,timestamp:1})},navigator:{geolocation:{getCurrentPosition(_ok,fail){asked++;fail({code:1});}}}};
 runInNewContext(source+'\nglobalThis.locate=locate;',sandbox);
 await assert.rejects(sandbox.locate(),/อนุญาต/);assert.equal(asked,1);
});
