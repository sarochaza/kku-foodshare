import { $, escape } from "./ui.js";
export function createMap(id, onPick = null) {
  if (!window.L) throw Error("โหลดแผนที่ไม่สำเร็จ กรุณาโหลดหน้าใหม่");
  const map = L.map(id, { scrollWheelZoom: false }).setView(
    [16.4745, 102.8237],
    14,
  );
  const tiles = L.tileLayer($('meta[name="tile-url"]').content, {
    maxZoom: 19,
    attribution: $('meta[name="tile-attribution"]').content,
  }).addTo(map);
  const notice = document.createElement("div");
  notice.className = "map-load-error";
  notice.hidden = true;
  notice.setAttribute("role", "status");
  notice.innerHTML =
    '<span>โหลดภาพแผนที่ไม่สำเร็จ ลองใหม่หรือกรอกพิกัดด้านล่าง / เปิดแอปแผนที่</span> <button type="button">ลองโหลดแผนที่อีกครั้ง</button>';
  document.getElementById(id).after(notice);
  tiles.on("tileerror", () => {
    notice.hidden = false;
    const fallback = document.querySelector(".manual-coordinates");
    if (fallback) fallback.open = true;
  });
  notice.querySelector("button").onclick = () => {
    notice.hidden = true;
    tiles.redraw();
  };
  let marker = null;
  const setPin = (lat, lng, zoom = true) => {
    if (marker) marker.setLatLng([lat, lng]);
    else {
      marker = L.marker([lat, lng], { draggable: !!onPick }).addTo(map);
      if (onPick)
        marker.on("dragend", () => {
          const p = marker.getLatLng();
          onPick(p.lat, p.lng);
        });
    }
    if (zoom) map.setView([lat, lng], 16);
  };
  if (onPick)
    map.on("click", (e) => {
      setPin(e.latlng.lat, e.latlng.lng, false);
      onPick(e.latlng.lat, e.latlng.lng);
    });
  setTimeout(() => map.invalidateSize(), 100);
  return { map, setPin };
}
export function locate() {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation)
      return reject(
        Error("อุปกรณ์นี้ไม่รองรับตำแหน่ง กรุณาปักหมุดเองบนแผนที่"),
      );
    navigator.geolocation.getCurrentPosition(
      (p) => resolve({ lat: p.coords.latitude, lng: p.coords.longitude }),
      () => reject(Error("เข้าถึงตำแหน่งไม่ได้ คุณยังเลือกจุดบนแผนที่ได้เอง")),
      { enableHighAccuracy: true, timeout: 12000, maximumAge: 60000 },
    );
  });
}
export function markers(map, posts, oldLayer) {
  if (oldLayer) map.removeLayer(oldLayer);
  const layer = L.layerGroup().addTo(map);
  posts.forEach((p) => {
    const marker = L.marker([p.latitude, p.longitude], {
      icon: L.divIcon({
        className: "food-map-marker",
        html: '<div class="map-pin"><span>🍱</span></div>',
        iconSize: [36, 36],
        iconAnchor: [18, 36],
      }),
    }).addTo(layer);
    const el = document.createElement("div");
    el.className = "map-popup";
    el.innerHTML = `<strong>${escape(p.title)}</strong><span>เหลือ ${p.availableQuantity} ${escape(p.unit)}</span><a href="/posts/${p.id}">ดูรายละเอียด →</a>`;
    marker.bindPopup(el);
  });
  if (posts.length)
    map.fitBounds(
      posts.map((p) => [p.latitude, p.longitude]),
      { padding: [35, 35], maxZoom: 15 },
    );
  return layer;
}
export function directions(p) {
  return `https://www.google.com/maps/dir/?api=1&destination=${encodeURIComponent(p.latitude + "," + p.longitude)}`;
}
