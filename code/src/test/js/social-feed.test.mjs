import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";

const app = await readFile(new URL("../../main/resources/static/js/app.js", import.meta.url), "utf8");
const ui = await readFile(new URL("../../main/resources/static/js/ui.js", import.meta.url), "utf8");

test("feed uses the multi-image gallery and comment API", () => {
  assert.match(ui, /export function gallery/);
  assert.match(ui, /data-report-post/);
  assert.match(app, /food-posts\/\$\{postId\}\/comments/);
});

test("notifications have real preferences and unread API routes", () => {
  assert.match(app, /notification-preferences/);
  assert.match(app, /data-report-comment/);
});
