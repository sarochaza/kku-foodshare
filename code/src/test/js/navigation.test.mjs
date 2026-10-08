import test from 'node:test';
import assert from 'node:assert/strict';
import {navigationKey, applyNavigation, initNavigation} from '../../main/resources/static/js/navigation.mjs';

function link(key, mobile = false) {
  const attributes = {}, classes = new Set(['active']);
  return {dataset: {nav: key}, attributes, classes,
    classList: {toggle(name, enabled) { if (enabled) classes.add(name); else classes.delete(name); }},
    closest: () => mobile,
    setAttribute: (name, value) => {attributes[name] = value;},
    removeAttribute: name => {delete attributes[name];}};
}

test('navigation distinguishes map query from list and home section', () => {
  assert.equal(navigationKey('/'), 'home');
  assert.equal(navigationKey('/', '', '#how'), 'how');
  assert.equal(navigationKey('/explore', '?view=map&q=rice'), 'map');
  assert.equal(navigationKey('/explore', '?q=rice'), 'explore');
  assert.equal(navigationKey('/home'), 'explore');
  assert.equal(navigationKey('/account/posts'), 'account');
  assert.equal(navigationKey('/posts/12/edit', '', '', 'editor'), 'editor');
  assert.equal(navigationKey('/login'), '');
});

test('changing tabs clears the previous blue state and aria-current', () => {
  const links = ['home', 'explore', 'map', 'how'].map(key => link(key));
  applyNavigation(links, 'map');
  assert.deepEqual(links.map(item => item.classes.has('active')), [false, false, true, false]);
  assert.equal(links[2].attributes['aria-current'], 'page');
  applyNavigation(links, 'how');
  assert.deepEqual(links.map(item => item.classes.has('active')), [false, false, false, true]);
  assert.equal(links[2].attributes['aria-current'], undefined);
  assert.equal(links[3].attributes['aria-current'], 'location');
});

test('mobile search includes map while home includes the how section', () => {
  const home = link('home', true), search = link('explore', true), desktopSearch = link('explore');
  applyNavigation([home, search, desktopSearch], 'map');
  assert.equal(search.classes.has('active'), true);
  assert.equal(desktopSearch.classes.has('active'), false);
  applyNavigation([home, search], 'how');
  assert.equal(home.classes.has('active'), true);
  assert.equal(search.classes.has('active'), false);
});

test('hash, history and view switches update navigation without refetching posts', () => {
  const links = ['home', 'explore', 'map', 'how'].map(key => link(key));
  const events = {};
  const win = {location: {pathname: '/', search: '', hash: ''}, addEventListener: (name, fn) => {events[name] = fn;}};
  initNavigation({querySelectorAll: () => links, body: {dataset: {page: 'home'}}}, win);
  assert.equal(links[0].classes.has('active'), true);
  win.location.hash = '#how'; events.hashchange();
  assert.equal(links[3].classes.has('active'), true);
  win.location = {pathname: '/explore', search: '?view=map', hash: ''}; events.popstate();
  assert.equal(links[2].classes.has('active'), true);
  events['foodshare:viewchange']({detail: {map: false}});
  assert.equal(links[1].classes.has('active'), true);
  assert.equal(links[2].classes.has('active'), false);
});
