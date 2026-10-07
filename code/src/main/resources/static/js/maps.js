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
const LOCATION_CACHE_KEY = "kku-foodshare-location-v1";
let locationAttempt=0;
function usableLocation(p) {
  return Number.isFinite(Number(p?.lat)) && Number.isFinite(Number(p?.lng)) &&
    Math.abs(Number(p.lat)) <= 90 && Math.abs(Number(p.lng)) <= 180 &&
    Number.isFinite(Number(p?.timestamp));
}
function readLockedLocation() {
  try {
    const p = JSON.parse(sessionStorage.getItem(LOCATION_CACHE_KEY) || "null");
    return usableLocation(p) ? p : null;
  } catch { return null; }
}
function lockLocation(p,persist=true) {
  const result = {lat:Number(p.lat),lng:Number(p.lng),accuracy:Number(p.accuracy),timestamp:Date.now()};
  if(persist) try { sessionStorage.setItem(LOCATION_CACHE_KEY, JSON.stringify(result)); } catch { /* browser storage can be disabled */ }
  return result;
}
function metersBetween(a,b) {
  const radians=x=>x*Math.PI/180, lat1=radians(a.lat),lat2=radians(b.lat),dlat=lat2-lat1,dlng=radians(b.lng-a.lng);
  const h=Math.sin(dlat/2)**2+Math.cos(lat1)*Math.cos(lat2)*Math.sin(dlng/2)**2;
  return 6371000*2*Math.atan2(Math.sqrt(h),Math.sqrt(Math.max(0,1-h)));
}
export function locate({fresh=false}={}) {
  const cached=!fresh && readLockedLocation();
  if (cached) return Promise.resolve({...cached});
  const attempt=++locationAttempt;
  return new Promise((resolve, reject) => {
    const geo=navigator.geolocation;
    if (!geo)
      return reject(
        Error("อุปกรณ์นี้ไม่รองรับตำแหน่ง กรุณาปักหมุดเองบนแผนที่"),
      );
    const options={enableHighAccuracy:true,timeout:12000,maximumAge:0};
    const savePosition=position=>lockLocation({lat:position.coords.latitude,lng:position.coords.longitude,accuracy:position.coords.accuracy},attempt===locationAttempt);
    if (!geo.watchPosition) {
      return geo.getCurrentPosition(p=>resolve(savePosition(p)),()=>reject(Error("เข้าถึงตำแหน่งไม่ได้ คุณยังเลือกจุดบนแผนที่ได้เอง")),options);
    }
    let watchId=null,timeoutId=null,best=null,previousAccurate=null,stableCount=0,settled=false;
    const clear=()=>{clearTimeout(timeoutId);if(watchId!==null)geo.clearWatch?.(watchId);};
    const finish=p=>{if(settled)return;settled=true;clear();resolve(lockLocation(p,attempt===locationAttempt));};
    timeoutId=setTimeout(()=>best ? finish(best) : fail(),8000);
    const fail=()=>{if(settled)return;settled=true;clear();reject(Error("เข้าถึงตำแหน่งไม่ได้ คุณยังเลือกจุดบนแผนที่ได้เอง"));};
    try {
      watchId=geo.watchPosition(position=>{
        const p={lat:position.coords.latitude,lng:position.coords.longitude,accuracy:position.coords.accuracy};
        if (!Number.isFinite(p.lat)||!Number.isFinite(p.lng)||!Number.isFinite(p.accuracy)) return;
        if (!best || p.accuracy<best.accuracy) best=p;
        if (p.accuracy<=40) {
          stableCount=previousAccurate && metersBetween(previousAccurate,p)<=Math.max(25,previousAccurate.accuracy,p.accuracy) ? stableCount+1 : 1;
          previousAccurate=p;
          if (stableCount>=2) finish(best);
        } else { previousAccurate=null;stableCount=0; }
      },()=>best ? finish(best) : fail(),options);
      if (settled && watchId!==null) geo.clearWatch?.(watchId);
    } catch { fail(); }
  });
}
export function markers(map, posts, oldLayer, onSelect = null) {
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
    if (onSelect) marker.on("click", () => onSelect(p));
    else marker.bindPopup(el);
  });
  if (posts.length)
    map.fitBounds(
      posts.map((p) => [p.latitude, p.longitude]),
      { padding: [35, 35], maxZoom: 15 },
    );
  return layer;
}
export function currentLocationMarker(map, coords, oldMarker = null) {
  if (oldMarker) {
    if (oldMarker.accuracyCircle) map.removeLayer(oldMarker.accuracyCircle);
    map.removeLayer(oldMarker);
  }
  const marker = L.marker([coords.lat, coords.lng], {
    zIndexOffset: 1000,
    icon: L.divIcon({
      className: "current-location-marker",
      html: '<span><i></i></span>',
      iconSize: [34, 34],
      iconAnchor: [17, 17],
    }),
  }).addTo(map);
  if (Number.isFinite(coords.accuracy) && coords.accuracy > 0) {
    marker.accuracyCircle = L.circle([coords.lat, coords.lng], {
      radius: coords.accuracy, color: "#347ce2", weight: 1, fillOpacity: 0.08,
      interactive: false,
    }).addTo(map);
  }
  marker.bindTooltip(`ตำแหน่งของคุณ${Number.isFinite(coords.accuracy) ? " · ความคลาดเคลื่อนประมาณ " + Math.round(coords.accuracy) + " เมตร" : ""}`, { direction: "top", offset: [0, -14] });
  return marker;
}
export function directions(p) {
  return `https://www.google.com/maps/dir/?api=1&destination=${encodeURIComponent(p.latitude + "," + p.longitude)}`;
}
