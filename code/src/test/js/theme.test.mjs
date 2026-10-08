import test from 'node:test';
import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';

const fragments = await readFile(new URL('../../main/resources/templates/fragments.html', import.meta.url), 'utf8');
const themeCss = await readFile(new URL('../../main/resources/static/css/theme.css', import.meta.url), 'utf8').catch(() => '');
const themeModule = await import('../../main/resources/static/js/theme.mjs').catch(() => null);

class ThemeSelect {
  constructor() { this.attributes = {}; this.handlers = {}; this.value = ''; }
  addEventListener(type, handler) { this.handlers[type] = handler; }
  setAttribute(name, value) { this.attributes[name] = String(value); }
  change(value) { this.value = value; this.handlers.change?.({target: this}); }
}

function storageWith(value) {
  return {
    value,
    getItem() { return this.value; },
    setItem(_key, next) { this.value = next; }
  };
}

test('theme selector loads and persists all three background themes', () => {
  assert.equal(typeof themeModule?.initThemeSelector, 'function', 'theme module exports initThemeSelector');
  const select = new ThemeSelect();
  const documentRef = {documentElement: {dataset: {}}, querySelectorAll: () => [select]};
  const storage = storageWith('monochrome');

  themeModule.initThemeSelector(documentRef, storage);
  assert.equal(documentRef.documentElement.dataset.theme, 'monochrome');
  assert.equal(select.value, 'monochrome');

  select.change('dark');
  assert.equal(documentRef.documentElement.dataset.theme, 'dark');
  assert.equal(storage.value, 'dark');

  select.change('color');
  assert.equal(documentRef.documentElement.dataset.theme, 'color');
  assert.equal(storage.value, 'color');
});

test('shared navigation exposes all themes and preserves an underline for active navigation', () => {
  assert.match(fragments, /href="\/css\/theme\.css"/);
  assert.match(fragments, /src="\/js\/theme\.mjs"/);
  assert.match(fragments, /data-theme-select/);
  assert.match(fragments, /value="color"[^>]*>ฟ้า–เขียว/);
  assert.match(fragments, /value="monochrome"[^>]*>ขาว–ดำ/);
  assert.match(fragments, /value="dark"[^>]*>พื้นหลังดำ/);
  assert.match(fragments, /href="#i-contrast"/);
  assert.match(themeCss, /html\[data-theme="monochrome"\] \.desktop-nav a\.active,[\s\S]*?background:\s*transparent/);
  assert.match(themeCss, /html\[data-theme="dark"\] \.desktop-nav a\.active,[\s\S]*?background:\s*transparent/);
  assert.match(themeCss, /html\[data-theme="dark"\]\s*\{\s*color-scheme:\s*dark/);
});
