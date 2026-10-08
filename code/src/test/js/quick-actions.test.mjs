import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import {runInNewContext} from 'node:vm';
const source=readFileSync(new URL('../../main/resources/static/js/quick-actions.js',import.meta.url),'utf8').replace(/^import .*;\n/gm,'').replaceAll('export function','function').replaceAll('export async function','async function');
function load(extra={}) {
 const sandbox={Date,crypto:{randomUUID:()=> 'request-key-123456'},Event:class{},Number,escape:String,icon:()=>'',...extra};
 runInNewContext(source+'\nglobalThis.exports={reservationLimit,extensionChoices,stockPreview,wireStepper,mountReservation,mountOwnerStock};',sandbox);
 return sandbox.exports;
}
test('existing reservation can change on a full post without creating a duplicate',()=>{
 const {reservationLimit}=load();
 assert.equal(reservationLimit({availableQuantity:0},{quantity:5}),5);
 assert.equal(reservationLimit({availableQuantity:3},{quantity:2}),5);
  assert.equal(reservationLimit({availableQuantity:20000},null),10000);
  assert.equal(reservationLimit({availableQuantity:9,maxPerPerson:2},null),2);
  assert.equal(reservationLimit({availableQuantity:9,maxPerPerson:4},{quantity:3}),4);
});
test('extension quick choices remain short and predictable',()=>{
 const {extensionChoices}=load();assert.deepEqual([...extensionChoices()],[30,60,120]);
});
test('stock preview protects bookings, distinguishes removing and giving, and corrects offline only',()=>{
 const {stockPreview}=load();const stock={quantity:10,reservedQuantity:4,collectedQuantity:2,offlineQuantity:1,availableQuantity:3};
 assert.equal(stockPreview(stock,'OFFLINE',3).available,0);
 assert.equal(stockPreview(stock,'OFFLINE',3).total,10);
 assert.equal(stockPreview(stock,'OFFLINE',3).offline,4);
 assert.equal(stockPreview(stock,'REMOVE',3).total,7);
 assert.equal(stockPreview(stock,'UNDO_OFFLINE',1).available,4);
 assert.equal(stockPreview(stock,'ADD',2).available,5);
 for (const [action,amount] of [['OFFLINE',4],['REMOVE',4],['UNDO_OFFLINE',2],['ADD',0],['ADD',1.5],['ADD',NaN],['invalid',1]])
  assert.throws(()=>stockPreview(stock,action,amount));
 assert.throws(()=>stockPreview({quantity:1,availableQuantity:1,offlineQuantity:0},'REMOVE',1));
 assert.throws(()=>stockPreview({quantity:10000,availableQuantity:10000,offlineQuantity:0},'ADD',1));
});
test('stepper clamps buttons to valid bounds while leaving invalid typed values for validation',()=>{
 const nodes={ '[data-quantity]':{value:'1',min:'1',max:'3',dispatchEvent(){this.oninput?.();}},'[data-minus]':{},'[data-plus]':{}};
 const root={querySelector:s=>nodes[s]};load({$: (s,r)=>r.querySelector(s)}).wireStepper(root);
 assert.equal(nodes['[data-minus]'].disabled,true);
 nodes['[data-plus]'].onclick();assert.equal(nodes['[data-quantity]'].value,2);
 nodes['[data-plus]'].onclick();assert.equal(nodes['[data-plus]'].disabled,true);
 nodes['[data-quantity]'].value='';nodes['[data-quantity]'].oninput();
 assert.equal(nodes['[data-quantity]'].value,'');
});
const tick=()=>new Promise(resolve=>setTimeout(resolve,0));
function ownerSetup() {
 const pending=[],posts=[],tasks=[];let approve;
 const root={nodes:{},html:'',set innerHTML(value){
  this.html=value;
  if (value.includes('data-stock-form')) this.nodes={
   '[data-stock-action]':{value:'ADD'},'[data-quantity]':{value:'1',min:'1',max:'10000',dispatchEvent(){this.oninput?.();}},
   '[data-minus]':{},'[data-plus]':{},'[data-save]':{},'[data-stock-help]':{},'[data-stock-preview]':{},'[data-stock-form]':{reportValidity:()=>true},
  };
 },get innerHTML(){return this.html;},querySelector(s){return this.nodes[s];}};
 const widget=load({$:(s,r)=>r.querySelector(s),api:(url,options)=>options?.method==='POST'?(posts.push(options.body),Promise.resolve({})):new Promise(resolve=>pending.push(resolve)),
  toast(){},ask:()=>new Promise(resolve=>approve=resolve),busy:(b,fn)=>{const task=fn();tasks.push(task);return task;}}).mountOwnerStock(root,42,async()=>{});
 return {root,widget,pending,posts,tasks,approve:()=>approve(true)};
}
const stock=(version,reserved=0)=>({quantity:5,reservedQuantity:reserved,collectedQuantity:0,offlineQuantity:0,availableQuantity:5-reserved,version,unit:'กล่อง'});
test('owner submission uses the exact snapshot version shown before confirmation',async()=>{
 const s=ownerSetup();s.pending[0](stock(1));await tick();
 s.root.nodes['[data-stock-action]'].value='OFFLINE';s.root.nodes['[data-stock-action]'].onchange();
 s.root.nodes['[data-quantity]'].value='2';s.root.nodes['[data-quantity]'].oninput();
 s.root.nodes['[data-stock-form]'].onsubmit({preventDefault(){},target:s.root.nodes['[data-stock-form]']});
 s.widget.refresh();s.pending[1](stock(2,2));await tick();s.approve();await Promise.all(s.tasks);
 assert.equal(s.posts[0].expectedVersion,1);assert.equal(s.posts[0].amount,2);
});
test('late owner stock response cannot overwrite the newest refresh',async()=>{
 const s=ownerSetup();s.widget.refresh();s.pending[1](stock(2,4));await tick();
 s.pending[0](stock(1));await tick();
 assert.match(s.root.innerHTML,/กันให้ผู้จอง<strong>4/);
});
test('lost new-booking response then cancel and rebook uses a fresh idempotency key',async()=>{
 let active=null,original=null;const keys=[],tasks=[];
 const post={id:42,availableQuantity:5,status:'AVAILABLE',title:'อาหาร',unit:'กล่อง'};
 const root={nodes:{},set innerHTML(value){this.html=value;this.nodes={
  '[data-save]':{disabled:false},'[data-quantity]':{value:active?String(active.quantity):'1',min:'1',max:'5',dispatchEvent(){this.oninput?.();}},
  '[data-minus]':{},'[data-plus]':{},'[data-booking-form]':{reportValidity:()=>true},
  '[data-qr]':{setAttribute(){}},'[data-cancel]':{},
 };},get innerHTML(){return this.html;},querySelector(s){return this.nodes[s];}};
 let id=0,uuid=0;
 const widget=load({crypto:{randomUUID:()=>`request-key-unique-${++uuid}`},$:(s,r)=>r.querySelector(s),toast(){},ask:async()=>true,busy:(b,fn)=>{const task=fn().catch(()=>{});tasks.push(task);return task;},
  api:async(url,options)=>{
   if (options?.method==='POST') {
    keys.push(options.headers['Idempotency-Key']);
    if (keys.length===1) {active={id:++id,status:'RESERVED',quantity:options.body.quantity,pickupCode:'001234'};original={...active};throw Error('response lost');}
    if (keys[1]===keys[0]) return {...original,status:'CANCELLED'};
    active={id:++id,status:'RESERVED',quantity:options.body.quantity,pickupCode:'004321'};return active;
   }
   if (options?.method==='DELETE') {active=null;return null;}
   return url.endsWith('/my-reservation')?active:{...post,availableQuantity:5-(active?.quantity||0)};
  }}).mountReservation(root,post,{beforeReserve:async()=>true});
 await tick();
 const submit=()=>root.nodes['[data-booking-form]'].onsubmit({preventDefault(){},target:root.nodes['[data-booking-form]']});
 submit();await Promise.all(tasks);assert.match(root.innerHTML,/คุณจองไว้แล้ว/);
 root.nodes['[data-cancel]'].onclick();await Promise.all(tasks);
 submit();await Promise.all(tasks);
 assert.notEqual(keys[0],keys[1]);assert.equal(active.id,2);
});
