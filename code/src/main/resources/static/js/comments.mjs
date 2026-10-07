import { api, escape, dateTime, gallery, signedIn, ask, toast } from './ui.js';

// Replies to replies share the original thread, so nesting never consumes mobile width.
export function groupComments(items) {
  const groups = new Map();
  for (const c of items) {
    const id = c.parentCommentId || c.id;
    if (!groups.has(id)) groups.set(id, {id, root: null, replies: []});
    const group = groups.get(id);
    if (c.parentCommentId) group.replies.push(c); else group.root = c;
  }
  return [...groups.values()];
}

export function renderComments(items, ownerId, expanded = new Set()) {
  const row = c => `<article class="comment-row" data-comment-id="${c.id}">
    <a class="comment-avatar" href="/members/${c.authorId}"><img src="/api/v1/members/${c.authorId}/photo" alt="${escape(c.authorName)}" loading="lazy"></a>
    <div class="comment-content"><div class="comment-bubble"><a href="/members/${c.authorId}" class="comment-author">${escape(c.authorName)}</a>${c.authorId === ownerId ? '<small class="comment-owner-label">ผู้แบ่งปัน</small>' : ''}
    <p>${c.replyToAuthorName ? `<span class="comment-mention">${escape(c.replyToAuthorName)}</span> ` : ''}${escape(c.body)}</p></div>
    <div class="comment-row-actions"><time>${escape(dateTime(c.createdAt))}</time><button type="button" data-reply-comment="${c.id}">ตอบกลับ</button>
    <details class="comment-menu"><summary aria-label="ตัวเลือกความคิดเห็น">…</summary><div>${c.canDelete ? `<button type="button" data-delete-comment="${c.id}">ลบความคิดเห็น</button>` : ''}<button type="button" data-report-comment="${c.id}">รายงานความคิดเห็น</button></div></details></div></div></article>`;
  if (!items.length) return '<p class="comments-empty">ยังไม่มีความคิดเห็น ลองถามรายละเอียดอาหารได้เลย</p>';
  return groupComments(items).map(group => {
    const deleted = !group.root && group.replies.some(c => c.parentDeleted);
    const root = group.root ? row(group.root) : `<p class="comment-placeholder">${deleted ? 'ความคิดเห็นต้นทางถูกลบแล้ว' : 'การตอบกลับความคิดเห็นก่อนหน้า'}</p>`;
    return `<section class="comment-thread">${root}${group.replies.length ? `<details class="comment-replies" data-thread="${group.id}" ${expanded.has(group.id) ? 'open' : ''}><summary>ดูการตอบกลับ ${group.replies.length} รายการ</summary><div>${group.replies.map(row).join('')}</div></details>` : ''}</section>`;
  }).join('');
}

let manager;
export function openPostComments(post, opener) { return manager?.open(post, opener); }

