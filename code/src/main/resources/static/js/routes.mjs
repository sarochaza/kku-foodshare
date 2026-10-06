import { validCoordinates } from './places.mjs';
const profiles={walking:'foot',driving:'car'};
function valid(origin,post,mode) {
  if(!profiles[mode] || !validCoordinates(origin?.lat,origin?.lng) || !validCoordinates(post?.latitude,post?.longitude)) throw Error('กรุณาเลือกจุดเริ่มต้นและวิธีเดินทางที่ถูกต้อง');
}
export function straightDistance(origin,post) {
  valid(origin,post,'walking');
  const rad=x=>Number(x)*Math.PI/180, dlat=rad(post.latitude-origin.lat),dlng=rad(post.longitude-origin.lng);
  const a=Math.sin(dlat/2)**2+Math.cos(rad(origin.lat))*Math.cos(rad(post.latitude))*Math.sin(dlng/2)**2;
  return 6371000*2*Math.atan2(Math.sqrt(a),Math.sqrt(Math.max(0,1-a)));
}
export function routeUrl(origin,post,mode) {
  valid(origin,post,mode);
  return `https://routing.openstreetmap.de/routed-${profiles[mode]}/route/v1/driving/${Number(origin.lng)},${Number(origin.lat)};${Number(post.longitude)},${Number(post.latitude)}?overview=full&geometries=geojson&steps=false`;
}
export function directionsUrl(origin,post,mode) {
  valid(origin,post,mode);
  return 'https://www.google.com/maps/dir/?'+new URLSearchParams({api:1,origin:`${origin.lat},${origin.lng}`,destination:`${post.latitude},${post.longitude}`,travelmode:mode});
}
export function routeSummary(route) {
  if(!route || !Number.isFinite(route.distance) || route.distance<0 || !Number.isFinite(route.duration) || route.duration<0 || route.geometry?.type!=='LineString' || !Array.isArray(route.geometry.coordinates) || route.geometry.coordinates.length<2 || route.geometry.coordinates.some(p=>!validCoordinates(p[1],p[0]))) throw Error('ไม่พบข้อมูลเส้นทางที่ถูกต้อง');
  return {distance:route.distance,minutes:Math.max(1,Math.ceil(route.duration/60)),coordinates:route.geometry.coordinates.map(([lng,lat])=>[lat,lng])};
}
export function originState() {
  let point=null,source=null,timestamp=0,confirmed=false;
  return {get point(){return point;},get source(){return source;},set(p,kind,now=Date.now()){if(!validCoordinates(p.lat,p.lng)) throw Error('พิกัดไม่ถูกต้อง');point={...p};source=kind;timestamp=now;confirmed=false;},confirm(){if(point)confirmed=true;},stale(now=Date.now()){return !!point && source==='gps' && now-timestamp>=60000;},ready(now=Date.now()){return !!point && confirmed && (source==='manual' || now-timestamp<60000);}};
}
let queue=Promise.resolve(),lastStart=0;
const cache=new Map();
export async function fetchRoute(origin,post,mode,signal,fetcher=fetch) {
  const url=routeUrl(origin,post,mode),cached=cache.get(url);
  if(cached && Date.now()-cached.time<300000) return cached.value;
  // One request per second across widgets; failed/aborted jobs cannot poison the queue.
  const task=queue.catch(()=>{}).then(async()=>{
    if(signal?.aborted) throw Error('ยกเลิกเส้นทางแล้ว');
    const wait=Math.max(0,1100-(Date.now()-lastStart));if(wait)await new Promise(r=>setTimeout(r,wait));
    if(signal?.aborted) throw Error('ยกเลิกเส้นทางแล้ว');lastStart=Date.now();
    const response=await fetcher(url,{signal});if(!response.ok)throw Error('บริการเส้นทางไม่พร้อม');
    const data=await response.json();if(data.code!=='Ok')throw Error('ไม่พบเส้นทาง');
    const value=routeSummary(data.routes?.[0]);
    if(cache.size>=30)cache.delete(cache.keys().next().value);cache.set(url,{time:Date.now(),value});return value;
  });queue=task;return task;
}
export const formatMetres=m=>m<1000?`${Math.round(m)} เมตร`:`${(m/1000).toFixed(1)} กม.`;
