// Local suggestions keep common KKU places instant and work without a paid API.
const BASE = 'https://photon.komoot.io';
const searchCache=new Map();
const KKU_CENTER={lat:16.4745,lng:102.8237};
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
function place(feature) {
  const [lng,lat] = feature.geometry?.coordinates || [];
  const p = feature.properties || {};
  if (!validCoordinates(lat,lng)) return null;
  const name = p.name || p.street || p.district || p.city || p.state;
  if (!name) return null;
  return {lat,lng,name,label:[...new Set([name,p.street,p.district,p.city,p.state].filter(Boolean))].join(' · '),country:p.countrycode};
}
async function request(path, params, signal, fetcher) {
  const response = await fetcher(`${BASE}/${path}?${new URLSearchParams(params)}`, {signal,headers:{'Accept-Language':'th'}});
  if (!response.ok) throw Error('บริการค้นหาสถานที่ไม่พร้อม');
  return (await response.json()).features || [];
}
export async function searchRemotePlaces(query, signal, fetcher = fetch) {
  if (query.trim().length < 3) return [];
  const cached=cachedPlaces(query);if(cached)return cached;
  const features = await request('api/', {q:query.trim(),countrycode:'TH',lat:16.4745,lon:102.8237,zoom:12,limit:5}, signal, fetcher);
  const places=features.map(place).filter(p=>p && p.country?.toUpperCase()==='TH')
    .sort((a,b)=>((a.lat-16.4745)**2+(a.lng-102.8237)**2)-((b.lat-16.4745)**2+(b.lng-102.8237)**2)).slice(0,5);
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
  async function pick(lat,lng) {
    if (!pin(lat,lng)) return;
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
    close();status.textContent=places.length ? (source==='local'?'พบสถานที่ใกล้ มข. / ขอนแก่น เลือกแล้วปรับจุดนัดรับได้':'ผลค้นหาสถานที่ เลือกแล้วปรับจุดนัดรับได้') : 'ไม่พบสถานที่ ลองชื่ออื่น หรือเลือกบนแผนที่ / ตัวเลือกขั้นสูง';
    for(const p of places) {
      const button=document.createElement('button');button.type='button';button.className='place-result';button.setAttribute('role','option');
      const title=document.createElement('strong');title.textContent=p.name;
      const address=document.createElement('small');address.textContent=p.label;
      button.append(title);button.append(address);
      button.onclick=()=>{pin(p.lat,p.lng,p.label);map?.setPin(p.lat,p.lng);input.value=p.name;name.value=p.name.slice(0,255);input.blur();status.textContent='เลือกสถานที่แล้ว สามารถแก้ชื่อจุดนัดรับให้ละเอียดขึ้นได้';};results.append(button);
    }
    results.hidden=!places.length;input.setAttribute('aria-expanded',String(!!places.length));
    if(places.length && matchMedia('(max-width: 600px)').matches) input.scrollIntoView({block:'start',behavior:'smooth'});
  }
  async function runRemoteSearch() {
    const q=input.value.trim();if(q.length<3)return;
    const seq=++searchRevision;clearTimeout(timer);searchController?.abort();
    const controller=new AbortController();searchController=controller;
    const timeout=setTimeout(()=>controller.abort(),8000);
    status.textContent='กำลังค้นหาสถานที่เพิ่มเติม…';
    try {
      const places=await searchRemotePlaces(q,controller.signal);
      if(seq!==searchRevision) return;
      render(places,'remote');
    } catch { if(seq===searchRevision) {status.textContent='ค้นหาเพิ่มเติมไม่ได้ กรุณาเลือกบนแผนที่หรือใช้ตัวเลือกขั้นสูง';advanced.open=true;} }
    finally {clearTimeout(timeout);}
  }
  input.addEventListener('input',()=>{
    cancelSearch();const q=input.value.trim();
    status.textContent=q.length<3 ? 'พิมพ์อย่างน้อย 3 ตัวอักษร' : 'กำลังค้นหา มข. และขอนแก่น…';
    if(q.length<3) return;
    timer=setTimeout(()=>{const places=localPlaces(q);render(places);remoteSearch=runRemoteSearch;},250);
  });
  input.addEventListener('keydown',e=>{
    if(e.key==='Escape') cancelSearch();
    if(e.key==='ArrowDown' && !results.hidden) {e.preventDefault();results.firstElementChild?.focus();}
    if(e.key==='Enter') {e.preventDefault();if(!results.hidden)results.firstElementChild?.click();else remoteSearch?.();}
  });
  document.getElementById('place-search-button')?.addEventListener('click',()=>{if(remoteSearch)remoteSearch();else input.dispatchEvent(new Event('input'));});
  results.addEventListener('keydown',e=>{
    if(e.key==='ArrowDown'||e.key==='ArrowUp') {e.preventDefault();(e.key==='ArrowDown'?e.target.nextElementSibling:e.target.previousElementSibling)?.focus();}
    if(e.key==='Escape') {cancelSearch();input.focus();}
  });
  document.addEventListener('click',e=>{if(!e.target.closest('.place-picker')) cancelSearch();});
  document.getElementById('apply-coordinates').onclick=()=>{
    if(!validCoordinates(manualLat.value,manualLng.value)) {pinStatus.textContent='กรุณากรอกพิกัดเป็นตัวเลขที่ถูกต้อง (ละติจูด -90 ถึง 90 ลองจิจูด -180 ถึง 180)';return;}
    map?.setPin(Number(manualLat.value),Number(manualLng.value));pick(manualLat.value,manualLng.value);
  };
  document.getElementById('use-location').onclick=async function() {
    this.disabled=true;pinStatus.textContent='กำลังหาตำแหน่งปัจจุบัน…';
    try {const p=await locate();map?.setPin(p.lat,p.lng);await pick(p.lat,p.lng);}
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
