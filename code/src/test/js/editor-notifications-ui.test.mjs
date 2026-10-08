import test from 'node:test';
import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';

const base = new URL('../../main/resources/templates/', import.meta.url);
const [editor, notifications] = await Promise.all([
  readFile(new URL('editor.html', base), 'utf8'),
  readFile(new URL('notifications.html', base), 'utf8')
]);

test('create and edit post screens omit the decorative back link and intro copy', () => {
  const visiblePage = editor.slice(editor.indexOf('<body'));
  assert.doesNotMatch(visiblePage, /←\s*โพสต์ของฉัน|SHARE A MEAL, SHARE A SMILE|แบ่งปันอาหารดี ๆ|บอกรายละเอียดสักนิด แล้วส่งต่อมื้อดี ๆ ให้เพื่อนในชุมชน/);
});

test('notifications screen keeps its controls but removes the decorative heading and description', () => {
  const visiblePage = notifications.slice(notifications.indexOf('<body'));
  assert.doesNotMatch(visiblePage, /A LITTLE UPDATE FOR YOU|<h1>การแจ้งเตือน<\/h1>|ติดตามการจองและเรื่องราวการแบ่งปันของคุณ/);
  assert.match(visiblePage, /id="notification-preferences"/);
  assert.match(visiblePage, /id="notification-list"/);
});
