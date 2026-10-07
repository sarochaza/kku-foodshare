import test from 'node:test';
import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
import {postPageSize} from '../../main/resources/static/js/feed-options.mjs';
const app = await readFile(new URL('../../main/resources/static/js/app.js', import.meta.url), 'utf8');

function harness(name, search = '') {
  const nodes = new Map(), calls = []; let onPage;
  const $ = selector => {
    if (!nodes.has(selector)) nodes.set(selector, {innerHTML: '', value: '', textContent: '', hidden: true,
      classList: {toggle() {}}, addEventListener() {}, closest: () => null});
    return nodes.get(selector);
  };
  const source = name === 'home'
    ? app.slice(app.indexOf('async function home()'), app.indexOf('function reportPost('))
    : app.slice(app.indexOf('async function explore()'), app.indexOf('async function memberProfile('));
  const location = {search, href: `https://foodshare.local/${name === 'home' ? '' : 'explore'}${search}`};
  const history = {state: {}, replaceState(_state, _unused, url) {location.href = String(url);}};
  const helpers = {$, $$: () => [], api: async url => {calls.push(url); return {items: [], page: Number(new URL(url, location.href).searchParams.get('page')), totalElements: 30, totalPages: 5};},
    location, history, postPageSize, signedIn: () => false, homeGuide() {},
    categories: {}, empty: () => 'empty', errorBox: (_el, error) => {throw error;},
    paginate: (_data, callback) => {onPage = callback;},
    hydrateFeedComments() {}, wireFeedMenus() {},
    createMap: () => ({map: {invalidateSize() {}}}), markers() {},
    locate: () => Promise.reject(Error('No location in test'))};
  const boot = new Function(...Object.keys(helpers), `${source}; return ${name};`)(...Object.values(helpers));
  return {boot, $, calls, page: n => onPage(n), location};
}

test('page size only accepts the offered counts with safe defaults', () => {
  for (const size of [6, 12, 24]) assert.equal(postPageSize(String(size)), size);
  for (const invalid of [null, '', 0, -1, 200, 'oops']) assert.equal(postPageSize(invalid), 12);
  assert.equal(postPageSize('oops', 6), 6);
});

test('home page count and pagination use the real list query while map stays at 200 on page zero', async () => {
  const h = harness('home'); await h.boot();
  assert.equal(h.$('#home-page-size').value, '6');
  await h.page(2);
  assert.match(h.calls.at(-2), /page=2&size=6/);
  assert.match(h.calls.at(-1), /page=0&size=200/);
  await h.$('#home-page-size').onchange({target: {value: '24'}});
  assert.match(h.calls.at(-2), /page=0&size=24/);
  assert.match(h.calls.at(-1), /page=0&size=200/);
  assert.equal(new URL(h.location.href).searchParams.get('size'), '24');
});

test('search count sends the chosen size while keeping filters and resetting page', async () => {
  const h = harness('explore', '?size=6&q=rice&category=FOOD&ownership=others&now=true');
  await h.boot(); assert.equal(h.$('#page-size').value, '6');
  await h.$('#page-size').onchange({target: {value: '24'}});
  const query = new URL(h.calls.at(-1), 'https://foodshare.local').searchParams;
  assert.equal(query.get('size'), '24'); assert.equal(query.get('page'), '0');
  assert.equal(query.get('q'), 'rice'); assert.equal(query.get('category'), 'FOOD');
  assert.equal(query.get('ownership'), 'others'); assert.equal(query.get('now'), 'true');
});
