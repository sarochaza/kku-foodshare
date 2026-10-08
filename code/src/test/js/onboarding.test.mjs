import test from 'node:test';
import assert from 'node:assert/strict';
import {initHomeGuide} from '../../main/resources/static/js/onboarding.mjs';

// Small DOM boundary double; these tests cover account gating/events, not browser layering.
function page({signed = true, needed = true, replay = false} = {}) {
  const nodes = new Map(), listeners = new Map();
  const classes = new Set();
  const shell = {dataset: {}, hidden: true, open: false,
    showModal() {this.open = true;}, close() {this.open = false;},
    addEventListener(name, handler) {listeners.set(name, handler);}};
  nodes.set('#home-guide', shell);
  for (const [name, content] of [['signed-in', String(signed)], ['onboarding-needed', String(needed)]])
    nodes.set(`meta[name="${name}"]`, {content});
  for (const name of ['title', 'copy', 'progress', 'skip', 'back', 'next'])
    nodes.set('#guide-' + name, {hidden: false, textContent: '', focus() {}});
  const doc = {querySelector: selector => nodes.get(selector) || null,
    querySelectorAll: () => [],
    documentElement: {classList: {add: name => classes.add(name), remove: name => classes.delete(name)}},
    defaultView: {location: {search: replay ? '?guide=1' : ''},
      get localStorage() {throw Error('Legacy browser storage must not decide another account onboarding');}}};
  return {doc, shell, classes, node: name => nodes.get('#guide-' + name),
    needed: () => nodes.get('meta[name="onboarding-needed"]').content,
    cancel: () => listeners.get('cancel')({preventDefault() {}})};
}
const settled = () => new Promise(resolve => setImmediate(resolve));

test('guest and completed accounts stay uninterrupted; every pending account gets its own first guide', () => {
  for (const options of [{signed: false}, {needed: false}]) {
    const p = page(options); initHomeGuide({doc: p.doc});
    assert.equal(p.shell.open, false); assert.equal(p.classes.size, 0);
  }
  for (let account = 0; account < 2; account++) {
    const p = page(); initHomeGuide({doc: p.doc});
    assert.equal(p.shell.open, true); assert.equal(p.node('progress').textContent, '1 / 6');
  }
});

test('next/back only navigate; finishing the existing six steps writes completion once', async () => {
  const p = page(); let writes = 0;
  initHomeGuide({doc: p.doc, complete: () => {writes++;}});
  p.node('next').onclick(); assert.equal(p.node('progress').textContent, '2 / 6');
  p.node('back').onclick(); assert.equal(p.node('progress').textContent, '1 / 6');
  assert.equal(writes, 0); assert.equal(p.needed(), 'true');
  for (let i = 0; i < 6; i++) p.node('next').onclick();
  p.node('skip').onclick(); await settled();
  assert.equal(writes, 1); assert.equal(p.needed(), 'false');
  assert.equal(p.shell.open, false); assert.equal(p.shell.hidden, true); assert.equal(p.classes.size, 0);
});

test('skip and Escape each persist account completion and release the page', async () => {
  for (const action of ['skip', 'cancel']) {
    const p = page(); let writes = 0;
    initHomeGuide({doc: p.doc, complete: () => {writes++;}});
    if (action === 'skip') p.node('skip').onclick(); else p.cancel();
    await settled(); assert.equal(writes, 1); assert.equal(p.shell.open, false); assert.equal(p.needed(), 'false');
  }
});

test('manual replay opens completed accounts and guests without writing guest account state', async () => {
  const completed = page({needed: false, replay: true});
  initHomeGuide({doc: completed.doc}); assert.equal(completed.shell.open, true);
  const guest = page({signed: false, replay: true}); let writes = 0;
  initHomeGuide({doc: guest.doc, complete: () => {writes++;}});
  guest.node('skip').onclick(); await settled(); assert.equal(writes, 0); assert.equal(guest.shell.open, false);
});

test('failed completion leaves the page usable and does not pretend the account is finished', async () => {
  const p = page(); const messages = [];
  initHomeGuide({doc: p.doc, complete: () => Promise.reject(Error('offline')), onError: m => messages.push(m)});
  p.node('skip').onclick(); await settled();
  assert.equal(p.shell.open, false); assert.equal(p.classes.size, 0); assert.equal(p.needed(), 'true');
  assert.equal(messages.length, 1);
});

test('reinitializing the same page does not reset progress or attach another completion handler', async () => {
  const p = page(); let writes = 0;
  const options = {doc: p.doc, complete: () => {writes++;}};
  initHomeGuide(options); p.node('next').onclick(); initHomeGuide(options);
  assert.equal(p.node('progress').textContent, '2 / 6');
  p.node('skip').onclick(); await settled(); assert.equal(writes, 1);
});
