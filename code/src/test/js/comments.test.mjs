import test from 'node:test';
import assert from 'node:assert/strict';
import {groupComments, renderComments, initComments} from '../../main/resources/static/js/comments.mjs';
import {feedCard} from '../../main/resources/static/js/ui.js';

const comment = (id, extra = {}) => ({id, authorId: 2, authorName: 'ผู้รับ', body: 'ยังมีอาหารไหม',
  createdAt: '2026-10-07T10:00:00', canDelete: false, ...extra});
const post = {id: 10, ownerId: 1, ownerName: 'ผู้แบ่งปัน', title: 'อาหาร', commentCount: 3,
  description: 'รายละเอียด', images: [{url: '/uploads/food.jpg'}], availableFrom: '2026-10-07T10:00:00',
  availableUntil: '2026-10-07T12:00:00', category: 'FOOD', availableQuantity: 2, unit: 'กล่อง'};

class Node {
  listeners = new Map(); dataset = {}; hidden = false; disabled = false;
  value = ''; innerHTML = ''; textContent = ''; open = false;
  addEventListener(name, handler) {this.listeners.set(name, handler);}
  async fire(name, event = {}) {await this.listeners.get(name)?.({preventDefault() {}, target: this, ...event});}
  focus() {this.focused = true;}
  showModal() {this.open = true;}
  close() {this.open = false; void this.fire('close');}
  querySelector() {return null;}
}
function harness({member = true, request = async () => ({items: [], page: 0, totalPages: 0, totalElements: 0})} = {}) {
  const nodes = new Map(), dialog = new Node(), doc = new Node(), calls = [], notices = [];
  const node = selector => {if (!nodes.has(selector)) nodes.set(selector, new Node()); return nodes.get(selector);};
  dialog.querySelector = node;
  doc.querySelector = () => dialog; doc.querySelectorAll = () => [];
  const manager = initComments({doc, signedIn: () => member, ask: async () => true,
    toast: (message, error) => notices.push({message, error}),
    api: async (url, options) => {calls.push({url, options}); return request(url, options);}});
  return {node, dialog, doc, calls, notices, manager,
    click: (selector, dataset) => node('[data-comments-list]').fire('click', {target: {closest: s => s === selector ? {dataset} : null}})};
}

test('feed shows only count and click-to-read, without an inline preview', () => {
  const html = feedCard(post);
  assert.match(html, /data-open-comments="10"/);
  assert.match(html, /data-comment-count="10">3/);
  assert.doesNotMatch(html, /data-comment-preview|comment-bubble/);
  assert.ok(html.indexOf('data-gallery-images') < html.indexOf('data-open-comments'));
});

