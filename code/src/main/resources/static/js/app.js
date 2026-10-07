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
  feedCard,
  gallery,
  commentButton,
  paginate,
  ask,
} from "./ui.js";
import {
  createMap,
  locate,
  markers,
  currentLocationMarker,
} from "./maps.js";
import { mountTrip } from "./trip.js";
import { mountReservation, mountOwnerStock, mountPostExtension } from "./quick-actions.js";
import { placePicker } from "./places.mjs";
import { initProfileMenu } from "./profile-menu.mjs";
import { initNavigation } from "./navigation.mjs";
import { initImageViewer } from "./image-viewer.mjs";
import { initComments, openPostComments } from "./comments.mjs";
import { sharingMapUrl, mapFeedRequest, focusFoodMap, hasLocation } from "./map-discovery.mjs";
import { initAuthUI } from "./auth.mjs";
import { drawPass, parsePass } from "./pickup.mjs";
import { setDefaultOrigin, fetchRoadDistances, formatMetres } from "./routes.mjs";
const page = document.body.dataset.page;
async function hydrateRoadDistances(posts, origin, isCurrent = () => true) {
  const unique = [...new Map(posts.filter(p => p?.latitude != null && p?.longitude != null).map(p => [p.id, p])).values()].slice(0, 50);
  if (!origin?.lat || !unique.length) return;
  try {
    const distances = await fetchRoadDistances(origin, unique);
    if (!isCurrent()) return;
    unique.forEach((post, index) => {
      post.roadDistanceKm = distances[index] / 1000;
      const label = `ขับรถ ${formatMetres(distances[index])} จากคุณ`;
      $$(`[data-road-distance="${post.id}"]`).forEach(node => { node.textContent = label; });
      $$(`[data-road-distance-preview="${post.id}"]`).forEach(node => { node.textContent = `ระยะทางถนน ${formatMetres(distances[index])}`; });
    });
  } catch {
    if (!isCurrent()) return;
    unique.forEach(post => $$(`[data-road-distance="${post.id}"]`).forEach(node => { node.textContent = "ระยะทางถนนไม่พร้อม"; }));
  }
}
function homeGuide() {
  const shell = $("#home-guide");
  if (!shell) return;
  const key = "foodshare-guide-v2";
  const replay = new URLSearchParams(location.search).has("guide");
  if (!signedIn() && !replay) return;
  try { if (!replay && localStorage.getItem(key)) return; } catch { /* private browsing */ }
  const steps = [
    ["#home-search", "ค้นหามื้อที่ถูกใจ", "พิมพ์ชื่ออาหารหรือจุดรับ แล้วดูรายการที่เพื่อน ๆ แบ่งปัน"],
    ["#home-category-filters", "เลือกสิ่งที่อยากรับ", "กรองอาหาร เครื่องดื่ม ของว่าง หรือเฉพาะรายการที่รับได้ตอนนี้"],
    ["#home-map", "ดูอาหารบนแผนที่สด", "แตะหมุดเพื่อเปิดการ์ดอาหารและดูจุดนัดรับได้ทันที"],
    ["#home-locate", "หามื้อที่ใกล้คุณ", "อนุญาตตำแหน่งเมื่อพร้อม แล้วระบบจะเรียงอาหารตามระยะทางให้"],
    ["#home-food", "เลือกและจองอาหาร", "แตะการ์ดเพื่ออ่านรายละเอียด จำนวนคงเหลือ และเวลานัดรับ"],
    [matchMedia("(max-width: 600px)").matches ? '.mobile-nav a[href="/posts/new"]' : '.hero-buttons a[href="/posts/new"]', "ส่งต่ออาหารดี ๆ", "กดปุ่มบวกเพื่อปักหมุด ถ่ายรูป และแบ่งปันอาหาร"],
  ];
  let index = 0, target;
  const escapeGuide = (event) => { if (event.key === "Escape") close(); };
  const close = () => {
    target?.classList.remove("guide-target"); shell.hidden = true;
    document.removeEventListener("keydown", escapeGuide);
    try { localStorage.setItem(key, "done"); } catch { /* private browsing */ }
  };
  const show = () => {
    target?.classList.remove("guide-target");
    const [selector, title, copy] = steps[index];
    target = $(selector) || (index === 2 ? $("#home-food") : null);
    if (target && getComputedStyle(target).display !== "none" && target.getClientRects().length) {
      target.scrollIntoView({ behavior: "smooth", block: "center" });
      target.classList.add("guide-target");
    }
    $("#guide-title").textContent = title;
    $("#guide-copy").textContent = copy;
    $("#guide-progress").textContent = `${index + 1} / ${steps.length}`;
    $("#guide-back").hidden = index === 0;
    $("#guide-next").textContent = index === steps.length - 1 ? "เสร็จสิ้น" : "ถัดไป";
  };
  $("#guide-skip").onclick = close;
  $("#guide-back").onclick = () => { index--; show(); };
  $("#guide-next").onclick = () => { if (++index === steps.length) close(); else show(); };
  shell.hidden = false; document.addEventListener("keydown", escapeGuide); show();
  $("#guide-next").focus();
}
async function home() {
  const el = $("#home-food"),
    mapList = $("#home-map-list"),
    state = { category: "", sort: "expiry", now: false, page: 0 },
    coords = {};
  let map = null,
    layer = null,
    locationMarker = null,
    mapPosts = [],
    requestSeq = 0,
    locationSeq = 0;

  const query = (size, page = state.page) =>
    new URLSearchParams({
      category: state.category,
      sort: state.sort,
      now: state.now,
      page,
      size,
      ...(coords.lat != null ? {lat: coords.lat, lng: coords.lng} : {}),
    });
  const updateMapEntry = () => {
    $$('[data-sharing-map]').forEach(link => { link.href = sharingMapUrl(state); });
  };
  const ensureMap = () => {
    if (!map) map = createMap("home-live-map").map;
    return map;
  };
  const showPreview = (p) => {
    const preview = $("#home-map-preview");
    preview.innerHTML = `<button type="button" class="map-preview-close" aria-label="ปิดการ์ดอาหาร">×</button><a class="map-preview-photo" href="/posts/${p.id}">${photo(p)}</a><div class="map-preview-copy"><span>${escape(categories[p.category] || "อาหารแบ่งปัน")}</span><h3><a href="/posts/${p.id}">${escape(p.title)}</a></h3><p>${icon("pin")} ${escape(p.pickupLocationName)}</p><small data-road-distance-preview="${p.id}">${p.roadDistanceKm != null ? `ระยะทางถนน ${formatMetres(p.roadDistanceKm*1000)}` : "กำลังคำนวณระยะถนน…"}</small><div><strong>เหลือ ${p.availableQuantity} ${escape(p.unit)}</strong><a href="/posts/${p.id}">ดูรายละเอียด ${icon("arrow")}</a></div></div>`;
    preview.hidden = false;
    $(".map-preview-close", preview).onclick = () => (preview.hidden = true);
  };
  const miniCard = (p) =>
    `<button type="button" class="home-map-item" data-home-post="${p.id}"><span class="home-map-thumb">${photo(p)}</span><span><strong>${escape(p.title)}</strong><small>${icon("pin")} ${escape(p.pickupLocationName)}</small><b data-road-distance="${p.id}">${p.distanceKm != null ? "กำลังคำนวณระยะถนน…" : `เหลือ ${p.availableQuantity} ${escape(p.unit)}`}</b></span></button>`;

  const load = async () => {
    const seq = ++requestSeq;
    updateMapEntry();
    $("#home-map-preview").hidden = true;
    el.innerHTML = '<div class="loading-box">กำลังค้นหาอาหารที่แบ่งปัน…</div>';
    mapList.innerHTML = '<div class="loading-box">กำลังโหลดจุดแบ่งปัน…</div>';
    const [listResult, mapResult] = await Promise.allSettled([
        api("/api/v1/food-posts?" + query(6)),
        api("/api/v1/food-posts?" + query(200, 0)),
    ]);
    if (seq !== requestSeq) return;
    if (listResult.status === "fulfilled") {
      const data = listResult.value;
      $("#home-results-title").textContent = state.category
        ? `${categories[state.category]}ที่พร้อมแบ่งปัน`
        : "มีอาหารดี ๆ รอแบ่งปัน";
      $("#home-results-copy").textContent = `พบ ${data.totalElements} รายการ${state.now ? "ที่รับได้ตอนนี้" : "ในชุมชน KKU"}`;
      el.innerHTML = data.items.length
        ? data.items.map(feedCard).join("")
        : empty(
            "ยังไม่พบมื้อที่ตรงกับตัวกรอง",
            "ลองเลือกหมวดอื่นหรือปิดตัวกรอง “รับได้ตอนนี้” แล้วค้นหาอีกครั้ง",
            "/explore",
            "สำรวจอาหารทั้งหมด",
          );
      wireFeedMenus(el);
      paginate(data, n => { state.page = n; return load(); });
    } else errorBox(el, listResult.reason, load);

    if (mapResult.status === "fulfilled") {
      const data = mapResult.value;
      mapPosts = data.items;
      $("#home-map-count").textContent = `${data.totalElements} จุดแบ่งปัน`;
      mapList.innerHTML = mapPosts.length
        ? mapPosts.slice(0, 4).map(miniCard).join("")
        : empty("ยังไม่มีหมุดในหมวดนี้", "ลองเลือกหมวดอื่นเพื่อดูจุดแบ่งปัน", null);
      try {
        layer = markers(ensureMap(), mapPosts, layer, showPreview);
        if (coords.lat != null) {
          locationMarker = currentLocationMarker(map, coords, locationMarker);
          focusFoodMap(map, mapPosts, coords, state.sort === "nearby");
        }
        map.invalidateSize();
      } catch (e) {
        $("#home-location-status").textContent = e.message;
      }
      $$('[data-home-post]', mapList).forEach((button) => {
        button.onclick = () => {
          const p = mapPosts.find((item) => item.id === Number(button.dataset.homePost));
          if (!p) return;
          ensureMap().setView([p.latitude, p.longitude], 17);
          showPreview(p);
        };
      });
    } else {
      errorBox(mapList, mapResult.reason, load);
      $("#home-map-count").textContent = "โหลดไม่สำเร็จ";
    }
    if (seq === requestSeq && coords.lat != null) {
      const posts = [...(listResult.status === "fulfilled" ? listResult.value.items : []), ...(mapResult.status === "fulfilled" ? mapPosts.slice(0, 4) : [])];
      void hydrateRoadDistances(posts, coords, () => seq === requestSeq);
    }
  };

  const useLocation = async ({fresh=false,silent=false}={}) => {
    const request=++locationSeq;
    try {
      const position=await locate({fresh});
      if(request!==locationSeq) return false;
      Object.assign(coords, position);
      setDefaultOrigin(position);
      state.sort = "nearby";
      state.page = 0;
      $("#home-sort").value = "nearby";
      $("#home-location-status").innerHTML = `${icon("locate")} ระบุตำแหน่งแล้ว${Number.isFinite(coords.accuracy)?` · คลาดเคลื่อนประมาณ ${Math.round(coords.accuracy)} เมตร`:""} · ยึดตำแหน่งนี้ไว้ในแท็บนี้`;
      await load();
      if(!silent) toast("แสดงตำแหน่งของคุณและเรียงอาหารใกล้สุดแล้ว");
      return true;
    } catch (e) {
      if(request!==locationSeq) return false;
      $("#home-location-status").innerHTML = `${icon("pin")} ${escape(e.message)} • ยังดูหมุดอาหารรอบ มข. ได้ตามปกติ`;
      if(!silent) toast(e.message, true);
      return false;
    }
  };

  $$("#home-category-filters [data-home-category]").forEach((button) => {
    button.onclick = () => {
      $$("#home-category-filters [data-home-category]").forEach((item) => {
        const selected = item === button;
        item.classList.toggle("active", selected);
        item.setAttribute("aria-pressed", selected);
      });
      state.category = button.dataset.homeCategory;
      state.page = 0;
      load();
    };
  });
  $("#home-available-now").onchange = (event) => {
    state.now = event.target.checked;
    state.page = 0;
    event.target.closest(".home-category, .home-now-compact")?.classList.toggle("active", state.now);
    load();
  };
  $("#home-sort").onchange = async (event) => {
    state.sort = event.target.value;
    state.page = 0;
    if (state.sort === "nearby" && !hasLocation(coords)) {
      const found = await busy($("#home-locate"), () => useLocation({fresh:true}));
      if (!found) {
        state.sort = "expiry";
        event.target.value = "expiry";
      }
    } else load();
  };
  $("#home-locate").onclick = () => busy($("#home-locate"), () => useLocation({fresh:true}));
  if (signedIn()) void useLocation({silent:true});
  await load();
  homeGuide();
}
function reportPost(postId, commentId = null) {
  return ask("แจ้งปัญหาให้ผู้ดูแล", "เลือกเหตุผล เช่น โพสต์เล่น, ข้อมูลไม่ถูกต้อง, ไม่เหมาะสม, สแปม หรืออื่น ๆ", {label:"เหตุผล", maxLength:1000, confirm:"ส่งรายงาน"}).then(async (reason) => {
    if (!reason) return; await api("/api/v1/reports", {method:"POST", body:{postId:Number(postId), commentId, reason}}); toast("ส่งรายงานให้ผู้ดูแลแล้ว");
  });
}
function wireFeedMenus(root) {
  $$('[data-feed-menu]', root).forEach((button) => button.onclick = () => { const menu=$("#feed-menu-"+button.dataset.feedMenu); $$('[id^="feed-menu-"]',root).forEach(x=>{if(x!==menu)x.hidden=true;}); menu.hidden=!menu.hidden; });
  $$('[data-report-post]', root).forEach((button) => button.onclick = () => busy(button, () => reportPost(button.dataset.reportPost)));
  $$('[data-save-post]', root).forEach((button) => button.onclick = () => busy(button, async () => {
    if (!signedIn()) { location.href = "/login"; return; }
    const isSaved = button.dataset.saved === "true";
    await api(`/api/v1/food-posts/${button.dataset.savePost}/saved`, { method: isSaved ? "DELETE" : "PUT" });
    button.dataset.saved = String(!isSaved);
    button.classList.toggle("is-saved", !isSaved);
    button.setAttribute("aria-pressed", String(!isSaved));
    button.setAttribute("aria-label", !isSaved ? "เลิกบันทึกโพสต์" : "บันทึกโพสต์");
    toast(!isSaved ? "บันทึกโพสต์ไว้แล้ว" : "นำโพสต์ออกจากรายการบันทึกแล้ว");
  }));
}
async function explore() {
  const params = new URLSearchParams(location.search);
  const requestedNearby = params.get("sort") === "nearby";
  let state = {
      q: params.get("q") || "",
      category: params.get("category") || "",
      sort: requestedNearby ? "expiry" : params.get("sort") || "expiry",
    ownership: params.get("ownership") || "",
      page: 0,
      size: 12,
      now: params.get("now") === "true",
    },
    coords = {},
    map = null,
    layer = null,
    locationMarker = null,
    mapVisible = params.get("view") === "map",
    requestSeq = 0,
    mapRequestSeq = 0,
    locationSeq = 0;
  $("#search").value = state.q;
  if ($("#sort")) $("#sort").value = state.sort;
  if ($("#available-now")) $("#available-now").checked = state.now;
  const mapShell = $("#explore-map-shell");
  const mapList = $("#explore-map-list");
  const mapStatus = $("#map-status");
  const mapFullButton = $("#explore-map-full");
  const nearbyStatus = $("#nearby-status"), manualLocation = $("#manual-location-button");
  let manualPick = null;
  const locationMessage = message => { if (nearbyStatus) { nearbyStatus.hidden = false; nearbyStatus.textContent = message; } };
  const showMapPost = p => {
    const preview = $("#explore-map-preview");
    if (!preview || !p) return;
    preview.innerHTML = `<button type="button" class="map-preview-close" aria-label="ปิดการ์ดอาหาร">×</button><a class="map-preview-photo" href="/posts/${p.id}">${photo(p)}</a><div class="map-preview-copy"><span>${escape(categories[p.category] || "อาหารแบ่งปัน")}</span><h3><a href="/posts/${p.id}">${escape(p.title)}</a></h3><p>${icon("pin")} ${escape(p.pickupLocationName)}</p><small data-road-distance-preview="${p.id}">${p.roadDistanceKm != null ? `ระยะทางถนน ${formatMetres(p.roadDistanceKm*1000)}` : "กำลังคำนวณระยะถนน…"}</small><div><strong>เหลือ ${p.availableQuantity} ${escape(p.unit)}</strong><a href="/posts/${p.id}">ดูรายละเอียด ${icon("arrow")}</a></div></div>`;
    preview.hidden = false;
    $(".map-preview-close", preview).onclick = () => { preview.hidden = true; };
  };
  $$("#category-filters [data-category]").forEach(b => b.classList.toggle("active", b.dataset.category === state.category));
  const loadMap = async () => {
    if (!mapVisible) return;
    const seq = ++mapRequestSeq;
    try {
      if (!map) map = createMap("explore-map").map;
      const d = await api(mapFeedRequest(state, coords));
      if (seq !== mapRequestSeq || !mapVisible) return;
      layer = markers(map, d.items, layer, p => showMapPost(p));
      if (mapList) mapList.innerHTML = d.items.slice(0, 5).map(p => `<button type="button" class="explore-map-item" data-explore-post="${p.id}">${p.imageUrl ? `<img src="${escape(p.imageUrl)}" alt="">` : `<span class="map-item-placeholder">${icon("food")}</span>`}<span><strong>${escape(p.title)}</strong><small>${icon("pin")} ${escape(p.pickupLocationName)}</small><b>เหลือ ${p.availableQuantity} ${escape(p.unit)}</b></span></button>`).join("") || "<p class=field-note>ยังไม่มีรายการตามตัวกรองนี้</p>";
      $$('[data-explore-post]', mapList).forEach(button => button.onclick = () => {
        const p = d.items.find(item => String(item.id) === button.dataset.explorePost);
        if (!p) return;
        map.setView([p.latitude, p.longitude], 17);
        showMapPost(p);
      });
      if (coords.lat != null) void hydrateRoadDistances(d.items.slice(0, 5), coords, () => mapVisible);
      if (coords.lat != null) {
        locationMarker = currentLocationMarker(map, coords, locationMarker);
        focusFoodMap(map, d.items, coords, state.sort === "nearby");
      }
      if (mapStatus) mapStatus.textContent =
        `พบ ${d.items.length} จุดแบ่งปัน${state.sort === "nearby" ? " • เรียงหมุดใกล้คุณตามระยะเส้นตรง" : " • เลือกหมุดเพื่อดูรายละเอียด"}${d.totalElements > 200 ? " • แสดง 200 จุดแรก ลองค้นหาให้เจาะจงขึ้น" : ""}`;
      map.invalidateSize();
    } catch (e) {
      if (seq === mapRequestSeq && mapVisible && mapStatus) mapStatus.textContent = e.message;
    }
  };
  const load = async () => {
    const seq = ++requestSeq;
    $("#food-results").innerHTML =
      '<div class="loading-box">กำลังค้นหาอาหาร…</div>';
    try {
      const d = await api(
        "/api/v1/food-posts?" + new URLSearchParams({ ...state, ...(coords.lat != null ? {lat: coords.lat, lng: coords.lng} : {}) }),
      );
      if (seq !== requestSeq) return;
      $("#result-count").textContent =
        `อาหารพร้อมแบ่งปัน ${d.totalElements} รายการ`;
      $("#food-results").innerHTML = d.items.length
        ? d.items.map(feedCard).join("")
        : empty(
            "ยังไม่เจอเมนูที่ค้นหา",
            "ลองเปลี่ยนคำค้นหรือตัวกรอง แล้วกลับมาดูอาหารจากเพื่อน ๆ อีกครั้ง",
            null,
          );
      paginate(d, (n) => {
        state.page = n;
        load();
      });
      wireFeedMenus($("#food-results"));
      if (coords.lat != null) void hydrateRoadDistances(d.items, coords, () => seq === requestSeq);
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
  $$('[data-owner]').forEach(button => button.onclick = () => {
    if (!signedIn() && button.dataset.owner === "mine") { location.href = "/login"; return; }
    state.ownership = button.dataset.owner;
    $$('[data-owner]').forEach(item => item.classList.toggle("active", item.dataset.owner === state.ownership));
    state.page = 0; load();
  });
  $$('[data-owner]').forEach(item => item.classList.toggle("active", item.dataset.owner === state.ownership));
  const nearby = async ({fresh=true}={}) => {
    const request=++locationSeq;
    locationMessage("กำลังหาตำแหน่งของคุณ… หากมีคำขอจากเบราว์เซอร์ ให้กดอนุญาต");
    try {
      const position=await locate({fresh});
      if(request!==locationSeq) return false;
      coords = position;
      mapRequestSeq++;
      setDefaultOrigin(position);
      cancelManualPick();
      toggle(true, {reload:false, resetExpanded:true});
      state.sort = "nearby";
      $("#sort").value = "nearby";
      state.page = 0;
      locationMessage(`พบตำแหน่งแล้ว${Number.isFinite(position.accuracy) ? ` • คลาดเคลื่อนประมาณ ${Math.round(position.accuracy)} เมตร` : ""} • กดใกล้ฉันอีกครั้งเพื่ออัปเดตตำแหน่ง`);
      if (manualLocation) manualLocation.hidden = true;
      await load();
      return true;
    } catch (e) {
      if(request!==locationSeq) return false;
      toast(e.message, true);
      locationMessage(e.message + " หรือกดเลือกจุดของฉันบนแผนที่");
      if (manualLocation) manualLocation.hidden = false;
      if (state.sort === "nearby") {
        state.sort = "expiry";
        $("#sort").value = "expiry";
      }
      return false;
    }
  };
  $("#nearby-button").onclick = () => busy($("#nearby-button"), () => nearby({fresh:true}));
  $("#sort").onchange = (e) => {
    state.sort = e.target.value;
    state.page = 0;
      if (state.sort === "nearby" && !hasLocation(coords)) nearby({fresh:false});
    else load();
  };
  function toggle(show, {reload=true, resetExpanded=false}={}) {
    mapVisible = show;
    if (!show) { mapRequestSeq++; cancelManualPick(); }
    if (mapShell) mapShell.hidden = !show;
    if (resetExpanded) {
      mapShell?.classList.remove("map-expanded");
      if (mapFullButton) { mapFullButton.textContent = "ขยายแผนที่"; mapFullButton.setAttribute("aria-expanded", "false"); }
    }
    $("#map-view")?.classList.toggle("active", show);
    $("#list-view")?.classList.toggle("active", !show);
    $("#map-view")?.setAttribute("aria-pressed", String(show));
    $("#list-view")?.setAttribute("aria-pressed", String(!show));
    const url = new URL(location.href);
    if (show) url.searchParams.set("view", "map");
    else url.searchParams.delete("view");
    history.replaceState(history.state, "", url);
    window.dispatchEvent(new CustomEvent("foodshare:viewchange", { detail: { map: show } }));
    if (show) {
      requestAnimationFrame(() => map?.invalidateSize());
      if (reload) void loadMap();
    }
  }
  $("#map-view")?.addEventListener("click", () => toggle(true, {resetExpanded:true}));
  $("#list-view")?.addEventListener("click", () => toggle(false));
  function cancelManualPick() {
    if (manualPick && map) map.off("click", manualPick);
    manualPick = null;
    if (manualLocation) manualLocation.disabled = false;
    mapShell?.classList.remove("map-picking-origin");
  }
  manualLocation?.addEventListener("click", () => {
    locationSeq++; cancelManualPick(); toggle(true, {resetExpanded:true});
    if (!map) return;
    manualLocation.disabled = true;
    mapShell?.classList.add("map-picking-origin");
    locationMessage("แตะจุดที่คุณอยู่บนแผนที่หนึ่งครั้ง เพื่อเรียงอาหารใกล้จุดนั้น");
    manualPick = async event => {
      locationSeq++; cancelManualPick();
      mapRequestSeq++;
      coords = {lat: event.latlng.lat, lng: event.latlng.lng, source: "manual"};
      setDefaultOrigin(coords); state.sort = "nearby"; state.page = 0; $("#sort").value = "nearby";
      locationMessage("ใช้จุดที่คุณเลือกบนแผนที่ • กดใกล้ฉันเพื่อเปลี่ยนกลับเป็นตำแหน่ง GPS");
      await load();
    };
    map.on("click", manualPick);
    mapShell?.scrollIntoView({block:"center", behavior:"smooth"});
  });
  mapFullButton?.addEventListener("click", () => {
    const expanded = mapShell?.classList.toggle("map-expanded");
    if (mapFullButton) { mapFullButton.textContent = expanded ? "ดูข้อมูล" : "ขยายแผนที่"; mapFullButton.setAttribute("aria-expanded", String(Boolean(expanded))); }
    requestAnimationFrame(() => map?.invalidateSize());
  });
  if (mapShell) mapShell.hidden = !mapVisible;
  $("#map-view")?.classList.toggle("active", mapVisible);
  $("#list-view")?.classList.toggle("active", !mapVisible);
  $("#map-view")?.setAttribute("aria-pressed", String(mapVisible));
  $("#list-view")?.setAttribute("aria-pressed", String(!mapVisible));
  await load();
  if (requestedNearby) { void nearby({fresh:false}); return; }
  const initialLocationRequest=++locationSeq;
  void locate().then(async p=>{if(initialLocationRequest===locationSeq){coords=p;setDefaultOrigin(p);await load();}}).catch(()=>{});
}
async function memberProfile() {
  const id = Number(document.body.dataset.memberId), profile = $("#member-profile"), posts = $("#member-posts");
  const load = async (page = 0) => {
    try {
      const [member, data] = await Promise.all([api(`/api/v1/members/${id}`), api(`/api/v1/members/${id}/posts?page=${page}`)]);
      profile.innerHTML = `<img src="/api/v1/members/${member.id}/photo" alt=""><div><span class="section-kicker">KKU FOODSHARE MEMBER</span><h1>${escape(member.name)}</h1><p>ดูรายการอาหารที่สมาชิกคนนี้เคยแบ่งปัน</p></div>`;
      posts.innerHTML = data.items.length ? data.items.map(feedCard).join("") : empty("ยังไม่มีประวัติการแบ่งปัน", "สมาชิกคนนี้ยังไม่มีโพสต์อาหารที่เปิดให้ดู", null);
      wireFeedMenus(posts); paginate(data, load);
    } catch (e) { errorBox(profile, e, load); }
  };
  await load();
}
async function detail() {
  const id = document.body.dataset.postId,
    el = $("#food-detail");
  try {
    const p = await api("/api/v1/food-posts/" + id);
    const closed = ["CANCELLED", "EXPIRED", "CLAIMED"].includes(p.status);
    el.innerHTML = `<div class="detail-layout"><section class="detail-main"><div class="detail-panel panel detail-food-content"><span class="section-kicker">${escape(categories[p.category])} · FROM OUR COMMUNITY</span><h1 class="detail-title">${escape(p.title)}</h1><div class="detail-meta"><span data-detail-state>${badge(p.status)}</span></div>${gallery(p, "detail-gallery")}<div class="detail-description"><h2>รายละเอียดอาหาร</h2><p>${escape(p.description)}</p><div class="detail-pickup-name"><strong>จุดนัดรับ</strong><span>${icon("pin")} ${escape(p.pickupLocationName)}</span></div>${p.allergens ? `<div class="allergen-note"><strong>ส่วนผสมที่อาจทำให้แพ้</strong><br>${escape(p.allergens)}</div>` : ""}</div></div><div id="detail-trip" class="detail-route-panel"></div></section><aside class="booking-panel panel"><span class="section-kicker">A MEAL MADE FOR SHARING</span><div class="booking-price">แบ่งปันฟรี ♡</div><p>มื้อดี ๆ จากเพื่อนในชุมชน</p><div class="booking-stock"><span>จำนวนที่ยังจองได้</span><span><strong data-detail-stock>${p.availableQuantity}</strong> ${escape(p.unit)}</span></div><div class="pickup-time">${icon("clock")}<div><strong>เวลานัดรับ</strong><span>${escape(pickupWindow(p))}</span></div></div>${p.mine ? `<a class="btn btn-primary full" href="/account/posts">จัดการโพสต์และผู้จอง ${icon("arrow")}</a>${!closed ? `<a class="btn btn-soft full" style="margin-top:10px" href="/posts/${p.id}/edit">${icon("edit")} แก้ไขโพสต์</a>` : ""}` : signedIn() ? '<div id="detail-booking" class="quick-booking"></div>' : `<a class="btn btn-primary full" href="/login">เข้าสู่ระบบเพื่อจองอาหาร ${icon("arrow")}</a>`}<div class="owner-label"><span>${icon("user")}</span><div><small>แบ่งปันโดย</small><strong>${escape(p.ownerName)}</strong></div></div>${signedIn() && !p.mine ? '<button class="report-button" id="report-post">รายงานปัญหาของโพสต์นี้</button>' : ""}</aside></div>`;
    const trip = mountTrip($("#detail-trip"), p);
    trip.refresh();
    const commentsPanel = document.createElement("section");
    commentsPanel.className = "detail-panel panel comments-panel";
    commentsPanel.id = "post-comments";
    commentsPanel.innerHTML = commentButton(p);
    $(".detail-food-content", el).after(commentsPanel);
    if (location.hash === "#post-comments") openPostComments(p, $("[data-open-comments]", commentsPanel));
    if (signedIn() && !p.mine) mountReservation($("#detail-booking"), p, trip, (fresh) => {
      $('[data-detail-stock]').textContent = fresh.availableQuantity;
      $('[data-detail-state]').innerHTML = badge(fresh.status);
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
  let picker;
  try {
    map = createMap("editor-map", (lat, lng) => picker?.pick(lat, lng));
  } catch (e) {
    $("#pin-status").textContent = e.message;
    $(".manual-coordinates").open = true;
  }
  picker = placePicker(form, map, locate);
  const limitToggle = $("#limit-per-person");
  const limitField = $("#per-person-limit-field");
  const maxPerPerson = form.elements.maxPerPerson;
  const syncPerPersonLimit = () => {
    const enabled = limitToggle.checked;
    limitField.hidden = !enabled;
    maxPerPerson.disabled = !enabled;
    maxPerPerson.required = enabled;
    maxPerPerson.max = form.elements.quantity.value || 10000;
    if (!enabled) maxPerPerson.value = "";
  };
  limitToggle.onchange = syncPerPersonLimit;
  form.elements.quantity.oninput = syncPerPersonLimit;
  syncPerPersonLimit();
  $("#food-photo").onchange = (e) => {
    const files = [...e.target.files];
    if (!files.length) return;
    if (files.length > 5 || files.some(file => file.size > 5 * 1024 * 1024 || !["image/jpeg", "image/png"].includes(file.type))) {
      e.target.value = "";
      toast("กรุณาเลือกรูป JPG/PNG ไม่เกิน 5 MB", true);
      return;
    }
    if (previewUrl) URL.revokeObjectURL(previewUrl);
    previewUrl = URL.createObjectURL(files[0]);
    $("#photo-preview").src = previewUrl;
    $("#photo-preview").hidden = false;
    $("#photo-previews").innerHTML = files.map((file, index) => `<span>${index + 1}. ${escape(file.name)}</span>`).join("");
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
      limitToggle.checked = Number.isInteger(p.maxPerPerson);
      if (limitToggle.checked) maxPerPerson.value = p.maxPerPerson;
      syncPerPersonLimit();
      picker.load(p);
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
    if (!picker.validate()) return;
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
      if (data.maxPerPerson !== undefined) data.maxPerPerson = Number(data.maxPerPerson);
      data.latitude = Number(data.latitude);
      data.longitude = Number(data.longitude);
      const saved = await api("/api/v1/food-posts" + (id ? "/" + id : ""), {
        method: id ? "PUT" : "POST",
        body: data,
      });
      id = saved.id;
      const files = [...$("#food-photo").files];
      if (files.length) {
        try {
          for (const file of files) {
            const upload = new FormData(); upload.append("file", file);
            await api(`/api/v1/food-posts/${id}/images`, { method: "POST", body: upload });
          }
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
  const maximum = Math.min(p.availableQuantity + r.quantity, p.maxPerPerson || 10000);
  return `<article class="reservation-card panel pickup-pass" data-reservation-status="${r.status}"><div class="pass-heading"><span>KKU FOODSHARE · PICKUP PASS</span><span>FS-${r.id}</span></div><div class="reservation-main"><a href="/posts/${p.id}" class="reservation-thumb">${photo(p)}</a><div class="reservation-summary">${badge(r.status)}<h2>${escape(p.title)}</h2><p>${icon("pin")}${escape(p.pickupLocationName)}</p><p>${icon("clock")}${escape(pickupWindow(p))}</p><p>ผู้แบ่งปัน: ${escape(p.ownerName)}</p></div><div class="reservation-quantity"><strong>${r.quantity}</strong>${escape(p.unit)}</div></div>${active && r.pickupCode ? `<div class="pass-ticket"><div class="pass-qr-wrap"><svg class="pass-qr" data-pass-id="${r.id}" data-pass-code="${escape(r.pickupCode)}"></svg><span>ให้เจ้าของโพสต์สแกน QR</span></div><div class="pass-code-wrap"><span class="section-kicker">SCAN & SHARE</span><h3>บัตรรับอาหารของคุณ</h3><p>แสดง QR นี้ให้เจ้าของโพสต์สแกนเมื่อมาถึงจุดรับ หรือแจ้งรหัส 6 หลักแทน</p><div class="pickup-code">รหัสสำรอง<strong>${escape(r.pickupCode)}</strong></div></div></div>` : ""}<div class="reservation-bottom">${!active ? `<span class="field-note">เลขอ้างอิง FS-${r.id}</span>` : `<span class="field-note">ใช้ได้เฉพาะการจองนี้ • อย่าแชร์ให้คนอื่น</span>`}<div class="row-actions">${active ? `<a class="btn btn-primary btn-sm" href="/posts/${p.id}">${icon("map")} เส้นทาง / เวลาเดินทาง</a><button class="btn btn-white btn-sm" data-change="${r.id}" data-quantity="${r.quantity}" data-max="${maximum}">แก้จำนวน</button><button class="btn btn-white btn-sm" data-cancel="${r.id}">ยกเลิก</button>` : ""}</div></div></article>`;
}
async function reservations(page = 0) {
  const el = $("#reservation-list");
  let selected = "all";
  const applyFilter = () => {
    $$('[data-reservation-status]', el).forEach(card => {
      const status = card.dataset.reservationStatus;
      card.hidden = selected === "active" ? status !== "RESERVED" : selected === "history" ? status === "RESERVED" : false;
    });
  };
  $$('[data-reservation-filter]').forEach(button => button.onclick = () => {
    selected = button.dataset.reservationFilter;
    $$('[data-reservation-filter]').forEach(item => item.classList.toggle("active", item === button));
    applyFilter();
  });
  try {
    const d = await api("/api/v1/me/reservations?page=" + page);
    el.innerHTML = d.items.length
      ? d.items.map(reservationCard).join("")
      : empty("ยังไม่มีมื้อที่จองไว้", "ไปเลือกอาหารดี ๆ จากเพื่อนในชุมชนกัน");
    applyFilter();
    $$("[data-pass-id]", el).forEach(svg => {
      try { drawPass(svg, Number(svg.dataset.passId), svg.dataset.passCode); }
      catch { svg.replaceWith(document.createTextNode("ใช้รหัส 6 หลักด้านข้างแทน")); }
    });
    $$('[data-trip-reservation]', el).forEach(button => {
      button.onclick = () => {
        const reservation = d.items.find(r => r.id === Number(button.dataset.tripReservation));
        if (!reservation) return;
        const card = button.closest('.reservation-card');
        let panel = card.querySelector('.reservation-trip');
        if (!panel) {
          panel = document.createElement('div'); panel.className = 'reservation-trip'; card.append(panel);
          const widget = mountTrip(panel, reservation.post);
          panel.querySelector('[data-trip-skip]').hidden = true;
          widget.refresh();
        } else {panel.hidden = !panel.hidden;}
        if (!panel.hidden) panel.scrollIntoView({behavior:'smooth', block:'center'});
      };
    });
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
async function savedPosts(page = 0) {
  const el = $("#saved-posts");
  try {
    const d = await api("/api/v1/me/saved-posts?page=" + page);
    el.innerHTML = d.items.length
      ? d.items.map(feedCard).join("")
      : empty("ยังไม่มีโพสต์ที่บันทึก", "กดรูปหัวใจบนโพสต์อาหารที่สนใจ แล้วกลับมาดูได้ที่นี่", "/explore", "ค้นหาอาหาร");
    wireFeedMenus(el); paginate(d, savedPosts);
  } catch (e) { errorBox(el, e, () => savedPosts(page)); }
}
async function manualPickup(id, page) {
  let reservationId = id;
  if (!reservationId) {
    const reference = await ask(
      "ค้นหารายการส่งมอบ",
      "กรอกเฉพาะตัวเลขหลัง FS- เช่น FS-125 ให้กรอก 125",
      { label: "เลขอ้างอิง", numeric: true, maxLength: 12, pattern: "[0-9]+", confirm: "ถัดไป" },
    );
    if (reference === null) return;
    reservationId = Number(reference);
    if (!Number.isSafeInteger(reservationId) || reservationId < 1) {
      toast("เลขอ้างอิงไม่ถูกต้อง", true);
      return;
    }
  }
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
  await api(`/api/v1/reservations/${reservationId}/collection`, {
    method: "POST",
    body: { code },
  });
  toast("ยืนยันส่งมอบแล้ว ขอบคุณที่แบ่งปัน");
  await myPosts(page);
}

function cameraFrame(video, canvas) {
  return new Promise((resolve, reject) => {
    if (!video.videoWidth || !video.videoHeight) {
      reject(new Error("กล้องยังไม่พร้อม"));
      return;
    }
    const scale = Math.min(1, 960 / video.videoWidth);
    canvas.width = Math.round(video.videoWidth * scale);
    canvas.height = Math.round(video.videoHeight * scale);
    canvas.getContext("2d", { alpha: false }).drawImage(video, 0, 0, canvas.width, canvas.height);
    canvas.toBlob(
      (blob) => (blob ? resolve(blob) : reject(new Error("อ่านภาพจากกล้องไม่สำเร็จ"))),
      "image/jpeg",
      0.76,
    );
  });
}

async function decodeQrImage(blob) {
  const form = new FormData();
  form.append("frame", blob, "qr-frame.jpg");
  return (await api("/api/v1/pickup/scan", { method: "POST", body: form })).value;
}

async function scanPickup(expectedId, page) {
  const dialog = $("#scan-dialog"),
    video = $("#scan-video"),
    canvas = $("#scan-canvas"),
    status = $("#scan-status"),
    stage = $("#scan-camera-stage"),
    review = $("#scan-review"),
    actions = $("#scan-review-actions"),
    success = $("#scan-success"),
    imageInput = $("#scan-image");
  if (!dialog) return;

  let stream = null,
    timer = null,
    scanning = false,
    closed = false,
    needsRefresh = false,
    currentPass = null,
    detector = null;

  try {
    if (window.BarcodeDetector) detector = new BarcodeDetector({ formats: ["qr_code"] });
  } catch {
    detector = null;
  }

  const stopCamera = () => {
    clearTimeout(timer);
    timer = null;
    scanning = false;
    stream?.getTracks().forEach((track) => track.stop());
    stream = null;
    video.srcObject = null;
  };
  const close = () => {
    stopCamera();
    if (dialog.open) dialog.close();
  };
  const showReview = async (pass) => {
    stopCamera();
    status.textContent = "พบ QR แล้ว กำลังตรวจสอบข้อมูล…";
    try {
      const r = await api(`/api/v1/reservations/${pass.id}`);
      currentPass = pass;
      $("#scan-member-photo").src = `/api/v1/reservations/${r.id}/member-photo`;
      $("#scan-member-name").textContent = r.memberName;
      $("#scan-reference").textContent = `เลขอ้างอิง FS-${r.id} · จองเมื่อ ${dateTime(r.createdAt)}`;
      $("#scan-post-title").textContent = r.post.title;
      $("#scan-quantity").textContent = `${r.quantity} ${r.post.unit}`;
      $("#scan-location").textContent = r.post.pickupLocationName;
      $("#scan-window").textContent = pickupWindow(r.post);
      stage.hidden = true;
      review.hidden = false;
      actions.hidden = false;
      success.hidden = true;
      $("#scan-confirm").focus();
    } catch (error) {
      currentPass = null;
      stage.hidden = false;
      review.hidden = true;
      status.textContent = error.message;
      timer = setTimeout(startCamera, 1000);
    }
  };
  const acceptValue = async (value) => {
    const pass = parsePass(value, expectedId);
    if (!pass) {
      status.textContent = expectedId
        ? "QR นี้ไม่ตรงกับผู้จองรายการที่เลือก"
        : "QR นี้ไม่ใช่บัตรรับอาหารของ KKU FoodShare";
      return false;
    }
    await showReview(pass);
    return true;
  };
  const scanTick = async () => {
    if (closed || !dialog.open || !stream || scanning || stage.hidden) return;
    scanning = true;
    let found = false;
    try {
      if (detector) {
        const results = await detector.detect(video);
        for (const item of results) {
          if (await acceptValue(item.rawValue)) {
            found = true;
            break;
          }
        }
      } else {
        const value = await decodeQrImage(await cameraFrame(video, canvas));
        found = await acceptValue(value);
      }
    } catch (error) {
      if (error.message && !error.message.includes("ยังไม่พบ QR"))
        status.textContent = error.message;
    } finally {
      scanning = false;
    }
    if (!found && dialog.open && stream && !stage.hidden)
      timer = setTimeout(scanTick, detector ? 240 : 650);
  };
  const startCamera = async () => {
    stopCamera();
    currentPass = null;
    stage.hidden = false;
    review.hidden = true;
    actions.hidden = false;
    success.hidden = true;
    imageInput.value = "";
    status.textContent = "กำลังเปิดกล้อง…";
    if (!navigator.mediaDevices?.getUserMedia) {
      status.textContent = "เบราว์เซอร์นี้เปิดกล้องไม่ได้ เลือกรูป QR หรือกรอกรหัสแทนได้";
      return;
    }
    try {
      stream = await navigator.mediaDevices.getUserMedia({
        video: {
          facingMode: { ideal: "environment" },
          width: { ideal: 1280 },
          height: { ideal: 720 },
        },
        audio: false,
      });
      if (closed || !dialog.open) {
        stream.getTracks().forEach((track) => track.stop());
        return;
      }
      video.srcObject = stream;
      await video.play();
      status.textContent = detector
        ? "เล็ง QR ให้อยู่ในกรอบ ระบบจะอ่านให้อัตโนมัติ"
        : "เล็ง QR ให้อยู่ในกรอบ ระบบกำลังอ่านภาพจากกล้อง";
      scanTick();
    } catch {
      stopCamera();
      status.textContent = "เปิดกล้องไม่ได้ กรุณาอนุญาตกล้อง เลือกรูป QR หรือกรอกรหัสแทน";
    }
  };

  dialog.onclose = () => {
    closed = true;
    stopCamera();
    dialog.onclose = null;
    if (needsRefresh) myPosts(page);
  };
  $("#scan-close").onclick = close;
  $("#scan-fallback").onclick = () => {
    close();
    manualPickup(expectedId, page).catch((error) => toast(error.message, true));
  };
  imageInput.onchange = async () => {
    const file = imageInput.files?.[0];
    if (!file) return;
    imageInput.value = "";
    stopCamera();
    status.textContent = "กำลังอ่าน QR จากรูป…";
    try {
      await acceptValue(await decodeQrImage(file));
    } catch (error) {
      stage.hidden = false;
      review.hidden = true;
      status.textContent = error.message;
    }
  };
  $("#scan-retry").onclick = startCamera;
  $("#scan-confirm").onclick = () =>
    busy($("#scan-confirm"), async () => {
      if (!currentPass) return;
      await api(`/api/v1/reservations/${currentPass.id}/collection`, {
        method: "POST",
        body: { code: currentPass.code },
      });
      needsRefresh = true;
      actions.hidden = true;
      success.hidden = false;
      $("#scan-done").focus();
      toast("ยืนยันส่งมอบแล้ว ขอบคุณที่แบ่งปัน");
    });
  $("#scan-done").onclick = close;
  dialog.showModal();
  await startCamera();
}

let ownerFilter = "all";
const ownerPostOpen = (p) => ["AVAILABLE", "LOW_STOCK", "SCHEDULED", "FULL"].includes(p.status);
const ownerMatches = (p, filter) =>
  filter === "all" ||
  (filter === "waiting" && p.reservedQuantity > 0) ||
  (filter === "open" && ownerPostOpen(p)) ||
  (filter === "done" && !ownerPostOpen(p));

function bookingRow(r) {
  const statusCopy = {
    RESERVED: "รอยืนยันการส่งมอบ",
    COLLECTED: "ส่งมอบเรียบร้อย",
    CANCELLED: "ผู้รับยกเลิกแล้ว",
    EXPIRED: "เลยเวลารับอาหาร",
  }[r.status] || r.status;
  return `<article class="booking-row" data-booking-row="${r.id}">
    <div class="booking-person">
      <img class="booking-avatar" src="/api/v1/reservations/${r.id}/member-photo" alt="รูปโปรไฟล์ ${escape(r.memberName)}" loading="lazy">
      <div><strong>${escape(r.memberName)}</strong><small>FS-${r.id} · จองเมื่อ ${escape(dateTime(r.createdAt))}</small></div>
    </div>
    <div class="booking-state">${badge(r.status)}<small>${escape(statusCopy)}</small></div>
    <div class="booking-details">
      <span><small>จำนวน</small><strong>${r.quantity} ${escape(r.post.unit)}</strong></span>
      <span><small>เวลานัดรับ</small><strong>${escape(pickupWindow(r.post))}</strong></span>
    </div>
    ${r.status === "RESERVED" ? `<div class="row-actions booking-actions"><button class="btn btn-primary btn-sm" data-scan="${r.id}">${icon("camera")} สแกน QR</button><button class="btn btn-white btn-sm" data-collect="${r.id}">กรอกรหัส</button></div>` : ""}
  </article>`;
}

function wireBookingActions(box, page) {
  $$('[data-collect]', box).forEach((button) =>
    (button.onclick = () =>
      busy(button, () => manualPickup(Number(button.dataset.collect), page))),
  );
  $$('[data-scan]', box).forEach((button) =>
    (button.onclick = () => scanPickup(Number(button.dataset.scan), page)),
  );
}
async function myPosts(page = 0) {
  const el = $("#my-posts");
  try {
    const d = await api("/api/v1/me/posts?page=" + page);
    el.innerHTML = d.items.length
      ? d.items
          .map(
            (p) => {
              const progress = p.quantity
                ? Math.round(((p.collectedQuantity + p.offlineQuantity) / p.quantity) * 100)
                : 0;
              return `<article class="managed-card panel" data-owner-card data-owner-status="${p.status}" data-owner-waiting="${p.reservedQuantity}" ${ownerMatches(p, ownerFilter) ? "" : "hidden"}>
                <div class="managed-overview"><a class="reservation-thumb" href="/posts/${p.id}">${photo(p)}</a><div class="managed-info">${badge(p.status)}<h2>${escape(p.title)}</h2><p>${icon("clock")} ${escape(pickupWindow(p))}</p><p>${icon("pin")} ${escape(p.pickupLocationName)}</p>${p.maxPerPerson ? `<p class="field-note">จำกัดคนละไม่เกิน ${p.maxPerPerson} ${escape(p.unit)}</p>` : ""}</div><div class="managed-counts"><span><strong>${p.availableQuantity}</strong>ยังจองได้</span><span><strong>${p.reservedQuantity}</strong>รอมารับ</span><span><strong>${p.collectedQuantity}</strong>รับผ่านเว็บ</span><span><strong>${p.offlineQuantity}</strong>แจกนอกเว็บ</span></div></div>
                <div class="managed-progress"><div><span>ความคืบหน้าการส่งมอบ</span><strong>${p.collectedQuantity + p.offlineQuantity} / ${p.quantity} ${escape(p.unit)}</strong></div><progress value="${p.collectedQuantity + p.offlineQuantity}" max="${Math.max(1, p.quantity)}">${progress}%</progress></div>
                ${p.status === "EXPIRED" && p.availableQuantity > 0 ? `<section class="expired-nudge"><strong>เวลารับหมดแล้ว แต่ยังเหลือ ${p.availableQuantity} ${escape(p.unit)}</strong><span>ต้องการขยายเวลารับต่อไหม?</span><button type="button" class="btn btn-green btn-sm" data-extend="${p.id}">${icon("clock")} ขยายเวลารับ</button></section>` : ""}
                <div class="managed-toolbar">${!["CANCELLED", "EXPIRED"].includes(p.status) && new Date(p.availableUntil + "+07:00") > new Date() ? `<button type="button" class="btn btn-green btn-sm" data-stock="${p.id}" aria-expanded="false" aria-controls="stock-${p.id}">${icon("edit")} จัดการจำนวน</button>` : ""}<button class="btn btn-soft btn-sm" data-bookings="${p.id}">${icon("bag")} ผู้จอง ${p.reservedQuantity ? `(${p.reservedQuantity} รอรับ)` : ""}</button><div class="row-actions">${!["CANCELLED", "EXPIRED", "CLAIMED"].includes(p.status) ? `<a class="btn btn-white btn-sm" href="/posts/${p.id}/edit">${icon("edit")} แก้ไข</a><button class="btn btn-white btn-sm" data-close="${p.id}">${icon("close")} ปิดโพสต์</button>` : ""}</div></div><section class="owner-extension" id="extension-${p.id}" hidden></section><section class="owner-stock" id="stock-${p.id}" hidden></section><div class="booking-list" id="bookings-${p.id}" hidden></div>
              </article>`;
            },
          )
          .join("")
      : empty(
          "การแบ่งปันเริ่มจากคุณ",
          "มีอาหารที่ยังทานได้อยู่ไหม? ลองสร้างโพสต์แรกกัน",
          "/posts/new",
          "แบ่งปันอาหาร",
        );
    $("#owner-total-posts").textContent = d.totalElements;
    api("/api/v1/me/posts/management-summary")
      .then((summary) => {
        $("#owner-total-posts").textContent = summary.totalPosts;
        $("#owner-open-posts").textContent = summary.openPosts;
        $("#owner-waiting-count").textContent = summary.waitingCount;
        $("#owner-collected-count").textContent = summary.collectedCount + summary.offlineCount;
      })
      .catch(() => {
        $("#owner-open-posts").textContent = d.items.filter(ownerPostOpen).length;
        $("#owner-waiting-count").textContent = d.items.reduce((sum, p) => sum + p.reservedQuantity, 0);
        $("#owner-collected-count").textContent = d.items.reduce((sum, p) => sum + p.collectedQuantity + p.offlineQuantity, 0);
      });
    paginate(d, myPosts);
    $$('[data-stock]', el).forEach((button) => {
      let widget;
      button.onclick = () => {
        const box = $("#stock-" + button.dataset.stock);
        box.hidden = !box.hidden;
        button.setAttribute('aria-expanded', String(!box.hidden));
        if (!box.hidden) {
          if (!widget) widget = mountOwnerStock(box, Number(button.dataset.stock), () => myPosts(page));
          else widget.refresh();
          box.scrollIntoView({behavior:'smooth', block:'nearest'});
        }
      };
    });
    $$('[data-extend]', el).forEach((button) => {
      let widget;
      button.onclick = () => {
        const post = d.items.find((item) => item.id === Number(button.dataset.extend));
        const box = $("#extension-" + button.dataset.extend);
        box.hidden = false;
        if (!widget) widget = mountPostExtension(box, post, () => myPosts(page));
        box.scrollIntoView({behavior:'smooth', block:'nearest'});
      };
    });
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
              ? rows.map(bookingRow).join("")
              : '<p class="field-note">ยังไม่มีผู้จองในโพสต์นี้</p>';
            wireBookingActions(box, page);
          })),
    );
    $$('[data-owner-filter]').forEach((button) => {
      button.classList.toggle("active", button.dataset.ownerFilter === ownerFilter);
      button.onclick = () => {
        ownerFilter = button.dataset.ownerFilter;
        $$('[data-owner-filter]').forEach((item) =>
          item.classList.toggle("active", item === button),
        );
        d.items.forEach((post) => {
          const card = $(`[data-bookings="${post.id}"]`, el)?.closest('[data-owner-card]');
          if (card) card.hidden = !ownerMatches(post, ownerFilter);
        });
      };
    });
    const openGlobalScanner = () => scanPickup(null, page);
    $("#owner-scan-any").onclick = openGlobalScanner;
    $("#owner-mobile-scan").onclick = openGlobalScanner;
    $("#owner-show-waiting").onclick = () => {
      $('[data-owner-filter="waiting"]').click();
      const first = $('[data-owner-card]:not([hidden]) [data-bookings]', el);
      if (first) {
        first.scrollIntoView({ behavior: "smooth", block: "center" });
        if ($(`#bookings-${first.dataset.bookings}`).hidden) first.click();
      } else toast("ตอนนี้ยังไม่มีผู้จองที่รอมารับ");
    };
  } catch (e) {
    errorBox(el, e, () => myPosts(page));
  }
}
async function notifications(page = 0) {
  const el = $("#notification-list");
  const notificationLabels = {RESERVATION:"การจอง", COMMENT:"คอมเมนต์", INTEREST:"อาหารที่สนใจ", UPDATE:"อัปเดต"};
  let selectedType = "ALL";
  try {
    const preferences = await api("/api/v1/me/notification-preferences");
    const preferenceForm = $("#notification-preference-form");
    if (preferenceForm) {
      preferenceForm.elements.keywords.value = preferences.keywords || "";
      $$("input[name=categories]", preferenceForm).forEach(input => input.checked = preferences.categories?.includes(input.value));
      preferenceForm.onsubmit = e => { e.preventDefault(); busy($("button", preferenceForm), async () => { await api("/api/v1/me/notification-preferences", {method:"PUT", body:{categories: $$('input[name=categories]:checked', preferenceForm).map(i=>i.value), keywords: preferenceForm.elements.keywords.value}}); toast("บันทึกความสนใจแล้ว"); }); };
    }
    const d = await api("/api/v1/me/notifications?page=" + page);
    const render = () => {
      const items = selectedType === "ALL" ? d.items : d.items.filter(n => n.type === selectedType);
      el.innerHTML = items.length
      ? items
          .map(
            (n) =>
              `<article class="notification-card ${n.read ? "" : "unread"}" data-type="${escape(n.type || "UPDATE")}"><img class="notification-avatar" src="${n.actorId ? `/api/v1/members/${n.actorId}/photo` : "/images/default-profile.png"}" alt=""><div class="notification-copy"><div class="notification-heading"><h3>${escape(n.actorName || n.title)}</h3><span class="notification-type-label">${escape(notificationLabels[n.type] || notificationLabels.UPDATE)}</span></div><p>${escape(n.actorName ? n.message : n.title + " · " + n.message)}</p><time>${escape(dateTime(n.createdAt))}</time></div><a href="${escape(n.href)}" data-read="${n.id}" aria-label="เปิดการแจ้งเตือน">เปิด</a></article>`,
          ).join("")
      : empty(
          selectedType === "ALL" ? "ยังไม่มีการแจ้งเตือน" : "ยังไม่มีรายการประเภทนี้",
          "เมื่อมีความคืบหน้าการจองหรือการแบ่งปัน เราจะแจ้งให้คุณทราบที่นี่",
        );
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
    };
    $$("[data-notification-type]").forEach(button => button.onclick = () => { selectedType = button.dataset.notificationType; $$("[data-notification-type]").forEach(item => { const active = item === button; item.classList.toggle("active", active); item.setAttribute("aria-pressed", active); }); render(); });
    render();
    paginate(d, notifications);
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
  "member-profile": memberProfile,
  saved: savedPosts,
  notifications,
  account,
  admin,
};
initProfileMenu();
initNavigation();
initImageViewer();
initComments();
initAuthUI();
if (boot[page])
  Promise.resolve(boot[page]()).catch((e) => toast(e.message, true));
