// Local suggestions keep common KKU places instant and work without a paid API.
const BASE = 'https://photon.komoot.io';
const searchCache=new Map();
const KKU_CENTER={lat:16.4745,lng:102.8237};
// Keep KKU results at the top while allowing the rest of central Khon Kaen.
const SEARCH_RADIUS_KM=25;
const KKU_SEARCH_BBOX='102.660,16.350,103.000,16.610';
const LOCAL_PLACES=Object.freeze([
  {name:'มหาวิทยาลัยขอนแก่น',label:'มหาวิทยาลัยขอนแก่น · ตำบลในเมือง · ขอนแก่น',lat:16.4745,lng:102.8237,aliases:['มข','มข.','มหาวิทยาลัยขอนแก่น','khon kaen university','kku']},
  {name:'หอสมุดกลาง มหาวิทยาลัยขอนแก่น',label:'หอสมุดกลาง · มหาวิทยาลัยขอนแก่น',lat:16.4768,lng:102.82325,aliases:['หอสมุด','หอสมุดกลาง','library','central library','kkulib']},
  {name:'ศูนย์อาหารและบริการ 1 (คอมเพล็กซ์)',label:'คอมเพล็กซ์ · มหาวิทยาลัยขอนแก่น',lat:16.4752,lng:102.8219,aliases:['คอมเพล็กซ์','complex','ศูนย์อาหาร','food court']},
  {name:'อาคารพจน์ สารสิน',label:'อาคารพจน์ สารสิน · มหาวิทยาลัยขอนแก่น',lat:16.4740,lng:102.8209,aliases:['พจน์ สารสิน','พจน์','สารสิน','pote sarasin']},
  {name:'โรงพยาบาลศรีนครินทร์',label:'โรงพยาบาลศรีนครินทร์ · มหาวิทยาลัยขอนแก่น',lat:16.4708,lng:102.8257,aliases:['ศรีนครินทร์','โรงพยาบาลศรีนครินทร์','srinagarind hospital','hospital']},
  {name:'อาคารเรียนรวมและพัฒนาทักษะพื้นฐาน',label:'อาคารเรียนรวม · มหาวิทยาลัยขอนแก่น',lat:16.4749,lng:102.8194,aliases:['อาคารเรียนรวม','เรียนรวม','complex building']},
  {name:'บึงสีฐาน มหาวิทยาลัยขอนแก่น',label:'บึงสีฐาน · มหาวิทยาลัยขอนแก่น',lat:16.4687,lng:102.8165,aliases:['บึงสีฐาน','สีฐาน','bueng si than']},
  {name:'ประตูสีฐาน มหาวิทยาลัยขอนแก่น',label:'ประตูสีฐาน · มหาวิทยาลัยขอนแก่น',lat:16.4669,lng:102.8161,aliases:['ประตูสีฐาน','ประตู มข','sithan gate']},
  {name:'เซ็นทรัล ขอนแก่น',label:'เซ็นทรัล ขอนแก่น · เมืองขอนแก่น',lat:16.4304,lng:102.8284,aliases:['เซ็นทรัล','central khon kaen','central']},
  {name:'บึงแก่นนคร',label:'บึงแก่นนคร · เมืองขอนแก่น',lat:16.4162,lng:102.8394,aliases:['บึงแก่นนคร','แก่นนคร','bueng kaen nakhon']},
  {name:'สถานีขนส่งผู้โดยสารจังหวัดขอนแก่น แห่งที่ 3',label:'บขส. 3 · เมืองขอนแก่น',lat:16.4421,lng:102.8271,aliases:['บขส','บขส 3','สถานีขนส่ง','bus terminal','terminal']},
  {name:'สถานีรถไฟขอนแก่น',label:'สถานีรถไฟขอนแก่น · เมืองขอนแก่น',lat:16.4322,lng:102.8236,aliases:['สถานีรถไฟ','รถไฟขอนแก่น','train station']}
]);
function normalized(value) { return String(value||'').toLocaleLowerCase('th').replace(/[.]/g,'').replace(/\s+/g,' ').trim(); }
function localRank(item,query) {
  const aliases=[item.name,item.label,...item.aliases].map(normalized);
  const exact=aliases.some(alias=>alias===query), prefix=aliases.some(alias=>alias.startsWith(query));
  return exact?0:prefix?1:2;
}
export function localPlaces(query) {
  const q=normalized(query);if(q.length<3)return [];
  return LOCAL_PLACES.filter(item=>[item.name,item.label,...item.aliases].some(alias=>normalized(alias).includes(q)))
    .sort((a,b)=>localRank(a,q)-localRank(b,q) || ((a.lat-KKU_CENTER.lat)**2+(a.lng-KKU_CENTER.lng)**2)-((b.lat-KKU_CENTER.lat)**2+(b.lng-KKU_CENTER.lng)**2))
    .slice(0,5).map(({aliases,...item})=>item);
}
export function cachedPlaces(query) {
  const item=searchCache.get(query.trim().toLocaleLowerCase('th'));
  return item && Date.now()-item.time<300000 ? item.places : null;
}
export function validCoordinates(lat, lng) {
  return [lat,lng].every(v => v !== null && v !== undefined && String(v).trim() !== '' && Number.isFinite(Number(v))) && Math.abs(Number(lat)) <= 90 && Math.abs(Number(lng)) <= 180;
}
function coordinatePair(value) {
  const match=String(value||'').match(/^\s*(-?\d{1,2}(?:\.\d+)?)\s*,\s*(-?\d{1,3}(?:\.\d+)?)\s*$/);
  if(!match || !validCoordinates(match[1],match[2])) return null;
  return {lat:Number(match[1]),lng:Number(match[2])};
}
export function parseGoogleMapsLocationUrl(value) {
  let url;
  try { url=new URL(String(value||'').trim()); } catch { return null; }
  const host=url.hostname.toLowerCase();
  const googleMapsHost=['google.com','www.google.com','maps.google.com','google.co.th','www.google.co.th','maps.google.co.th'].includes(host);
  if(url.protocol!=='https:' || !googleMapsHost || !url.pathname.toLowerCase().includes('/maps') || /\/maps\/dir(?:\/|$)/i.test(url.pathname)) return null;
  const query=coordinatePair(url.searchParams.get('query')||url.searchParams.get('q')||url.searchParams.get('ll'));
  if(query) return {...query,source:'query'};
  const place=url.href.match(/!3d(-?\d+(?:\.\d+)?)!4d(-?\d+(?:\.\d+)?)/i);
  if(place && validCoordinates(place[1],place[2])) return {lat:Number(place[1]),lng:Number(place[2]),source:'place-pin'};
  const center=url.href.match(/@(-?\d+(?:\.\d+)?),(-?\d+(?:\.\d+)?)(?:,|$)/);
  if(center && validCoordinates(center[1],center[2])) return {lat:Number(center[1]),lng:Number(center[2]),source:'map-center'};
  return null;
}
function place(feature) {
  const [lng,lat] = feature.geometry?.coordinates || [];
  const p = feature.properties || {};
  if (!validCoordinates(lat,lng)) return null;
  const name = p.name || p.street || p.district || p.city || p.state;
  if (!name) return null;
  return {lat,lng,name,label:[...new Set([name,p.street,p.district,p.city,p.state].filter(Boolean))].join(' · '),country:p.countrycode};
}
function distanceKm(a,b) {
  const rad=x=>x*Math.PI/180,lat1=rad(a.lat),lat2=rad(b.lat),dlat=lat2-lat1,dlng=rad(b.lng-a.lng);
  const h=Math.sin(dlat/2)**2+Math.cos(lat1)*Math.cos(lat2)*Math.sin(dlng/2)**2;
  return 6371*2*Math.atan2(Math.sqrt(h),Math.sqrt(Math.max(0,1-h)));
}
export function nearbyKkuPlaces(places) {
  return places.filter(p=>distanceKm(KKU_CENTER,p)<=SEARCH_RADIUS_KM);
}
export function mergePlaceResults(local,remote) {
  const merged=[];
  for(const p of [...local,...remote]) {
    const key=normalized(p.name);
    if(merged.some(existing=>normalized(existing.name)===key || distanceKm(existing,p)<0.08)) continue;
    merged.push(p);
    if(merged.length===5) break;
  }
  return merged;
}
async function request(path, params, signal, fetcher) {
  const response = await fetcher(`${BASE}/${path}?${new URLSearchParams(params)}`, {signal,headers:{'Accept-Language':'th'}});
  if (!response.ok) throw Error('บริการค้นหาสถานที่ไม่พร้อม');
  return (await response.json()).features || [];
}
export async function searchRemotePlaces(query, signal, fetcher = fetch) {
  if (query.trim().length < 3) return [];
  const cached=cachedPlaces(query);if(cached)return cached;
  const features = await request('api/', {q:query.trim(),countrycode:'TH',lat:KKU_CENTER.lat,lon:KKU_CENTER.lng,zoom:13,location_bias_scale:0.2,bbox:KKU_SEARCH_BBOX,lang:'th',limit:30}, signal, fetcher);
  const places=nearbyKkuPlaces(features.map(place).filter(p=>p && p.country?.toUpperCase()==='TH'))
    .sort((a,b)=>distanceKm(KKU_CENTER,a)-distanceKm(KKU_CENTER,b)).slice(0,5);
  if(searchCache.size>=50)searchCache.delete(searchCache.keys().next().value);
  searchCache.set(query.trim().toLocaleLowerCase('th'),{time:Date.now(),places});
  return places;
}
// Kept for existing consumers/tests; picker calls the remote service only after an explicit request.
export const searchPlaces=searchRemotePlaces;
export async function reversePlace(lat,lng,signal,fetcher=fetch) {
  const features = await request('reverse', {lat,lon:lng,limit:1},signal,fetcher);
  return features.map(place).find(Boolean) || null;
}
export function placePicker(form, map, locate) {
  const input = document.getElementById('place-search');
  const results = document.getElementById('place-results');
  const status = document.getElementById('place-search-status');
  const pinStatus = document.getElementById('pin-status');
  const mapsLink = document.getElementById('maps-link-input');
  const mapsLinkStatus = document.getElementById('maps-link-status');
  const applyMapsLink = document.getElementById('apply-maps-link');
  const advanced = document.querySelector('.manual-coordinates');
  const name = form.elements.pickupLocationName;
  const manualLat = document.getElementById('manual-latitude');
  const manualLng = document.getElementById('manual-longitude');
  let remoteSearch=null, timer, searchController, reverseController, revision=0, searchRevision=0, selected=false;
  function close() { results.replaceChildren(); results.hidden=true; input.setAttribute('aria-expanded','false'); }
  function cancelSearch() { remoteSearch=null;clearTimeout(timer);searchRevision++;searchController?.abort();close(); }
  function pin(lat,lng,label) {
    if (!validCoordinates(lat,lng)) return false;
    revision++; reverseController?.abort();cancelSearch();selected=true;
    form.elements.latitude.value=Number(lat).toFixed(7);
    form.elements.longitude.value=Number(lng).toFixed(7);
    manualLat.value=form.elements.latitude.value;manualLng.value=form.elements.longitude.value;
    pinStatus.textContent=label ? `เลือกจุดรับแล้ว: ${label}` : 'เลือกจุดรับแล้ว · กำลังค้นหาชื่อพื้นที่โดยประมาณ…';
    return true;
  }
  async function pick(lat,lng,fromMapLink=false) {
    if (!pin(lat,lng)) return;
    if(!fromMapLink && mapsLinkStatus) mapsLinkStatus.textContent='';
    const seq=revision, originalName=name.value;
    const controller=new AbortController();reverseController=controller;
    const timeout=setTimeout(()=>controller.abort(),8000);
    try {
      const p=await reversePlace(lat,lng,controller.signal);
      if (seq!==revision) return;
      pinStatus.textContent=p ? `เลือกจุดรับแล้ว · พื้นที่โดยประมาณ: ${p.label}` : 'เลือกจุดรับแล้ว · ไม่พบชื่อพื้นที่ สามารถระบุชื่อจุดนัดรับเองได้';
      if (p && !originalName.trim() && name.value===originalName) name.value=p.name.slice(0,255);
    } catch { if(seq===revision) pinStatus.textContent='เลือกจุดรับแล้ว · ค้นหาชื่อพื้นที่ไม่ได้ กรุณาระบุชื่อจุดนัดรับเอง'; }
    finally { clearTimeout(timeout); }
  }
  function render(places, source='local') {
    close();status.textContent=places.length ? 'พบสถานที่ใน มข. และตัวเมืองขอนแก่น · สถานที่ใกล้ มข. จะแสดงก่อน' : 'ไม่พบสถานที่ในบริเวณนี้ ลองชื่ออื่น หรือเลือกบนแผนที่ / ตัวเลือกขั้นสูง';
    for(const p of places) {
      const button=document.createElement('button');button.type='button';button.className='place-result';button.setAttribute('role','option');
      const title=document.createElement('strong');title.textContent=p.name;
      const address=document.createElement('small');address.textContent=p.label;
      button.append(title);button.append(address);
      button.onclick=()=>{pin(p.lat,p.lng,p.label);map?.setPin(p.lat,p.lng);input.value=p.name;name.value=p.name.slice(0,255);input.blur();status.textContent='เลือกสถานที่แล้ว สามารถแก้ชื่อจุดนัดรับให้ละเอียดขึ้นได้';};results.append(button);
    }
    results.hidden=!places.length;input.setAttribute('aria-expanded',String(!!places.length));
    if(places.length && matchMedia('(max-width: 600px)').matches) input.scrollIntoView({block:'nearest',behavior:'smooth'});
  }
  async function runRemoteSearch(local=localPlaces(input.value.trim())) {
    const q=input.value.trim();if(q.length<3)return;
    const seq=++searchRevision;clearTimeout(timer);searchController?.abort();
    const controller=new AbortController();searchController=controller;
    const timeout=setTimeout(()=>controller.abort(),8000);
    status.textContent=local.length?'กำลังค้นหาเพิ่ม · แสดงสถานที่ใกล้ มข. ที่พบแล้ว':'กำลังค้นหาสถานที่ในขอนแก่น…';
    try {
      const places=await searchRemotePlaces(q,controller.signal);
      if(seq!==searchRevision) return;
      render(mergePlaceResults(local,places),'combined');
    } catch { if(seq===searchRevision) {status.textContent=local.length?'ค้นหาเพิ่มไม่ได้ · แสดงสถานที่ใกล้ มข. ที่พบแล้ว':'ค้นหาเพิ่มไม่ได้ กรุณาเลือกบนแผนที่หรือใช้ตัวเลือกขั้นสูง';if(!local.length)advanced.open=true;} }
    finally {clearTimeout(timeout);}
  }
  input.addEventListener('input',()=>{
    cancelSearch();const q=input.value.trim();
    if(q.length<3) {status.textContent='พิมพ์อย่างน้อย 3 ตัวอักษร';return;}
    const places=localPlaces(q);render(places);
    if(!places.length) status.textContent='กำลังค้นหาสถานที่ในขอนแก่น…';
    remoteSearch=()=>runRemoteSearch(places);
    timer=setTimeout(()=>runRemoteSearch(places),550);
  });
  input.addEventListener('keydown',e=>{
    if(e.key==='Escape') cancelSearch();
    if(e.key==='ArrowDown' && !results.hidden) {e.preventDefault();results.firstElementChild?.focus();}
    if(e.key==='Enter') {e.preventDefault();if(!results.hidden)results.firstElementChild?.click();else remoteSearch?.();}
  });
  document.getElementById('place-search-button')?.addEventListener('click',()=>{if(input.value.trim().length>=3)runRemoteSearch(localPlaces(input.value.trim()));else input.dispatchEvent(new Event('input'));});
  results.addEventListener('keydown',e=>{
    if(e.key==='ArrowDown'||e.key==='ArrowUp') {e.preventDefault();(e.key==='ArrowDown'?e.target.nextElementSibling:e.target.previousElementSibling)?.focus();}
    if(e.key==='Escape') {cancelSearch();input.focus();}
  });
  document.addEventListener('click',e=>{if(!e.target.closest('.place-picker')) cancelSearch();});
  document.getElementById('apply-coordinates').onclick=()=>{
    if(!validCoordinates(manualLat.value,manualLng.value)) {pinStatus.textContent='กรุณากรอกพิกัดเป็นตัวเลขที่ถูกต้อง (ละติจูด -90 ถึง 90 ลองจิจูด -180 ถึง 180)';return;}
    map?.setPin(Number(manualLat.value),Number(manualLng.value));pick(manualLat.value,manualLng.value);
  };
  applyMapsLink?.addEventListener('click',()=>{
    const value=mapsLink.value.trim();
    const point=parseGoogleMapsLocationUrl(value);
    if(!point) {
      mapsLinkStatus.textContent=/^https:\/\/(share\.google|maps\.app\.goo\.gl|goo\.gl)(\/|$)/i.test(value)
        ? 'ลิงก์ Google Maps แบบย่อยังอ่านพิกัดไม่ได้โดยตรง · เปิดลิงก์ในเบราว์เซอร์แล้วคัดลอก URL แบบเต็มจากแถบที่อยู่ หรือเลือกจุดบนแผนที่'
        : 'อ่านพิกัดจากลิงก์นี้ไม่ได้ · ใช้ URL เต็มของ Google Maps ที่มีพิกัด หรือเลือกจุดบนแผนที่';
      mapsLink.focus();return;
    }
    map?.setPin(point.lat,point.lng);
    void pick(point.lat,point.lng,true);
    mapsLinkStatus.textContent=point.source==='map-center'
      ? 'วางหมุดจากจุดกึ่งกลางแผนที่แล้ว ซึ่งอาจไม่ใช่หมุดสถานที่ที่แชร์ · ตรวจหมุดก่อนเผยแพร่ และแตะแผนที่หรือลากหมุดเพื่อแก้ได้'
      : 'อ่านพิกัดจากลิงก์แล้ว · ตรวจหมุดบนแผนที่ก่อนเผยแพร่ และแตะแผนที่หรือลากหมุดเพื่อแก้ได้';
  });
  mapsLink?.addEventListener('keydown',event=>{
    if(event.key==='Enter') {event.preventDefault();applyMapsLink?.click();}
  });
  document.getElementById('use-location').onclick=async function() {
    this.disabled=true;pinStatus.textContent='กำลังหาตำแหน่งปัจจุบัน…';
    try {const p=await locate({fresh:true});map?.setPin(p.lat,p.lng);await pick(p.lat,p.lng);}
    catch(e) {pinStatus.textContent=e.message;advanced.open=true;}
    finally {this.disabled=false;}
  };
  // Manual drafts do not alter the submitted pin until the user applies them.
  function validate() {
    const valid=selected && validCoordinates(form.elements.latitude.value,form.elements.longitude.value) && !!name.value.trim();
    if(!valid) {
      pinStatus.textContent=!name.value.trim() ? 'กรุณาระบุชื่อจุดรับอาหาร' : 'กรุณาเลือกจุดรับบนแผนที่ หรือกดแสดงหมุดตามพิกัดในตัวเลือกขั้นสูง';
      document.querySelector('.map-panel').scrollIntoView({behavior:'smooth',block:'center'});
      (!name.value.trim()?name:input).focus({preventScroll:true});
    }
    return valid;
  }
  return {pick,validate,load(p) {pin(p.latitude,p.longitude,p.pickupLocationName);input.value=p.pickupLocationName;map?.setPin(p.latitude,p.longitude);}};
}
