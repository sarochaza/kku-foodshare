import test from 'node:test';
import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';

const app = await readFile(new URL('../../main/resources/static/js/app.js', import.meta.url), 'utf8');
const home = await readFile(new URL('../../main/resources/templates/home.html', import.meta.url), 'utf8');
const homeController = await readFile(new URL('../../main/java/com/kku/foodshare/controller/web/HomeController.java', import.meta.url), 'utf8');
const security = await readFile(new URL('../../main/java/com/kku/foodshare/config/SecurityConfig.java', import.meta.url), 'utf8');

test('guest welcome explains both roles before the live map', () => {
  assert.ok(home.indexOf('id="guest-start"') < home.indexOf('id="home-discover"'));
  assert.match(home, /หนึ่งบัญชีเป็นได้ทั้งผู้รับและผู้แบ่งปัน/);
  assert.match(home, /href="\/register"/);
  assert.match(home, /href="\/login"/);
});
test('root URL opens the public welcome page without requiring login', () => {
  assert.match(homeController,/@GetMapping\("\/"\)[\s\S]*return "home"/);
  assert.match(security,/requestMatchers\([\s\S]*"\/"[\s\S]*\.permitAll\(\)/);
});
test('homepage and auth pages use compact viewport layout rules', async () => {
  const css = await readFile(new URL('../../main/resources/static/css/app.css', import.meta.url), 'utf8');
  assert.match(css,/body\.welcome-page \.landing-hero[^{]*\{[^}]*min-height: min\(/);
  assert.match(css,/body\.auth-page \.auth-layout[^{]*\{[^}]*min-height: 0 !important/);
  assert.match(css,/@media \(max-height: 800px\)/);
});
test('the previous bottom callout is removed from the About/how-to page', async () => {
  const about = await readFile(new URL('../../main/resources/templates/about.html', import.meta.url), 'utf8');
  assert.doesNotMatch(about,/about-bottom-cta/);
  const aboutCss = await readFile(new URL('../../main/resources/static/css/about.css', import.meta.url), 'utf8');
  assert.doesNotMatch(aboutCss,/about-bottom-cta/);
});

test('feed and detail do not preload protected comment previews', () => {
  assert.doesNotMatch(app, /hydrateFeedComments|mountComments|comments\?size=1/);
});

test('notification comment link opens the dialog at post detail', () => {
  assert.match(app, /location.hash === "#post-comments"\) openPostComments/);
});

test('automatic tour and location do not interrupt guest landing', () => {
  assert.match(app, /if \(!signedIn\(\) && !replay\) return/);
  assert.match(app, /if \(signedIn\(\)\) void useLocation\(\{silent:true\}\)/);
});
