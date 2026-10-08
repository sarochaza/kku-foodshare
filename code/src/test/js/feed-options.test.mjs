import test from 'node:test';
import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
import {sharingMapUrl, mapFeedRequest, focusFoodMap, hasLocation} from '../../main/resources/static/js/map-discovery.mjs';
const app = await readFile(new URL('../../main/resources/static/js/app.js', import.meta.url), 'utf8');

function harness(name, search = '') {
  const nodes = new Map(), calls = []; let onPage;
  const $ = selector => {
    if (!nodes.has(selector)) nodes.set(selector, {innerHTML: '', value: '', textContent: '', hidden: true,
      classList: {toggle() {}}, setAttribute() {}, addEventListener() {}, closest: () => null});
    return nodes.get(selector);
  };
  const source = name === 'home'
    ? app.slice(app.indexOf('async function home()'), app.indexOf('function reportPost('))
    : app.slice(app.indexOf('async function explore()'), app.indexOf('async function memberProfile('));
  const location = {search, href: `https://foodshare.local/${name === 'home' ? '' : 'explore'}${search}`};
  const history = {state: {}, replaceState(_state, _unused, url) {location.href = String(url);}};
  const helpers = {$, $$: () => [], api: async url => {calls.push(url); return {items: [], page: Number(new URL(url, location.href).searchParams.get('page')), totalElements: 30, totalPages: 5};},
    location, history, sharingMapUrl, mapFeedRequest, focusFoodMap, hasLocation, signedIn: () => false, homeGuide() {},
    categories: {}, empty: () => 'empty', errorBox: (_el, error) => {throw error;},
    paginate: (_data, callback) => {onPage = callback;},
    hydrateFeedComments() {}, wireFeedMenus() {},
    createMap: () => ({map: {invalidateSize() {}}}), markers() {},
    locate: () => Promise.reject(Error('No location in test'))};
  const boot = new Function(...Object.keys(helpers), `${source}; return ${name};`)(...Object.values(helpers));
  return {boot, $, calls, page: n => onPage(n), location};
}

test('page size controls are removed from home and search', async () => {
  for (const name of ['home', 'explore', 'dashboard']) {
    const html = await readFile(new URL(`../../main/resources/templates/${name}.html`, import.meta.url), 'utf8');
    assert.doesNotMatch(html, /แสดงต่อหน้า|id="(?:home-)?page-size"/);
  }
});

test('home pagination remains at six cards while the map stays at 200 on page zero', async () => {
  const h = harness('home'); await h.boot();
  await h.page(2);
  assert.match(h.calls.at(-2), /page=2&size=6/);
  assert.match(h.calls.at(-1), /page=0&size=200/);
});

test('search keeps category, ownership and availability filters with fixed pagination', async () => {
  const h = harness('explore', '?size=6&q=rice&category=FOOD&ownership=others&now=true');
  await h.boot(); await h.page(2);
  const query = new URL(h.calls.at(-1), 'https://foodshare.local').searchParams;
  assert.equal(query.get('size'), '12'); assert.equal(query.get('page'), '2');
  assert.equal(query.get('q'), 'rice'); assert.equal(query.get('category'), 'FOOD');
  assert.equal(query.get('ownership'), 'others'); assert.equal(query.get('now'), 'true');
});
