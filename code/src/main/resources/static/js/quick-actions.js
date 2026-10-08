import { $, $$, api, escape, icon, busy, toast, ask, inputTime } from "./ui.js";
import { drawPass } from "./pickup.mjs";

export function reservationLimit(post, reservation) {
  const perPerson = Number.isInteger(post.maxPerPerson) ? post.maxPerPerson : 10000;
  return Math.min(10000, perPerson, post.availableQuantity + (reservation?.quantity || 0));
}

export function extensionChoices() { return [30, 60, 120]; }

export function mountPostExtension(root, post, onSaved) {
  let saving = false;
  const setQuick = (minutes) => {
    const input = $('[data-extension-until]', root);
    input.value = inputTime(new Date(Date.now() + minutes * 60000));
    $$('[data-extension-minutes]', root).forEach((button) =>
      button.classList.toggle('active', Number(button.dataset.extensionMinutes) === minutes));
  };
  const render = () => {
    const defaultUntil = inputTime(new Date(Date.now() + 60 * 60000));
    root.innerHTML = `<h3>ขยายเวลารับอาหาร</h3><p class="field-note">${escape(post.title)} ยังเหลือ ${post.availableQuantity} ${escape(post.unit)} การจองที่หมดเวลาแล้วจะไม่ถูกเปิดกลับมา</p><form data-extension-form><div class="extension-presets">${extensionChoices().map((minutes) => `<button type="button" class="btn btn-white ${minutes === 60 ? 'active' : ''}" data-extension-minutes="${minutes}">+${minutes < 60 ? minutes + ' นาที' : minutes / 60 + ' ชั่วโมง'}</button>`).join('')}</div><label>เปิดรับถึงเวลา<input data-extension-until type="datetime-local" min="${inputTime(new Date(Date.now() + 60000))}" value="${defaultUntil}" required></label><div class="row-actions"><button type="submit" class="btn btn-green" data-extension-save>${icon('clock')} ยืนยันขยายเวลา</button><button type="button" class="btn btn-white" data-extension-later>ไว้ก่อน</button></div></form>`;
    $$('[data-extension-minutes]', root).forEach((button) =>
      button.onclick = () => setQuick(Number(button.dataset.extensionMinutes)));
    $('[data-extension-until]', root).oninput = () =>
      $$('[data-extension-minutes]', root).forEach((button) => button.classList.remove('active'));
    $('[data-extension-later]', root).onclick = () => { root.hidden = true; };
    $('[data-extension-form]', root).onsubmit = (event) => {
      event.preventDefault();
      const form = event.currentTarget, save = $('[data-extension-save]', root);
      if (saving || !form.reportValidity()) return;
      saving = true;
      busy(save, async () => {
        try {
          const availableUntil = $('[data-extension-until]', root).value;
          if (!(await ask('ขยายเวลารับอาหารต่อ?', 'โพสต์จะกลับมาเปิดรับจองถึงเวลาที่เลือก โดยการจองที่หมดเวลาแล้วจะไม่กลับมา'))) return;
          await api(`/api/v1/food-posts/${post.id}/extend`, {method:'POST', body:{availableUntil}});
          toast('ขยายเวลารับอาหารแล้ว');
          await onSaved();
        } finally { saving = false; }
      });
    };
  };
  render();
}

export function stockPreview(stock, action, amount) {
  if (!Number.isInteger(amount) || amount < 1 || amount > 10000)
    throw Error("กรุณาระบุจำนวนเต็มระหว่าง 1 ถึง 10,000");
  let total = stock.quantity, offline = stock.offlineQuantity;
  if (action === "ADD") {
    if (total + amount > 10000) throw Error("จำนวนทั้งหมดต้องไม่เกิน 10,000");
    total += amount;
  } else if (action === "REMOVE" || action === "OFFLINE") {
    if (amount > stock.availableQuantity)
      throw Error("ใช้ได้เฉพาะจำนวนที่ยังว่าง ต้องกันของให้ผู้จองไว้ก่อน");
    if (action === "REMOVE") {
      if (total - amount < 1) throw Error("หากนำของทั้งหมดออก กรุณาใช้ปุ่มปิดโพสต์แทน");
      total -= amount;
    } else offline += amount;
  } else if (action === "UNDO_OFFLINE") {
    if (amount > offline) throw Error("แก้ยอดได้ไม่เกินจำนวนที่บันทึกว่าแจกนอกเว็บ");
    offline -= amount;
  } else throw Error("กรุณาเลือกวิธีจัดการจำนวน");
  return {total, offline, available: stock.availableQuantity + total - stock.quantity - offline + stock.offlineQuantity};
}

