import test from 'node:test';
import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';

const app = await readFile(new URL('../../main/resources/static/js/app.js', import.meta.url), 'utf8');
const home = await readFile(new URL('../../main/resources/templates/home.html', import.meta.url), 'utf8');

test('guest welcome explains both roles before the live map', () => {
  assert.ok(home.indexOf('id="guest-start"') < home.indexOf('id="home-discover"'));
  assert.match(home, /หนึ่งบัญชีเป็นได้ทั้งผู้รับและผู้แบ่งปัน/);
  assert.match(home, /href="\/register"/);
  assert.match(home, /href="\/login"/);
});

test('guest feed does not call protected comment preview APIs', async () => {
  const code = app.slice(app.indexOf('async function hydrateFeedComments('), app.indexOf('function wireFeedMenus('));
  let calls = 0;
  const hydrate = new Function('$$', 'signedIn', 'api', `${code}; return hydrateFeedComments;`)(
    () => [{dataset: {commentPreview: '1'}}], () => false, () => {calls++; throw Error('401');});
  await hydrate({});
  assert.equal(calls, 0);
});

test('guest post detail invites login without fetching protected comments', async () => {
  const code = app.slice(app.indexOf('async function mountComments('), app.indexOf('async function editor('));
  const element = {innerHTML: ''}; let calls = 0;
  const mount = new Function('$', 'signedIn', 'api', `${code}; return mountComments;`)(
    () => element, () => false, () => {calls++; throw Error('401');});
  await mount(1);
  assert.equal(calls, 0);
  assert.match(element.innerHTML, /href="\/login"/);
});

test('automatic tour and location do not interrupt guest landing', () => {
  assert.match(app, /if \(!signedIn\(\) && !replay\) return/);
  assert.match(app, /if \(signedIn\(\)\) void useLocation\(\{silent:true\}\)/);
});
