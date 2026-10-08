import test from 'node:test';
import assert from 'node:assert/strict';
import { initProfileMenu } from '../../main/resources/static/js/profile-menu.mjs';

class Element {
  constructor() { this.hidden=true; this.attrs={}; this.listeners={}; this.focused=false; }
  addEventListener(type, listener) { this.listeners[type]=listener; }
  setAttribute(name, value) { this.attrs[name]=String(value); }
  contains(target) { return target===this; }
  focus() { this.focused=true; }
}

test('profile menu toggles, closes with Escape, and closes on outside click',()=>{
  const button=new Element(), menu=new Element(), documentListeners={};
  globalThis.document={
    getElementById:id=>id==='profile-menu-button'?button:id==='profile-menu'?menu:null,
    addEventListener:(type,listener)=>documentListeners[type]=listener,
  };
  initProfileMenu();
  button.listeners.click();
  assert.equal(menu.hidden,false);assert.equal(button.attrs['aria-expanded'],'true');
  documentListeners.keydown({key:'Escape'});
  assert.equal(menu.hidden,true);assert.equal(button.attrs['aria-expanded'],'false');assert.equal(button.focused,true);
  button.listeners.click();
  documentListeners.click({target:{}});
  assert.equal(menu.hidden,true);
});