function stepper(value, max, id) {
  return `<div class="quantity-stepper"><button type="button" class="btn btn-white" data-minus aria-label="ลดจำนวน">−</button><input id="${id}" data-quantity name="quantity" type="number" inputmode="numeric" min="1" max="${Math.max(1, max)}" step="1" value="${value}" required><button type="button" class="btn btn-white" data-plus aria-label="เพิ่มจำนวน">+</button></div>`;
}

export function wireStepper(root, onChange = () => {}) {
  const input = $('[data-quantity]', root), minus = $('[data-minus]', root), plus = $('[data-plus]', root);
  const update = () => {
    const q = Number(input.value);
    minus.disabled = !Number.isInteger(q) || q <= Number(input.min);
    plus.disabled = !Number.isInteger(q) || q >= Number(input.max);
    onChange(q);
  };
  for (const [button, delta] of [[minus, -1], [plus, 1]]) {
    button.onclick = () => {
      input.value = Math.max(Number(input.min), Math.min(Number(input.max), (Number(input.value) || 1) + delta));
      input.dispatchEvent(new Event('input', {bubbles: true}));
    };
  }
  input.oninput = update;
  update();
  return update;
}

// Existing reservations are loaded before offering a new booking, including FULL posts.
export function mountReservation(root, originalPost, trip, onStock = () => {}) {
  let post = originalPost, reservation = null, requestKey = crypto.randomUUID(), submitting = false, seq = 0;
  const refresh = async () => {
    const current = ++seq;
    root.innerHTML = '<p class="field-note" role="status">กำลังตรวจการจองของคุณ…</p>';
    try {
      const [fresh, mine] = await Promise.all([
        api(`/api/v1/food-posts/${post.id}`),
        api(`/api/v1/food-posts/${post.id}/my-reservation`),
      ]);
      if (current !== seq) return;
      post = fresh; reservation = mine;
      // An authoritative lookup resolves any creation whose response was lost.
      // Keep the key only while the creation outcome is still unknown.
      if (mine) requestKey = crypto.randomUUID();
      onStock(post); render();
    } catch (error) {
      if (current !== seq) return;
      root.innerHTML = `<p class="info-note" role="alert">${escape(error.message)}</p><button type="button" class="btn btn-soft full" data-retry>ตรวจการจองอีกครั้ง</button><a class="quick-reservations-link" href="/reservations">ดูการจองของฉัน</a>`;
      $('[data-retry]', root).onclick = () => { if (!submitting) refresh(); };
    }
  };
  const run = (button, task) => {
    if (submitting) return;
    submitting = true;
    return busy(button, async () => {
      try { await task(); }
      catch (error) { await refresh(); throw error; }
      finally { submitting = false; }
    });
  };
  const render = () => {
    const existing = reservation?.status === 'RESERVED';
    const mutable = ['AVAILABLE', 'LOW_STOCK', 'SCHEDULED', 'FULL'].includes(post.status);
    const max = reservationLimit(post, existing ? reservation : null);
    if (!existing && (!mutable || post.availableQuantity < 1)) {
      root.innerHTML = `<p class="info-note">${reservation ? 'การจองเดิมหมดเวลารับแล้ว' : 'รายการนี้ยังไม่พร้อมรับจอง'}</p><a class="quick-reservations-link" href="/reservations">ดูการจองของฉัน</a>`;
      return;
    }
    const capNote = post.maxPerPerson ? ` · จำกัดไม่เกิน ${post.maxPerPerson} ${escape(post.unit)} ต่อคน` : '';
    root.innerHTML = `${existing ? `<div class="my-booking-label">${icon('check')} คุณจองไว้แล้ว <strong>${reservation.quantity} ${escape(post.unit)}</strong><small>FS-${reservation.id} · ปรับจำนวนที่นี่ได้เลย</small></div>` : ''}
      ${mutable ? `<form data-booking-form><label for="detail-booking-quantity">${existing ? 'ปรับจำนวนที่จอง' : 'จำนวนที่ต้องการ'} (${escape(post.unit)})</label>${stepper(existing ? reservation.quantity : 1, max, 'detail-booking-quantity')}<p class="field-note">${existing ? `รวมที่คุณจองไว้แล้ว ปรับได้สูงสุด ${max}` : `จองได้สูงสุด ${max}`} ${escape(post.unit)}${capNote}</p><button type="submit" class="btn btn-primary full" data-save>${icon(existing ? 'check' : 'bag')} ${existing ? 'บันทึกจำนวนจอง' : 'จองอาหารนี้'}</button></form>` : '<p class="info-note">โพสต์ปิดรับแล้ว ยกเลิกการจองที่ยังค้างได้</p>'}
      ${existing ? `<div class="quick-booking-actions"><button type="button" class="btn btn-soft" data-qr>${icon('camera')} ดู QR รับอาหาร</button><button type="button" class="btn btn-white" data-cancel>ยกเลิกการจอง</button></div><section class="quick-pickup" data-ticket hidden><h3>บัตรรับอาหารของคุณ</h3><svg class="pass-qr" data-quick-pass></svg><p class="pickup-code">รหัสสำรอง<strong>${escape(reservation.pickupCode)}</strong></p><p class="field-note">แสดงให้เจ้าของโพสต์เมื่อมาถึง • อย่าแชร์รหัสนี้</p></section>` : ''}`;
    if (mutable) {
      const save = $('[data-save]', root), input = $('[data-quantity]', root);
      wireStepper(root, (q) => {
        save.disabled = !Number.isInteger(q) || q < 1 || q > max || (existing && q === reservation.quantity);
      });
      $('[data-booking-form]', root).onsubmit = (e) => {
        e.preventDefault();
        if (!e.target.reportValidity() || submitting) return;
        const quantity = Number(input.value);
        if (!Number.isInteger(quantity) || quantity < 1 || quantity > max) return;
        run(save, async () => {
          if (!existing && !(await trip.beforeReserve())) return;
          if (!(await ask(existing ? 'บันทึกจำนวนจองใหม่?' : 'จองมื้อนี้ไว้เลยไหม?',
            `${post.title} จำนวน ${quantity} ${post.unit}${existing ? ` (เดิม ${reservation.quantity})` : ' • กรุณาไปรับภายในเวลาที่กำหนด'}`))) return;
          if (existing) await api(`/api/v1/reservations/${reservation.id}`, {method:'PUT', body:{quantity}});
          else {
            await api(`/api/v1/food-posts/${post.id}/reservations`, {method:'POST', headers:{'Idempotency-Key':requestKey}, body:{quantity}});
            requestKey = crypto.randomUUID();
          }
          toast(existing ? 'บันทึกจำนวนจองแล้ว' : 'จองสำเร็จ เปิด QR รับอาหารได้ที่นี่เลย');
          await refresh();
        });
      };
    }
    if (existing) {
      let qrDrawn = false;
      $('[data-qr]', root).onclick = (e) => {
        const ticket = $('[data-ticket]', root);
        ticket.hidden = !ticket.hidden;
        e.currentTarget.setAttribute('aria-expanded', String(!ticket.hidden));
        if (!ticket.hidden && !qrDrawn) {
          try { drawPass($('[data-quick-pass]', root), reservation.id, reservation.pickupCode); }
          catch { $('[data-quick-pass]', root).hidden = true; }
          qrDrawn = true;
        }
      };
      $('[data-qr]', root).setAttribute('aria-expanded','false');
      $('[data-cancel]', root).onclick = () => run($('[data-cancel]', root), async () => {
        if (!(await ask('ยกเลิกการจองนี้?', 'จำนวนที่คุณจองไว้จะกลับไปให้เพื่อนคนอื่นจอง'))) return;
        await api(`/api/v1/reservations/${reservation.id}`, {method:'DELETE'});
        requestKey = crypto.randomUUID();
        toast('ยกเลิกการจองแล้ว'); await refresh();
      });
    }
  };
  refresh();
  return {refresh};
}