test('grouping retains one visual level, including replies to other replies', () => {
  const grouped = groupComments([comment(1), comment(2, {parentCommentId: 1}), comment(3, {parentCommentId: 1, replyToCommentId: 2})]);
  assert.equal(grouped.length, 1);
  assert.deepEqual(grouped[0].replies.map(c => c.id), [2, 3]);
  const html = renderComments([comment(1), comment(2, {parentCommentId: 1, replyToAuthorName: 'เจ้าของ'})], 1);
  assert.equal((html.match(/class="comment-replies"/g) || []).length, 1);
  assert.match(html, /ดูการตอบกลับ 1 รายการ/);
  assert.match(html, /comment-mention">เจ้าของ/);
});

test('missing or deleted roots preserve replies without exposing deleted text', () => {
  const html = renderComments([comment(2, {parentCommentId: 1, parentDeleted: true})], 1);
  assert.match(html, /ความคิดเห็นต้นทางถูกลบแล้ว/);
  assert.match(html, /ยังมีอาหารไหม/);
});

test('comment body, names and reply mentions are escaped and deletion follows server permission', () => {
  const html = renderComments([comment(1, {body: '<img onerror=alert(1)>', authorName: '<script>', replyToAuthorName: '<svg>', canDelete: true})], 1);
  assert.doesNotMatch(html, /<script>|<svg>|<img onerror/);
  assert.match(html, /&lt;img onerror/);
  assert.match(html, /data-delete-comment="1"/);
  assert.doesNotMatch(renderComments([comment(2)], 1), /data-delete-comment/);
});

test('comments fetch only on opening; guests get a login invite without protected requests', async () => {
  const member = harness(); assert.equal(member.calls.length, 0);
  await member.manager.open(post);
  assert.equal(member.node('[data-comments-title]').textContent, post.title);
  assert.doesNotMatch(member.node('[data-comments-title]').textContent, /ความคิดเห็น|คอมเมนต์/);
  assert.equal(member.calls.length, 1);
  assert.match(member.calls[0].url, /food-posts\/10\/comments\?page=0/);
  const guest = harness({member: false}); await guest.manager.open(post);
  assert.equal(guest.calls.length, 0);
  assert.equal(guest.node('[data-comments-form]').hidden, true);
  assert.equal(guest.node('[data-comments-login]').hidden, false);
  assert.match(guest.node('[data-comments-post]').innerHTML, /data-gallery-images/);
});

test('reply sends the selected comment ID and expands its root thread', async () => {
  const root = comment(1), child = comment(2, {parentCommentId: 1});
  const h = harness({request: async (_url, opts) => opts?.method === 'POST'
    ? comment(3, {parentCommentId: 1, replyToCommentId: 2})
    : {items: [root, child], page: 0, totalPages: 1, totalElements: 2}});
  await h.manager.open(post);
  await h.click('[data-reply-comment]', {replyComment: '2'});
  assert.equal(h.node('[data-comment-reply]').hidden, false);
  h.node('[data-comment-input]').value = ' ขอบคุณ ';
  await h.node('[data-comments-form]').fire('submit');
  assert.deepEqual(h.calls.at(-1).options.body, {body: 'ขอบคุณ', parentCommentId: 2});
  assert.match(h.node('[data-comments-list]').innerHTML, /data-thread="1" open/);
  assert.equal(h.node('[data-comments-count]').textContent, '3 ข้อความ');
  assert.equal(h.node('[data-comment-reply]').hidden, true);
});

test('root comments omit parent, cancellation resets reply, failed submissions preserve draft', async () => {
  const h = harness({request: async (_url, opts) => {
    if (opts?.method === 'POST') throw Error('ส่งไม่ได้');
    return {items: [comment(1)], page: 0, totalPages: 1, totalElements: 1};
  }});
  await h.manager.open(post);
  await h.click('[data-reply-comment]', {replyComment: '1'});
  await h.node('[data-comment-cancel-reply]').fire('click');
  h.node('[data-comment-input]').value = 'ลองส่ง';
  await h.node('[data-comments-form]').fire('submit');
  assert.deepEqual(h.calls.at(-1).options.body, {body: 'ลองส่ง'});
  assert.equal(h.node('[data-comment-input]').value, 'ลองส่ง');
  assert.equal(h.node('[data-comment-send]').disabled, false);
  assert.equal(h.notices[0].error, true);
});

test('load more merges duplicate IDs, keeps expanded replies and real totals', async () => {
  const h = harness({request: async url => url.endsWith('page=0')
    ? {items: [comment(1), comment(2, {parentCommentId: 1})], page: 0, totalPages: 2, totalElements: 3}
    : {items: [comment(2, {parentCommentId: 1}), comment(3)], page: 1, totalPages: 2, totalElements: 3}});
  await h.manager.open(post);
  await h.node('[data-comments-list]').fire('toggle', {target: {dataset: {thread: '1'}, open: true}});
  await h.node('[data-comments-more]').fire('click');
  // Event dispatch starts the async request; settle its continuation.
  await new Promise(resolve => setImmediate(resolve));
  assert.equal((h.node('[data-comments-list]').innerHTML.match(/data-comment-id="2"/g) || []).length, 1);
  assert.match(h.node('[data-comments-list]').innerHTML, /data-thread="1" open/);
  assert.equal(h.node('[data-comments-count]').textContent, '3 ข้อความ');
  assert.equal(h.node('[data-comments-more]').hidden, true);
});

test('closing invalidates late responses and restores opener focus', async () => {
  let resolve;
  const h = harness({request: () => new Promise(done => {resolve = done;})});
  const opener = new Node(); opener.isConnected = true;
  const pending = h.manager.open(post, opener);
  await h.node('[data-comments-close]').fire('click');
  resolve({items: [comment(1)], page: 0, totalPages: 1, totalElements: 1}); await pending;
  assert.equal(h.dialog.open, false); assert.equal(opener.focused, true);
  assert.equal(h.node('[data-comments-list]').innerHTML, '');
});

test('double-submit does not create duplicate comments', async () => {
  let finish;
  const h = harness({request: async (_url, opts) => opts?.method === 'POST'
    ? new Promise(resolve => {finish = resolve;})
    : {items: [], page: 0, totalPages: 0, totalElements: 0}});
  await h.manager.open(post); h.node('[data-comment-input]').value = 'จองได้ไหม';
  const first = h.node('[data-comments-form]').fire('submit');
  await h.node('[data-comments-form]').fire('submit');
  assert.equal(h.calls.filter(c => c.options?.method === 'POST').length, 1);
  finish(comment(1)); await first;
});

test('delete reloads real comments and leaves replies attached to a deleted-root placeholder', async () => {
  let deleted = false;
  const h = harness({request: async (_url, opts) => {
    if (opts?.method === 'DELETE') {deleted = true; return null;}
    return {items: deleted ? [comment(2, {parentCommentId: 1, parentDeleted: true})]
      : [comment(1, {canDelete: true}), comment(2, {parentCommentId: 1})],
      page: 0, totalPages: 1, totalElements: deleted ? 1 : 2};
  }});
  await h.manager.open(post);
  await h.click('[data-delete-comment]', {deleteComment: '1'});
  assert.ok(h.calls.some(c => c.url === '/api/v1/comments/1' && c.options.method === 'DELETE'));
  assert.equal(h.node('[data-comments-count]').textContent, '1 ข้อความ');
  assert.match(h.node('[data-comments-list]').innerHTML, /ความคิดเห็นต้นทางถูกลบแล้ว/);
  assert.match(h.node('[data-comments-list]').innerHTML, /data-comment-id="2"/);
});

test('report uses the existing moderation endpoint with correct post and comment', async () => {
  const h = harness(); await h.manager.open(post);
  await h.click('[data-report-comment]', {reportComment: '7'});
  assert.equal(h.calls.at(-1).url, '/api/v1/reports');
  assert.equal(h.calls.at(-1).options.body.postId, 10);
  assert.equal(h.calls.at(-1).options.body.commentId, 7);
  assert.equal(h.node('[data-comment-feedback]').hidden, false);
});

test('switching posts ignores the old request and blank or overlong input makes no mutation', async () => {
  let finishOld;
  const h = harness({request: async url => url.includes('food-posts/10/')
    ? new Promise(resolve => {finishOld = resolve;})
    : {items: [comment(8)], page: 0, totalPages: 1, totalElements: 1}});
  const old = h.manager.open(post);
  await h.manager.open({...post, id: 11});
  finishOld({items: [comment(9)], page: 0, totalPages: 1, totalElements: 1}); await old;
  assert.match(h.node('[data-comments-list]').innerHTML, /data-comment-id="8"/);
  assert.doesNotMatch(h.node('[data-comments-list]').innerHTML, /data-comment-id="9"/);
  for (const text of ['   ', 'a'.repeat(801)]) {
    h.node('[data-comment-input]').value = text;
    await h.node('[data-comments-form]').fire('submit');
  }
  assert.equal(h.calls.filter(c => c.options?.method === 'POST').length, 0);
});
