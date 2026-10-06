export const $ = (selector, root = document) => root.querySelector(selector);
export const $$ = (selector, root = document) => [
  ...root.querySelectorAll(selector),
];
export const escape = (value) =>
  String(value ?? "").replace(
    /[&<>"']/g,
    (c) =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[
        c
      ],
  );
export const icon = (name) =>
  `<svg class="icon" aria-hidden="true"><use href="#i-${name}"></use></svg>`;
export const signedIn = () => $('meta[name="signed-in"]')?.content === "true";
export async function api(url, options = {}) {
  const headers = new Headers(options.headers || {});
  let body = options.body;
  if (body && !(body instanceof FormData)) {
    headers.set("Content-Type", "application/json");
    body = JSON.stringify(body);
  }
  if (options.method && options.method !== "GET")
    headers.set(
      $('meta[name="_csrf_header"]').content,
      $('meta[name="_csrf"]').content,
    );
  let response;
  try {
    response = await fetch(url, {
      ...options,
      body,
      headers,
      credentials: "same-origin",
    });
  } catch {
    throw Error("เชื่อมต่อไม่สำเร็จ กรุณาตรวจสอบอินเทอร์เน็ตแล้วลองใหม่");
  }
  if (response.status === 204) return null;
  const data = await response.json().catch(() => null);
  if (!response.ok) {
    if (response.status === 401) location.href = "/login";
    throw Error(data?.message || "ทำรายการไม่สำเร็จ กรุณาลองอีกครั้ง");
  }
  return data;
}
let toastTimer;
export function toast(message, error = false) {
  const el = $("#toast");
  el.textContent = message;
  el.classList.toggle("error", error);
  el.hidden = false;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => (el.hidden = true), 5000);
}
export async function busy(button, task) {
  if (button.disabled) return;
  button.disabled = true;
  button.setAttribute("aria-busy", "true");
  try {
    return await task();
  } catch (e) {
    toast(e.message, true);
  } finally {
    button.disabled = false;
    button.removeAttribute("aria-busy");
  }
}
export function asDate(value) {
  return new Date(/[Zz]|[+-]\d\d:\d\d$/.test(value) ? value : value + "+07:00");
}
export function date(value) {
  return new Intl.DateTimeFormat("th-TH", {
    day: "numeric",
    month: "short",
    year: "numeric",
    timeZone: "Asia/Bangkok",
  }).format(asDate(value));
}
export function time(value) {
  return new Intl.DateTimeFormat("th-TH", {
    hour: "2-digit",
    minute: "2-digit",
    timeZone: "Asia/Bangkok",
  }).format(asDate(value));
}
export function dateTime(value) {
  return `${date(value)} · ${time(value)} น.`;
}
export function pickupWindow(p) {
  return `${date(p.availableFrom)} · ${time(p.availableFrom)} – ${date(p.availableFrom) !== date(p.availableUntil) ? date(p.availableUntil) + " " : ""}${time(p.availableUntil)} น.`;
}
export function inputTime(date) {
  return new Date(date.getTime() + 7 * 3600000).toISOString().slice(0, 16);
}
const states = {
  AVAILABLE: ["พร้อมแบ่งปัน", ""],
  LOW_STOCK: ["ใกล้หมดแล้ว", "orange"],
  SCHEDULED: ["นัดรับล่วงหน้า", "blue"],
  FULL: ["จองเต็มแล้ว", "orange"],
  CLAIMED: ["รับครบแล้ว", "gray"],
  CANCELLED: ["ยกเลิกแล้ว", "gray"],
  EXPIRED: ["หมดเวลารับ", "gray"],
  RESERVED: ["จองสำเร็จ", "blue"],
  COLLECTED: ["รับอาหารแล้ว", ""],
  OPEN: ["รอตรวจสอบ", "orange"],
  RESOLVED: ["ตรวจสอบแล้ว", "gray"],
};
export function badge(state) {
  const [label, style] = states[state] || [state, "gray"];
  return `<span class="badge ${style}">${escape(label)}</span>`;
}
export const categories = {
  FOOD: "อาหาร",
  DRINK: "เครื่องดื่ม",
  SNACK: "ของว่าง",
};
export function photo(p) {
  return p.imageUrl
    ? `<img src="${escape(p.imageUrl)}" alt="${escape(p.title)}" loading="lazy">`
    : `<div class="food-placeholder">${icon("food")}</div>`;
}
export function empty(title, text, link = "/explore", label = "ค้นหาอาหาร") {
  return `<div class="empty-state">${icon("leaf")}<h3>${escape(title)}</h3><p>${escape(text)}</p>${link ? `<a class="btn btn-soft" href="${link}">${escape(label)} ${icon("arrow")}</a>` : ""}</div>`;
}
export function errorBox(el, e, retry) {
  el.replaceChildren();
  const box = document.createElement("div");
  box.className = "error-box";
  const p = document.createElement("p");
  p.textContent = e.message;
  box.append(p);
  if (retry) {
    const button = document.createElement("button");
    button.className = "btn btn-soft";
    button.textContent = "ลองอีกครั้ง";
    button.onclick = retry;
    box.append(button);
  }
  el.append(box);
}
export function card(p) {
  return `<article class="food-card"><a class="card-image" href="/posts/${p.id}" aria-label="ดู ${escape(p.title)}">${photo(p)}${badge(p.status)}<span class="free-badge">แบ่งปันฟรี</span></a><div class="card-content"><div class="card-topline"><span>${escape(categories[p.category])}</span><span>${p.distanceKm != null ? `${p.distanceKm.toFixed(1)} กม. จากคุณ` : "KKU COMMUNITY"}</span></div><h3><a href="/posts/${p.id}">${escape(p.title)}</a></h3><p class="card-location">${icon("pin")}${escape(p.pickupLocationName)}</p><p class="card-time">${icon("clock")}รับภายใน ${escape(dateTime(p.availableUntil))}</p><div class="card-bottom"><span class="card-stock">เหลือ <strong>${p.availableQuantity}</strong> ${escape(p.unit)}</span><a class="card-link" href="/posts/${p.id}">ดูรายละเอียด ${icon("arrow")}</a></div></div></article>`;
}
export function paginate(data, onPage) {
  const el = $("#pagination");
  if (!el) return;
  el.replaceChildren();
  if (data.totalPages < 2) return;
  const start = Math.max(0, data.page - 2),
    end = Math.min(data.totalPages, start + 5);
  for (let i = start; i < end; i++) {
    const b = document.createElement("button");
    b.textContent = i + 1;
    b.classList.toggle("active", i === data.page);
    b.setAttribute("aria-label", `หน้าที่ ${i + 1}`);
    if (i === data.page) b.setAttribute("aria-current", "page");
    b.onclick = () => onPage(i);
    el.append(b);
  }
}
export function ask(title, description, field = null) {
  return new Promise((resolve) => {
    const dialog = $("#action-dialog");
    $("#dialog-title").textContent = title;
    $("#dialog-description").textContent = description;
    const group = $("#dialog-field"),
      input = $("#dialog-input");
    group.hidden = !field;
    input.value = field?.value ?? "";
    input.required = !!field;
    input.type = field?.type || "text";
    input.maxLength = field?.maxLength || 1000;
    input.inputMode = field?.numeric ? "numeric" : "text";
    input.removeAttribute("min");
    input.removeAttribute("max");
    input.removeAttribute("pattern");
    if (field?.min != null) input.min = field.min;
    if (field?.max != null) input.max = field.max;
    if (field?.pattern) input.pattern = field.pattern;
    $("#dialog-label").textContent = field?.label || "";
    $("#dialog-confirm").textContent = field?.confirm || "ยืนยัน";
    dialog.returnValue = "";
    dialog.onclose = () => {
      resolve(
        dialog.returnValue === "confirm" ? (field ? input.value : true) : null,
      );
      dialog.onclose = null;
    };
    $(".dialog-close", dialog).formNoValidate = true;
    $$('button[value="cancel"]', dialog).forEach(
      (b) => (b.formNoValidate = true),
    );
    dialog.showModal();
  });
}