export function initComments(options = {}) {
  const doc = options.doc || document;
  const request = options.api || api, isSignedIn = options.signedIn || signedIn;
  const confirm = options.ask || ask, toastMessage = options.toast || toast;
  const dialog = doc.querySelector('#comments-dialog');
  if (!dialog || dialog.dataset.initialized) return;
  dialog.dataset.initialized = 'true';
  const find = selector => dialog.querySelector(selector);
  const list = find('[data-comments-list]'), more = find('[data-comments-more]');
  const status = find('[data-comments-status]'), form = find('[data-comments-form]');
  const input = find('[data-comment-input]'), send = find('[data-comment-send]');
  const replyChip = find('[data-comment-reply]'), replyName = find('[data-comment-reply-name]');
  const feedback = find('[data-comment-feedback]');
  const notify = (message, error = false) => {
    feedback.textContent = message; feedback.hidden = false;
    feedback.classList?.toggle('error', error);
    toastMessage(message, error);
  };
  let post = null, opener = null, items = [], page = -1, total = 0, pages = 0;
  let generation = 0, loading = false, sending = false, reply = null;
  const expanded = new Set();
  const current = token => token === generation && dialog.open;
  const count = () => {
    find('[data-comments-count]').textContent = `${total} ข้อความ`;
    doc.querySelectorAll(`[data-comment-count="${post.id}"]`).forEach(node => {node.textContent = total;});
    // Update embedded context so reopening doesn't briefly revert to the initial count.
    doc.querySelectorAll(`[data-open-comments="${post.id}"]`).forEach(node => {
      try {const context = JSON.parse(node.dataset.commentPost); context.commentCount = total; node.dataset.commentPost = JSON.stringify(context);} catch { /* Optional detail context. */ }
    });
  };
  const resetReply = () => {reply = null; replyChip.hidden = true; input.placeholder = 'เขียนความคิดเห็นเกี่ยวกับอาหาร…';};
  const paint = () => {list.innerHTML = renderComments(items, post.ownerId, expanded); more.hidden = page >= pages - 1; count();};
  const load = async (next = 0) => {
    if (loading) return;
    const token = generation, postId = post.id;
    loading = true; more.disabled = true; send.disabled = true; status.hidden = false; status.textContent = 'กำลังโหลดความคิดเห็น…';
    try {
      const data = await request(`/api/v1/food-posts/${postId}/comments?page=${next}`);
      if (!current(token)) return;
      const merged = new Map((next === 0 ? [] : items).map(c => [c.id, c]));
      data.items.forEach(c => merged.set(c.id, c));
      items = [...merged.values()].sort((a, b) => String(a.createdAt).localeCompare(String(b.createdAt)) || a.id - b.id);
      page = data.page; pages = data.totalPages; total = data.totalElements;
      status.hidden = true; paint();
    } catch (e) {
      if (current(token)) {status.textContent = `${e.message} — กดลองอีกครั้ง`; status.hidden = false; more.hidden = false; more.textContent = 'ลองอีกครั้ง';}
    } finally {if (current(token)) {loading = false; more.disabled = false; send.disabled = sending;}}
  };
  const open = async (context, source) => {
    if (!Number.isSafeInteger(Number(context?.id)) || Number(context.id) < 1) return;
    generation++; post = {...context, id: Number(context.id)}; opener = source;
    items = []; page = -1; pages = 0; total = Math.max(0, Number(post.commentCount) || 0);
    expanded.clear(); loading = false; sending = false; send.disabled = false;
    resetReply(); input.value = ''; list.innerHTML = ''; status.hidden = true; feedback.hidden = true; more.hidden = true; more.textContent = 'อ่านความคิดเห็นเพิ่มเติม';
    find('[data-comments-title]').textContent = post.title;
    find('[data-comments-post]').innerHTML = `<a class="comments-post-owner" href="/members/${post.ownerId}"><img src="/api/v1/members/${post.ownerId}/photo" alt=""><strong>${escape(post.ownerName)}</strong></a><h3>${escape(post.title)}</h3>${gallery(post, 'post-gallery comments-post-gallery')}${post.description ? `<p>${escape(post.description)}</p>` : ''}`;
    const member = isSignedIn(); form.hidden = !member; find('[data-comments-login]').hidden = member;
    count(); if (!dialog.open) dialog.showModal();
    if (member) await load();
    else list.innerHTML = '<p class="comments-empty">เข้าสู่ระบบเพื่ออ่าน แสดงความคิดเห็น และตอบกลับ</p>';
  };
  manager = {open};
  const close = () => {if (dialog.open) dialog.close();};
  find('[data-comments-close]').addEventListener('click', close);
  dialog.addEventListener('cancel', e => {e.preventDefault(); close();});
  dialog.addEventListener('click', e => {if (e.target === dialog) close();});
  dialog.addEventListener('close', () => {generation++; loading = false; if (opener?.isConnected) opener.focus({preventScroll: true});});
  find('[data-comment-cancel-reply]').addEventListener('click', () => {resetReply(); input.focus();});
  more.addEventListener('click', () => {more.textContent = 'อ่านความคิดเห็นเพิ่มเติม'; void load(page + 1);});
  list.addEventListener('toggle', e => {
    const id = Number(e.target.dataset?.thread);
    if (id) {if (e.target.open) expanded.add(id); else expanded.delete(id);}
  }, true);
  list.addEventListener('click', async e => {
    const replyButton = e.target.closest?.('[data-reply-comment]');
    if (replyButton) {
      const c = items.find(item => item.id === Number(replyButton.dataset.replyComment));
      if (!c || c.parentDeleted) {notify('ความคิดเห็นต้นทางถูกลบแล้ว', true); return;}
      reply = c; replyName.textContent = `ตอบกลับ ${c.authorName}`; replyChip.hidden = false;
      input.placeholder = `ตอบกลับ ${c.authorName}…`; input.focus(); return;
    }
    const remove = e.target.closest?.('[data-delete-comment]'), report = e.target.closest?.('[data-report-comment]');
    const button = remove || report;
    if (!button || button.disabled || loading || sending) return;
    const token = generation, postId = post.id;
    button.disabled = true;
    try {
      if (remove) {
        const ok = await confirm('ลบความคิดเห็น?', 'ข้อความจะถูกลบ แต่การตอบกลับที่มีอยู่จะยังคงอยู่');
        if (ok == null || !current(token)) return;
        await request(`/api/v1/comments/${Number(remove.dataset.deleteComment)}`, {method: 'DELETE'});
        if (!current(token)) return;
        resetReply(); await load(0);
      } else {
        const reason = await confirm('รายงานความคิดเห็น', 'ระบุเหตุผล เช่น ไม่เหมาะสมหรือสแปม', {label: 'เหตุผล', maxLength: 1000, confirm: 'ส่งรายงาน'});
        if (!reason || !current(token)) return;
        await request('/api/v1/reports', {method: 'POST', body: {postId, commentId: Number(report.dataset.reportComment), reason}});
        if (current(token)) notify('ส่งรายงานให้ผู้ดูแลแล้ว');
      }
    } catch (err) {if (current(token)) notify(err.message, true);} finally {button.disabled = false;}
  });
  form.addEventListener('submit', async e => {
    e.preventDefault(); if (sending || loading || !isSignedIn()) return;
    const body = input.value.trim();
    if (!body || body.length > 800) {notify('เขียนความคิดเห็น 1–800 ตัวอักษร', true); return;}
    const token = generation, postId = post.id, target = reply;
    sending = true; send.disabled = true; feedback.hidden = true;
    try {
      const created = await request(`/api/v1/food-posts/${postId}/comments`, {method: 'POST', body: {body, ...(target ? {parentCommentId: target.id} : {})}});
      if (!current(token)) return;
      if (!items.some(c => c.id === created.id)) {items.push(created); total++;}
      if (created.parentCommentId) expanded.add(created.parentCommentId);
      input.value = ''; resetReply(); paint();
      list.querySelector(`[data-comment-id="${created.id}"]`)?.scrollIntoView({block: 'nearest', behavior: 'smooth'});
      input.focus();
    } catch (err) {if (current(token)) notify(err.message, true);} finally {if (current(token)) {sending = false; send.disabled = false;}}
  });
  doc.addEventListener('click', e => {
    const button = e.target.closest?.('[data-open-comments]');
    if (!button || e.defaultPrevented) return;
    try {void open(JSON.parse(button.dataset.commentPost), button);} catch {notify('เปิดความคิดเห็นไม่ได้ กรุณาลองโหลดหน้านี้ใหม่', true);}
  });
  return manager;
}
