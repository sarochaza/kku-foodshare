import test from 'node:test';
import assert from 'node:assert/strict';
import {gallery} from '../../main/resources/static/js/ui.js';
import {initImageViewer, imageIndex, safeImageUrl} from '../../main/resources/static/js/image-viewer.mjs';

function node() {
  const events = {};
  return {events, hidden: false, textContent: '', dataset: {},
    addEventListener: (name, callback) => {events[name] = callback;},
    removeAttribute(name) {delete this[name];}};
}
function fixture(total = 5, selected = 3) {
  const doc = node(), dialog = node();
  const elements = Object.fromEntries(['image', 'title', 'count', 'previous', 'next', 'error', 'stage', 'close']
    .map(key => [`[data-viewer-${key}]`, node()]));
  let focuses = 0, prevented = 0;
  const holder = {dataset: {galleryImages: JSON.stringify(Array.from({length: total}, (_, n) => `/uploads/image-${n}.jpg`)), galleryTitle: 'ข้าวผัด'}};
  const link = {dataset: {galleryImage: String(selected)}, isConnected: true,
    closest: () => holder, focus: () => {focuses++;}};
  doc.querySelector = () => dialog;
  dialog.querySelector = selector => elements[selector];
  dialog.showModal = () => {dialog.open = true;};
  dialog.close = () => {dialog.open = false; dialog.events.close();};
  initImageViewer(doc);
  const click = (extra = {}) => doc.events.click({target: {closest: () => link}, button: 0,
    preventDefault() {prevented++;}, ...extra});
  return {doc, dialog, elements, link, click, get focuses() {return focuses;}, get prevented() {return prevented;}};
}

test('collage carries all five images including the hidden +1 image', () => {
  const html = gallery({title: 'อาหาร', images: Array.from({length: 5}, (_, n) => ({url: `/uploads/${n}.jpg`}))});
  assert.match(html, /gallery-4/);
  assert.equal((html.match(/data-gallery-image=/g) || []).length, 4);
  assert.match(html, /gallery-more">\+1/);
  const encoded = html.match(/data-gallery-images="([^"]+)"/)[1].replaceAll('&quot;', '"');
  assert.deepEqual(JSON.parse(encoded), Array.from({length: 5}, (_, n) => `/uploads/${n}.jpg`));
});

test('legacy single image and escaped post title are supported without unsafe URL schemes', () => {
  const html = gallery({title: '"><script>alert(1)</script>', imageUrl: '/uploads/old.jpg'});
  assert.match(html, /gallery-1/);
  assert.match(html, /data-gallery-image="0"/);
  assert.doesNotMatch(html, /<script>/);
  assert.equal(safeImageUrl('javascript:alert(1)'), '');
  assert.equal(safeImageUrl('data:text/html,bad'), '');
  assert.equal(safeImageUrl('/uploads/food.jpg'), '/uploads/food.jpg');
});

test('tap opens the selected image in place and buttons reach hidden images', () => {
  const f = fixture(); f.click();
  assert.equal(f.dialog.open, true); assert.equal(f.prevented, 1);
  assert.equal(f.elements['[data-viewer-image]'].src, '/uploads/image-3.jpg');
  assert.equal(f.elements['[data-viewer-count]'].textContent, 'รูป 4 จาก 5');
  f.elements['[data-viewer-next]'].events.click();
  assert.equal(f.elements['[data-viewer-image]'].src, '/uploads/image-4.jpg');
  f.elements['[data-viewer-next]'].events.click();
  assert.equal(f.elements['[data-viewer-image]'].src, '/uploads/image-0.jpg');
  f.elements['[data-viewer-previous]'].events.click();
  assert.equal(f.elements['[data-viewer-image]'].src, '/uploads/image-4.jpg');
});

test('close button, backdrop and Escape return focus to the original post without navigation', () => {
  const f = fixture();
  f.click(); f.elements['[data-viewer-close]'].events.click();
  assert.equal(f.dialog.open, false); assert.equal(f.focuses, 1);
  assert.equal(f.elements['[data-viewer-image]'].src, undefined);
  f.click(); f.dialog.events.click({target: f.dialog});
  assert.equal(f.dialog.open, false); assert.equal(f.focuses, 2);
  f.click(); let prevented = false;
  f.dialog.events.cancel({preventDefault() {prevented = true;}});
  assert.equal(prevented, true); assert.equal(f.dialog.open, false); assert.equal(f.focuses, 3);
});

test('touch swipe and arrow keys change images; vertical scrolling does not', () => {
  const f = fixture(); f.click(); const stage = f.elements['[data-viewer-stage]'];
  stage.events.touchstart({touches: [{clientX: 200, clientY: 100}]});
  stage.events.touchend({touches: [], changedTouches: [{clientX: 80, clientY: 105}]});
  assert.equal(f.elements['[data-viewer-count]'].textContent, 'รูป 5 จาก 5');
  stage.events.touchstart({touches: [{clientX: 200, clientY: 100}]});
  stage.events.touchend({touches: [], changedTouches: [{clientX: 190, clientY: 300}]});
  assert.equal(f.elements['[data-viewer-count]'].textContent, 'รูป 5 จาก 5');
  f.dialog.events.keydown({key: 'ArrowLeft', preventDefault() {}});
  assert.equal(f.elements['[data-viewer-count]'].textContent, 'รูป 4 จาก 5');
});

test('single-image controls are hidden and a failed load can recover on the next image', () => {
  const f = fixture(1, 0); f.click();
  assert.equal(f.elements['[data-viewer-next]'].hidden, true);
  assert.equal(f.elements['[data-viewer-previous]'].hidden, true);
  f.elements['[data-viewer-image]'].events.error();
  assert.equal(f.elements['[data-viewer-error]'].hidden, false);
  f.elements['[data-viewer-close]'].events.click(); f.click();
  assert.equal(f.elements['[data-viewer-error]'].hidden, true);
  assert.equal(f.elements['[data-viewer-image]'].hidden, false);
  assert.equal(imageIndex(0, -1, 5), 4);
  assert.equal(imageIndex(0, 1, 0), 0);
});

test('Ctrl-click keeps normal new-tab behavior and malformed gallery data is ignored', () => {
  const f = fixture(); f.click({ctrlKey: true});
  assert.equal(f.dialog.open, undefined); assert.equal(f.prevented, 0);
  f.link.closest = () => ({dataset: {galleryImages: 'invalid'}}); f.click();
  assert.equal(f.dialog.open, undefined); assert.equal(f.prevented, 0);
});
