import {
  $,
  $$,
  api,
  escape,
  icon,
  signedIn,
  toast,
  busy,
  dateTime,
  pickupWindow,
  inputTime,
  badge,
  categories,
  photo,
  empty,
  errorBox,
  card,
  paginate,
  ask,
} from "./ui.js";
import { createMap, locate, markers, directions } from "./maps.js";
const page = document.body.dataset.page;
async function home() {
  const el = $("#home-food");
  const load = async () => {
    try {
      const d = await api("/api/v1/food-posts?sort=latest&size=3");
      el.innerHTML = d.items.length
        ? d.items.map(card).join("")
        : empty(
            "เริ่มต้นมื้อแห่งการแบ่งปัน",
            "ตอนนี้ยังไม่มีอาหารที่เปิดรับจอง มาเป็นคนแรกที่ส่งต่อมื้อดี ๆ กัน",
            "/posts/new",
            "เริ่มแบ่งปันอาหาร",
          );
    } catch (e) {
      errorBox(el, e, load);
    }
  };
  await load();
}
async function explore() {
  const params = new URLSearchParams(location.search);
  let state = {
      q: params.get("q") || "",
      category: "",
      sort: "expiry",
      page: 0,
      now: false,
    },
    coords = {},
    map = null,
    layer = null,
    mapVisible = params.get("view") === "map",
    requestSeq = 0;
  $("#search").value = state.q;
  const loadMap = async () => {
    if (!mapVisible) return;
    try {
      if (!map) map = createMap("explore-map").map;
      const d = await api(
        "/api/v1/food-posts/map?" +
          new URLSearchParams({
            q: state.q,
            category: state.category,
            now: state.now,
          }),
      );
      layer = markers(map, d.items, layer);
      $("#map-status").textContent =
        `พบ ${d.items.length} จุดแบ่งปัน • เลือกหมุดเพื่อดูรายละเอียด${d.totalElements > 200 ? " • แสดง 200 จุดแรก ลองค้นหาให้เจาะจงขึ้น" : ""}`;
      map.invalidateSize();
    } catch (e) {
      $("#map-status").textContent = e.message;
    }
  };
  const load = async () => {
    const seq = ++requestSeq;
    $("#food-results").innerHTML =
      '<div class="loading-box">กำลังค้นหาอาหาร…</div>';
    try {
      const d = await api(
        "/api/v1/food-posts?" + new URLSearchParams({ ...state, ...coords }),
      );
      if (seq !== requestSeq) return;
      $("#result-count").textContent =
        `อาหารพร้อมแบ่งปัน ${d.totalElements} รายการ`;
      $("#food-results").innerHTML = d.items.length
        ? d.items.map(card).join("")
        : empty(
            "ยังไม่เจอเมนูที่ค้นหา",
            "ลองเปลี่ยนคำค้นหรือตัวกรอง แล้วกลับมาดูอาหารจากเพื่อน ๆ อีกครั้ง",
            null,
          );
      paginate(d, (n) => {
        state.page = n;
        load();
      });
      await loadMap();
    } catch (e) {
      if (seq === requestSeq) errorBox($("#food-results"), e, load);
    }
  };
  $("#search-form").onsubmit = (e) => {
    e.preventDefault();
    state.q = $("#search").value.trim();
    state.page = 0;
    load();
  };
  $$("#category-filters button").forEach(
    (b) =>
      (b.onclick = () => {
        $$("#category-filters button").forEach((x) =>
          x.classList.toggle("active", x === b),
        );
        state.category = b.dataset.category;
        state.page = 0;
        load();
      }),
  );
  $("#available-now").onchange = (e) => {
    state.now = e.target.checked;
    state.page = 0;
    load();
  };
  const nearby = async () => {
    try {
      coords = await locate();
      state.sort = "nearby";
      $("#sort").value = "nearby";
      state.page = 0;
      await load();
    } catch (e) {
      toast(e.message, true);
      if (state.sort === "nearby") {
        state.sort = "expiry";
        $("#sort").value = "expiry";
      }
    }
  };
  $("#nearby-button").onclick = () => busy($("#nearby-button"), nearby);
  $("#sort").onchange = (e) => {
    state.sort = e.target.value;
    state.page = 0;
    if (state.sort === "nearby" && !coords.lat) nearby();
    else load();
  };
  function toggle(show) {
    mapVisible = show;
    $("#explore-map-shell").hidden = !show;
    $("#map-view").classList.toggle("active", show);
    $("#list-view").classList.toggle("active", !show);
    if (show) loadMap();
  }
  $("#map-view").onclick = () => toggle(true);
  $("#list-view").onclick = () => toggle(false);
  $("#explore-map-shell").hidden = !mapVisible;
  $("#map-view").classList.toggle("active", mapVisible);
  $("#list-view").classList.toggle("active", !mapVisible);
  await load();
}
async function detail() {
  const id = document.body.dataset.postId,
    el = $("#food-detail");
  try {
    const p = await api("/api/v1/food-posts/" + id);
    const canReserve =
      ["AVAILABLE", "LOW_STOCK", "SCHEDULED"].includes(p.status) &&
      p.availableQuantity > 0;
    const closed = ["CANCELLED", "EXPIRED", "CLAIMED"].includes(p.status);
    el.innerHTML = `<div class="detail-layout"><section><div class="detail-photo">${photo(p)}</div><div class="detail-panel panel"><span class="section-kicker">${escape(categories[p.category])} · FROM OUR COMMUNITY</span><h1 class="detail-title">${escape(p.title)}</h1><div class="detail-meta">${badge(p.status)}<span>${icon("pin")} ${escape(p.pickupLocationName)}</span></div><h2>รายละเอียดอาหาร</h2><p>${escape(p.description)}</p>${p.allergens ? `<div class="allergen-note"><strong>ส่วนผสมที่อาจทำให้แพ้</strong><br>${escape(p.allergens)}</div>` : ""}</div><section class="detail-panel panel"><h2>${icon("pin")} จุดนัดรับอาหาร</h2><p>${escape(p.pickupLocationName)}</p><div id="detail-map" class="detail-map"></div><p class="field-note" id="detail-map-status">พิกัด ${p.latitude}, ${p.longitude}</p><a class="btn btn-soft full" href="${directions(p)}" target="_blank" rel="noopener">${icon("map")} เปิดเส้นทางไปจุดรับ</a></section></section><aside class="booking-panel panel"><span class="section-kicker">A MEAL MADE FOR SHARING</span><div class="booking-price">แบ่งปันฟรี ♡</div><p>มื้อดี ๆ จากเพื่อนในชุมชน</p><div class="booking-stock"><span>จำนวนที่ยังจองได้</span><span><strong>${p.availableQuantity}</strong> ${escape(p.unit)}</span></div><div class="pickup-time">${icon("clock")}<div><strong>เวลานัดรับ</strong><span>${escape(pickupWindow(p))}</span></div></div>${p.mine ? `<a class="btn btn-primary full" href="/account/posts">จัดการโพสต์และผู้จอง ${icon("arrow")}</a>${!closed ? `<a class="btn btn-soft full" style="margin-top:10px" href="/posts/${p.id}/edit">${icon("edit")} แก้ไขโพสต์</a>` : ""}` : signedIn() ? (canReserve ? `<form id="reserve-form"><label>จำนวนที่ต้องการ (${escape(p.unit)})<input name="quantity" type="number" min="1" max="${p.availableQuantity}" value="1" required></label><button class="btn btn-primary btn-lg full" type="submit">${icon("bag")} จองอาหารนี้</button><p class="field-note center">จองแล้ว ดูรหัสรับอาหารได้ใน “การจองของฉัน”</p></form>` : `<p class="info-note">รายการนี้ยังไม่พร้อมรับจอง</p>`) : `<a class="btn btn-primary full" href="/login">เข้าสู่ระบบเพื่อจองอาหาร ${icon("arrow")}</a>`}<div class="owner-label"><span>${icon("user")}</span><div><small>แบ่งปันโดย</small><strong>${escape(p.ownerName)}</strong></div></div>${signedIn() && !p.mine ? '<button class="report-button" id="report-post">รายงานปัญหาของโพสต์นี้</button>' : ""}</aside></div>`;
    try {
      createMap("detail-map").setPin(p.latitude, p.longitude);
    } catch (e) {
      $("#detail-map-status").textContent =
        e.message + " • ใช้ปุ่มเปิดเส้นทางด้านล่างได้";
    }
    let key = crypto.randomUUID();
    $("#reserve-form")?.addEventListener("submit", (e) => {
      e.preventDefault();
      busy($("button", e.target), async () => {
        const qty = Number(e.target.elements.quantity.value);
        if (
          !(await ask(
            "จองมื้อนี้ไว้เลยไหม?",
            `${p.title} จำนวน ${qty} ${p.unit} • กรุณาไปรับภายในเวลาที่กำหนด`,
          ))
        )
          return;
        await api(`/api/v1/food-posts/${id}/reservations`, {
          method: "POST",
          headers: { "Idempotency-Key": key },
          body: { quantity: qty },
        });
        location.href = "/reservations";
      });
    });
    $("#report-post")?.addEventListener("click", () =>
      busy($("#report-post"), async () => {
        const reason = await ask(
          "แจ้งปัญหาให้ผู้ดูแล",
          "บอกสิ่งที่พบเพื่อช่วยให้ชุมชนของเราปลอดภัย",
          { label: "รายละเอียดปัญหา", maxLength: 1000, confirm: "ส่งรายงาน" },
        );
        if (reason) {
          await api("/api/v1/reports", {
            method: "POST",
            body: { postId: Number(id), reason },
          });
          toast("ส่งรายงานให้ผู้ดูแลแล้ว ขอบคุณที่ช่วยดูแลชุมชน");
        }
      }),
    );
  } catch (e) {
    errorBox(el, e, detail);
  }
}
async function editor() {
  const form = $("#post-form");
  let id = Number(document.body.dataset.postId),
    map = null,
    previewUrl = null,
    originalTimes = {};
  form.addEventListener(
    "invalid",
    (e) => {
      if (["latitude", "longitude"].includes(e.target.name))
        $(".manual-coordinates").open = true;
    },
    true,
  );
  function pin(lat, lng) {
    form.elements.latitude.value = Number(lat).toFixed(7);
    form.elements.longitude.value = Number(lng).toFixed(7);
    $("#pin-status").textContent =
      `เลือกจุดรับแล้ว · ${Number(lat).toFixed(5)}, ${Number(lng).toFixed(5)}`;
  }
  try {
    map = createMap("editor-map", pin);
  } catch (e) {
    $("#pin-status").textContent = e.message;
  }
  $("#apply-coordinates").onclick = () => {
    const lat = Number(form.elements.latitude.value),
      lng = Number(form.elements.longitude.value);
    if (
      !form.elements.latitude.value ||
      !form.elements.longitude.value ||
      Math.abs(lat) > 90 ||
      Math.abs(lng) > 180
    )
      return toast("กรุณากรอกพิกัดที่ถูกต้อง", true);
    pin(lat, lng);
    map?.setPin(lat, lng);
  };
  $("#use-location").onclick = () =>
    busy($("#use-location"), async () => {
      const { lat, lng } = await locate();
      map?.setPin(lat, lng);
      pin(lat, lng);
    });
  $("#food-photo").onchange = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    if (
      file.size > 5 * 1024 * 1024 ||
      !["image/jpeg", "image/png"].includes(file.type)
    ) {
      e.target.value = "";
      toast("กรุณาเลือกรูป JPG/PNG ไม่เกิน 5 MB", true);
      return;
    }
    if (previewUrl) URL.revokeObjectURL(previewUrl);
    previewUrl = URL.createObjectURL(file);
    $("#photo-preview").src = previewUrl;
    $("#photo-preview").hidden = false;
  };
  if (id) {
    try {
      const p = await api("/api/v1/food-posts/" + id);
      for (const [k, v] of Object.entries(p))
        if (form.elements.namedItem(k))
          form.elements.namedItem(k).value = v ?? "";
      originalTimes = {
        availableFrom: p.availableFrom,
        availableUntil: p.availableUntil,
      };
      form.elements.availableFrom.value = p.availableFrom.slice(0, 16);
      form.elements.availableUntil.value = p.availableUntil.slice(0, 16);
      pin(p.latitude, p.longitude);
      map?.setPin(p.latitude, p.longitude);
      if (p.imageUrl) {
        $("#photo-preview").src = p.imageUrl;
        $("#photo-preview").hidden = false;
      }
      $("#save-post").innerHTML = icon("check") + " บันทึกการแก้ไข";
    } catch (e) {
      toast(e.message, true);
      $("#save-post").disabled = true;
    }
  } else {
    form.elements.availableFrom.value = inputTime(new Date());
    form.elements.availableUntil.value = inputTime(
      new Date(Date.now() + 2 * 3600000),
    );
  }
  form.onsubmit = (e) => {
    e.preventDefault();
    busy($("#save-post"), async () => {
      const error = $("#form-error");
      error.hidden = true;
      if (!form.elements.latitude.value || !form.elements.longitude.value)
        throw Error("กรุณาปักหมุดจุดรับอาหารบนแผนที่ก่อน");
      const data = Object.fromEntries(new FormData(form));
      for (const k of ["availableFrom", "availableUntil"])
        if (originalTimes[k] && data[k] === originalTimes[k].slice(0, 16))
          data[k] = originalTimes[k];
      data.quantity = Number(data.quantity);
      data.latitude = Number(data.latitude);
      data.longitude = Number(data.longitude);
      const saved = await api("/api/v1/food-posts" + (id ? "/" + id : ""), {
        method: id ? "PUT" : "POST",
        body: data,
      });
      id = saved.id;
      const file = $("#food-photo").files[0];
      if (file) {
        try {
          const upload = new FormData();
          upload.append("file", file);
          await api(`/api/v1/food-posts/${id}/images`, {
            method: "POST",
            body: upload,
          });
        } catch (e) {
          error.textContent =
            "โพสต์บันทึกแล้ว แต่รูปยังอัปโหลดไม่สำเร็จ: " +
            e.message +
            " กรุณาลองกดบันทึกอีกครั้ง";
          error.hidden = false;
          $("#save-post").innerHTML = icon("check") + " บันทึกอีกครั้ง";
          history.replaceState(null, "", `/posts/${id}/edit`);
          return;
        }
      }
      location.href = "/posts/" + id;
    });
  };
}
function reservationCard(r) {
  const p = r.post,
    active = r.status === "RESERVED";
  return `<article class="reservation-card panel"><div class="reservation-main"><a href="/posts/${p.id}" class="reservation-thumb">${photo(p)}</a><div class="reservation-summary">${badge(r.status)}<h2>${escape(p.title)}</h2><p>${icon("pin")}${escape(p.pickupLocationName)}</p><p>${icon("clock")}${escape(pickupWindow(p))}</p><p>ผู้แบ่งปัน: ${escape(p.ownerName)}</p></div><div class="reservation-quantity"><strong>${r.quantity}</strong>${escape(p.unit)}</div></div><div class="reservation-bottom">${active ? `<div class="pickup-code">แสดงรหัสนี้ให้ผู้แบ่งปัน<strong>${escape(r.pickupCode)}</strong></div>` : `<span class="field-note">เลขอ้างอิง FS-${r.id}</span>`}<div class="row-actions">${active ? `<a class="btn btn-primary btn-sm" href="${directions(p)}" target="_blank" rel="noopener">${icon("map")} เส้นทาง</a><button class="btn btn-white btn-sm" data-change="${r.id}" data-quantity="${r.quantity}" data-max="${p.availableQuantity + r.quantity}">แก้จำนวน</button><button class="btn btn-white btn-sm" data-cancel="${r.id}">ยกเลิก</button>` : ""}</div></div></article>`;
}
async function reservations(page = 0) {
  const el = $("#reservation-list");
  try {
    const d = await api("/api/v1/me/reservations?page=" + page);
    el.innerHTML = d.items.length
      ? d.items.map(reservationCard).join("")
      : empty("ยังไม่มีมื้อที่จองไว้", "ไปเลือกอาหารดี ๆ จากเพื่อนในชุมชนกัน");
    paginate(d, reservations);
    $$("[data-cancel]", el).forEach(
      (b) =>
        (b.onclick = () =>
          busy(b, async () => {
            if (
              !(await ask(
                "ยกเลิกการจองนี้?",
                "จำนวนอาหารจะคืนให้เพื่อนคนอื่นจองได้",
              ))
            )
              return;
            await api("/api/v1/reservations/" + b.dataset.cancel, {
              method: "DELETE",
            });
            toast("ยกเลิกการจองเรียบร้อย");
            await reservations(page);
          })),
    );
    $$("[data-change]", el).forEach(
      (b) =>
        (b.onclick = () =>
          busy(b, async () => {
            const q = await ask(
              "แก้ไขจำนวนที่จอง",
              "เลือกจำนวนที่คุณต้องการรับ",
              {
                label: "จำนวน",
                type: "number",
                value: b.dataset.quantity,
                min: 1,
                max: Number(b.dataset.max),
              },
            );
            if (q === null) return;
            await api("/api/v1/reservations/" + b.dataset.change, {
              method: "PUT",
              body: { quantity: Number(q) },
            });
            toast("บันทึกจำนวนใหม่แล้ว");
            await reservations(page);
          })),
    );
  } catch (e) {
    errorBox(el, e, () => reservations(page));
  }
}
async function myPosts(page = 0) {
  const el = $("#my-posts");
  try {
    const d = await api("/api/v1/me/posts?page=" + page);
    el.innerHTML = d.items.length
      ? d.items
          .map(
            (p) =>
              `<article class="managed-card panel"><div class="managed-overview"><a class="reservation-thumb" href="/posts/${p.id}">${photo(p)}</a><div class="managed-info">${badge(p.status)}<h2>${escape(p.title)}</h2><p>${escape(pickupWindow(p))}</p><p>${icon("pin")} ${escape(p.pickupLocationName)}</p></div><div class="managed-counts"><span><strong>${p.availableQuantity}</strong>ยังจองได้</span><span><strong>${p.reservedQuantity}</strong>รอมารับ</span><span><strong>${p.collectedQuantity}</strong>รับแล้ว</span></div></div><div class="managed-toolbar"><button class="btn btn-soft btn-sm" data-bookings="${p.id}">${icon("bag")} ดูผู้จอง / ยืนยันรับ</button><div class="row-actions">${!["CANCELLED", "EXPIRED"].includes(p.status) ? `<a class="btn btn-white btn-sm" href="/posts/${p.id}/edit">${icon("edit")} แก้ไข</a><button class="btn btn-white btn-sm" data-close="${p.id}">${icon("close")} ปิดโพสต์</button>` : ""}</div></div><div class="booking-list" id="bookings-${p.id}" hidden></div></article>`,
          )
          .join("")
      : empty(
          "การแบ่งปันเริ่มจากคุณ",
          "มีอาหารที่ยังทานได้อยู่ไหม? ลองสร้างโพสต์แรกกัน",
          "/posts/new",
          "แบ่งปันอาหาร",
        );
    paginate(d, myPosts);
    $$("[data-close]", el).forEach(
      (b) =>
        (b.onclick = () =>
          busy(b, async () => {
            if (
              !(await ask(
                "ปิดโพสต์อาหารนี้?",
                "การจองที่ยังไม่รับจะถูกยกเลิก และผู้จองจะได้รับการแจ้งเตือน",
              ))
            )
              return;
            await api("/api/v1/food-posts/" + b.dataset.close, {
              method: "DELETE",
            });
            toast("ปิดโพสต์แล้ว");
            await myPosts(page);
          })),
    );
    $$("[data-bookings]", el).forEach(
      (b) =>
        (b.onclick = () =>
          busy(b, async () => {
            const box = $("#bookings-" + b.dataset.bookings);
            if (!box.hidden) {
              box.hidden = true;
              return;
            }
            box.hidden = false;
            box.innerHTML = '<p class="field-note">กำลังโหลดผู้จอง…</p>';
            const rows = await api(
              "/api/v1/food-posts/" + b.dataset.bookings + "/reservations",
            );
            box.innerHTML = rows.length
              ? rows
                  .map(
                    (r) =>
                      `<div class="booking-row"><div><strong>${escape(r.memberName)}</strong><small>${r.quantity} ${escape(r.post.unit)} · FS-${r.id}</small></div>${badge(r.status)}${r.status === "RESERVED" ? `<button class="btn btn-primary btn-sm" data-collect="${r.id}">${icon("check")} ยืนยันส่งมอบ</button>` : ""}</div>`,
                  )
                  .join("")
              : '<p class="field-note">ยังไม่มีผู้จองในโพสต์นี้</p>';
            $$("[data-collect]", box).forEach(
              (button) =>
                (button.onclick = () =>
                  busy(button, async () => {
                    const code = await ask(
                      "ยืนยันการส่งมอบอาหาร",
                      "ขอรหัส 6 หลักจากหน้าการจองของผู้รับ แล้วกรอกด้านล่าง",
                      {
                        label: "รหัสรับอาหาร",
                        numeric: true,
                        maxLength: 6,
                        pattern: "[0-9]{6}",
                        confirm: "ยืนยันรับแล้ว",
                      },
                    );
                    if (code === null) return;
                    await api(
                      "/api/v1/reservations/" +
                        button.dataset.collect +
                        "/collection",
                      { method: "POST", body: { code } },
                    );
                    toast("ยืนยันส่งมอบแล้ว ขอบคุณที่แบ่งปัน");
                    await myPosts(page);
                  })),
            );
          })),
    );
  } catch (e) {
    errorBox(el, e, () => myPosts(page));
  }
}
async function notifications(page = 0) {
  const el = $("#notification-list");
  try {
    const d = await api("/api/v1/me/notifications?page=" + page);
    el.innerHTML = d.items.length
      ? d.items
          .map(
            (n) =>
              `<article class="notification-card ${n.read ? "" : "unread"}"><span class="notification-icon">${icon("bell")}</span><div><h3>${escape(n.title)}</h3><p>${escape(n.message)}</p><time>${escape(dateTime(n.createdAt))}</time></div><a href="${escape(n.href)}" data-read="${n.id}">ดูรายการ →</a></article>`,
          )
          .join("")
      : empty(
          "ยังไม่มีการแจ้งเตือน",
          "เมื่อมีความคืบหน้าการจอง เราจะแจ้งให้คุณทราบที่นี่",
        );
    paginate(d, notifications);
    $$("[data-read]", el).forEach(
      (a) =>
        (a.onclick = async (e) => {
          e.preventDefault();
          try {
            await api("/api/v1/me/notifications/" + a.dataset.read + "/read", {
              method: "POST",
            });
            location.href = a.getAttribute("href");
          } catch (e) {
            toast(e.message, true);
          }
        }),
    );
  } catch (e) {
    errorBox(el, e, () => notifications(page));
  }
}
function account() {
  const f = $("#profile-form");
  f.onsubmit = (e) => {
    e.preventDefault();
    busy($("button", f), async () => {
      await api("/api/v1/me/profile", {
        method: "PATCH",
        body: { name: f.elements.name.value },
      });
      toast("บันทึกชื่อเรียบร้อย");
      setTimeout(() => location.reload(), 700);
    });
  };
}
async function admin() {
  let tab = "reports";
  async function load(page = 0) {
    const el = $("#admin-content");
    try {
      const d = await api("/api/v1/admin/" + tab + "?page=" + page);
      if (tab === "reports") {
        el.innerHTML = d.items.length
          ? d.items
              .map(
                (r) =>
                  `<article class="panel admin-card">${badge(r.status)}<h3 style="margin-top:9px">${escape(r.title)}</h3><p>${escape(r.reason)}</p><p class="field-note">ผู้รายงาน: ${escape(r.reporter)} · ${escape(dateTime(r.createdAt))}</p>${r.status === "OPEN" ? `<div class="row-actions"><a href="/posts/${r.postId}" class="btn btn-white btn-sm">ดูโพสต์</a><button class="btn btn-soft btn-sm" data-resolve="${r.id}">จบการตรวจสอบ</button><button class="btn btn-danger btn-sm" data-moderate="${r.id}">ปิดโพสต์นี้</button></div>` : `<p>ผลการตรวจ: ${escape(r.resolution)}</p>`}</article>`,
              )
              .join("")
          : empty("ไม่มีรายงานที่ต้องจัดการ", "ขอบคุณที่ช่วยดูแลชุมชน", null);
        $$("[data-resolve],[data-moderate]", el).forEach(
          (b) =>
            (b.onclick = () =>
              busy(b, async () => {
                const close = !!b.dataset.moderate;
                const reason = await ask(
                  close ? "ปิดโพสต์ที่ถูกรายงาน?" : "บันทึกผลการตรวจสอบ",
                  close
                    ? "การจองที่ยังไม่รับจะถูกยกเลิก"
                    : "ระบุเหตุผลเพื่อเก็บเป็นหลักฐานการดำเนินการ",
                  { label: "เหตุผล", maxLength: 1000 },
                );
                if (!reason) return;
                await api(
                  "/api/v1/admin/reports/" +
                    (b.dataset.moderate || b.dataset.resolve),
                  { method: "PATCH", body: { reason, closePost: close } },
                );
                toast("บันทึกผลการตรวจแล้ว");
                load(page);
              })),
        );
      } else {
        el.innerHTML = d.items
          .map(
            (u) =>
              `<article class="panel admin-card admin-user"><span class="notification-icon">${icon("user")}</span><div class="user-info"><strong>${escape(u.name)}</strong><small>${escape(u.email)} · ${escape(u.role)}</small></div><span class="badge ${u.active ? "" : "gray"}">${u.active ? "ใช้งานอยู่" : "ระงับแล้ว"}</span><button class="btn btn-white btn-sm" data-user="${u.id}" data-active="${!u.active}">${u.active ? "ระงับบัญชี" : "เปิดใช้งาน"}</button></article>`,
          )
          .join("");
        $$("[data-user]", el).forEach(
          (b) =>
            (b.onclick = () =>
              busy(b, async () => {
                const active = b.dataset.active === "true",
                  reason = await ask(
                    active ? "เปิดใช้บัญชีนี้?" : "ระงับบัญชีนี้?",
                    active
                      ? "บัญชีจะกลับมาเข้าใช้งานได้"
                      : "โพสต์ที่ยังเปิดรับของบัญชีนี้จะถูกปิด",
                    { label: "เหตุผล", maxLength: 1000 },
                  );
                if (!reason) return;
                await api("/api/v1/admin/users/" + b.dataset.user, {
                  method: "PATCH",
                  body: { active, reason },
                });
                toast("บันทึกสถานะบัญชีแล้ว");
                load(page);
              })),
        );
      }
      paginate(d, load);
    } catch (e) {
      errorBox(el, e, () => load(page));
    }
  }
  $$("[data-admin-tab]").forEach(
    (b) =>
      (b.onclick = () => {
        tab = b.dataset.adminTab;
        $$("[data-admin-tab]").forEach((x) =>
          x.classList.toggle("active", x === b),
        );
        load();
      }),
  );
  await load();
}
const boot = {
  home,
  explore,
  dashboard: explore,
  detail,
  editor,
  reservations,
  "my-posts": myPosts,
  notifications,
  account,
  admin,
};
if (boot[page])
  Promise.resolve(boot[page]()).catch((e) => toast(e.message, true));
