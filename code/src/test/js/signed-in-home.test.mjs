import test from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';

const base = new URL('../../main/', import.meta.url);
const read = (file) => readFile(new URL(file, base), 'utf8');
const [homeController, dashboardController, fragments, explore, dashboard, feedCss, account, welcomeCss] = await Promise.all([
  read('java/com/kku/foodshare/controller/web/HomeController.java'),
  read('java/com/kku/foodshare/controller/web/DashboardController.java'),
  read('resources/templates/fragments.html'),
  read('resources/templates/explore.html'),
  read('resources/templates/dashboard.html'),
  read('resources/static/css/feed-polish.css'),
  read('resources/templates/account.html'),
  read('resources/static/css/welcome.css')
]);

test('root opens the landing page for signed-in users while the dashboard route remains available', () => {
  assert.match(homeController, /@GetMapping\("\/"\)[\s\S]*return "home"/);
  assert.doesNotMatch(homeController, /redirect:\/explore/);
  assert.match(dashboardController, /return "dashboard"/);
});

test('signed-in navigation omits the home leaf tab and keeps the share button in the five-slot center', () => {
  assert.match(fragments, /th:if="\$\{viewer == null\}"[^>]*>[\s\S]*?data-nav="home"/);
  assert.match(fragments, /mobile-nav[^>]*th:classappend="\$\{viewer != null \? 'signed-in-nav' : ''\}"/);
  assert.match(fragments, /data-nav="editor" class="mobile-post"/);
  assert.match(feedCss, /\.mobile-nav > a\s*\{[^}]*width:\s*calc\(100%\s*\/\s*6\)/);
  assert.match(feedCss, /\.mobile-nav\.signed-in-nav > a\s*\{\s*width:\s*20%/);
});

test('About is in the header and signed-in mobile navigation offers My Posts instead', () => {
  const mobileNav = fragments.match(/<nav class="mobile-nav"[\s\S]*?<\/nav>/)?.[0];
  assert.ok(mobileNav, 'mobile navigation exists');
  assert.match(fragments, /<symbol id="i-info"/);
  assert.match(fragments, /class="header-about-link"[^>]*href="\/about"/);
  assert.match(mobileNav, /th:if="\$\{viewer != null\}"[\s\S]*href="\/account\/posts"[\s\S]*i-grid[\s\S]*โพสต์ของฉัน/);
  assert.doesNotMatch(mobileNav, /data-nav="about"|เกี่ยวกับ/);
});

test('account page removes the three decorative heading lines', () => {
  const visiblePage = account.slice(account.indexOf('<body'));
  assert.doesNotMatch(visiblePage, /WELCOME TO YOUR CORNER|บัญชีของฉัน|ทุกการแบ่งปันของคุณมีความหมาย/);
});

test('mobile welcome layout reduces duplicate calls to action and floating overlays', () => {
  assert.match(welcomeCss, /\.welcome-page \.hero-buttons\s*\.btn[^{}]*\{[^}]*display:\s*none/);
  assert.match(welcomeCss, /\.welcome-page \.floating-note[^{}]*\{[^}]*display:\s*none/);
});

test('ambient background motion honors reduced-motion preference', () => {
  assert.match(feedCss, /@keyframes\s+app-background-drift/);
  assert.match(feedCss, /@media\s*\(prefers-reduced-motion:\s*reduce\)\s*\{\s*body, body\.welcome-page\s*\{\s*animation:\s*none/);
});

test('food discovery and dashboard omit the “มื้อดี ๆ อยู่ใกล้คุณ” promo hero', () => {
  assert.doesNotMatch(explore, /มื้อดี\s*ๆ\s*อยู่ใกล้คุณ/);
  assert.doesNotMatch(dashboard, /มื้อดี\s*ๆ\s*อยู่ใกล้คุณ/);
});