const stockActions = {
  ADD: ['เพิ่มอาหารเข้าจำนวนทั้งหมด', 'มีอาหารเพิ่ม? เพิ่มจำนวนที่พร้อมแบ่งปัน โดยคิวเดิมยังอยู่ครบ'],
  OFFLINE: ['บันทึกแจกนอกเว็บ', 'บันทึกของที่ส่งมอบให้คนจากช่องทางอื่น ใช้เฉพาะจำนวนที่ยังว่าง'],
  REMOVE: ['ลดจำนวนที่ยังว่าง', 'ใช้เมื่อของเสียหรือมีของน้อยกว่าที่ลงไว้ ไม่นับเป็นการแจก'],
  UNDO_OFFLINE: ['แก้ยอดแจกนอกเว็บที่ลงเกิน', 'ใช้เมื่อบันทึกผิดเท่านั้น ของจำนวนนี้จะกลับมาเปิดให้จอง'],
};

export function mountOwnerStock(root, postId, onSaved) {
  let stock, submitting = false, seq = 0;
  const refresh = async (message = '') => {
    const current = ++seq;
    root.innerHTML = '<p class="field-note">กำลังตรวจจำนวนล่าสุด…</p>';
    try {
      const fresh = await api(`/api/v1/food-posts/${postId}/stock`);
      if (current !== seq) return;
      stock = fresh; render(message);
    } catch (error) {
      if (current !== seq) return;
      root.innerHTML = `<p class="info-note" role="alert">${escape(error.message)}</p><button type="button" class="btn btn-soft" data-retry>ลองใหม่</button>`;
      $('[data-retry]', root).onclick = () => { if (!submitting) refresh(); };
    }
  };
  const render = (message) => {
    root.innerHTML = `<h3>จัดการจำนวนอาหาร</h3><div class="stock-snapshot"><span>ยังว่าง<strong>${stock.availableQuantity}</strong></span><span>กันให้ผู้จอง<strong>${stock.reservedQuantity}</strong></span><span>รับผ่านเว็บ<strong>${stock.collectedQuantity}</strong></span><span>แจกนอกเว็บ<strong>${stock.offlineQuantity}</strong></span></div><p class="field-note">จำนวนทั้งหมด ${stock.quantity} ${escape(stock.unit)} · คิวผู้จองได้รับสิทธิ์ก่อนเสมอ</p>${message ? `<p class="info-note" role="alert">${escape(message)}</p>` : ''}
      <form data-stock-form><label for="stock-action-${postId}">ต้องการทำอะไร?</label><select id="stock-action-${postId}" data-stock-action>${Object.entries(stockActions).map(([key,[label]])=>`<option value="${key}">${label}</option>`).join('')}</select><p class="field-note" data-stock-help></p><label for="stock-quantity-${postId}">จำนวน (${escape(stock.unit)})</label>${stepper(1, 10000, `stock-quantity-${postId}`)}<p class="stock-preview" data-stock-preview role="status"></p><button type="submit" class="btn btn-green full" data-save>${icon('check')} ตรวจสอบและยืนยัน</button></form>`;
    const action = $('[data-stock-action]', root), input = $('[data-quantity]', root), save = $('[data-save]', root);
    const preview = () => {
      $('[data-stock-help]', root).textContent = stockActions[action.value][1];
      input.max = action.value === 'ADD' ? 10000 - stock.quantity : action.value === 'UNDO_OFFLINE' ? stock.offlineQuantity : action.value === 'REMOVE' ? Math.min(stock.availableQuantity, stock.quantity - 1) : stock.availableQuantity;
      try {
        const next = stockPreview(stock, action.value, Number(input.value));
        $('[data-stock-preview]', root).textContent = `หลังบันทึก: ยังว่าง ${next.available} · กันให้ผู้จอง ${stock.reservedQuantity} · แจกนอกเว็บ ${next.offline} ${stock.unit}`;
        save.disabled = false;
      } catch (error) { $('[data-stock-preview]', root).textContent = error.message; save.disabled = true; }
    };
    const updateStepper = wireStepper(root, preview);
    // Preview sets the action-specific maximum before updating +/- disabled states.
    action.onchange = () => { preview(); updateStepper(); };
    preview(); updateStepper();
    $('[data-stock-form]', root).onsubmit = (e) => {
      e.preventDefault();
      if (submitting || !e.target.reportValidity()) return;
      const selectedAction = action.value, amount = Number(input.value), confirmedStock = {...stock};
      let next;
      try { next = stockPreview(confirmedStock, selectedAction, amount); }
      catch (error) { toast(error.message, true); return; }
      submitting = true;
      busy(save, async () => {
        try {
          if (!(await ask(stockActions[selectedAction][0] + '?',
            `${amount} ${confirmedStock.unit} • หลังบันทึกยังว่าง ${next.available} และกันให้ผู้จอง ${confirmedStock.reservedQuantity} ${confirmedStock.unit}`))) return;
          await api(`/api/v1/food-posts/${postId}/stock`, {method:'POST', body:{action:selectedAction, amount, expectedVersion:confirmedStock.version}});
          toast('บันทึกจำนวนแล้ว'); await onSaved();
        } catch (error) {
          await refresh(error.message + ' · ตรวจยอดล่าสุดก่อนลองอีกครั้ง');
          throw error;
        } finally { submitting = false; }
      });
    };
  };
  refresh();
  return {refresh};
}
