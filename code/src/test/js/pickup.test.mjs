import test from "node:test";
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { runInNewContext } from "node:vm";
import { payload, parsePass } from "../../main/resources/static/js/pickup.mjs";

test("QR pass binds reservation id to its existing six-digit code", () => {
  assert.equal(payload(42, "001234"), "FS1:42:001234");
  assert.deepEqual(parsePass("FS1:42:001234", 42), { id: 42, code: "001234" });
  assert.deepEqual(parsePass("FS1:42:001234"), { id: 42, code: "001234" });
});

test("pickup pass renders a non-empty QR SVG with a quiet zone", () => {
  const browser = { window: {} };
  runInNewContext(readFileSync(new URL("../../main/resources/static/vendor/pickup-qr.js", import.meta.url), "utf8"), browser);
  const attrs = {};
  const svg = { setAttribute: (name, value) => { attrs[name] = value; }, innerHTML: "" };
  browser.window.FoodShareQR.render(svg, payload(42, "001234"));
  assert.equal(attrs.viewBox, "0 0 29 29");
  assert.equal(attrs.role, "img");
  assert.match(svg.innerHTML, /^<path fill="#183945" d="M/);
  assert.ok((svg.innerHTML.match(/h1v1h-1z/g) || []).length > 100);
});

test("wrong reservation, malformed content and untrusted URLs cannot be collected", () => {
  for (const value of ["FS1:43:001234", "FS1:42:12345", "https://evil.example", "FS1:42:001234:extra"]) {
    assert.equal(parsePass(value, 42), null);
  }
});
