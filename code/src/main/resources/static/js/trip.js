import {createMap,locate} from './maps.js';
import {asDate} from './ui.js';
import {originState,fetchRoute,straightDistance,formatMetres,directionsUrl} from './routes.mjs';
let counter=0;
export function mountTrip(root,post,{summaryTarget=null}={}) {
  const id=`trip-map-${++counter}`;
  root.classList.add('trip-panel');
  root.innerHTML=`<h3>ระยะทางและเวลาไปรับอาหาร</h3><p class="field-note">ตรวจจุดเริ่มต้นก่อนจอง หากหมุดไม่ตรง แตะแผนที่หรือลากหมุดสีน้ำเงินเพื่อแก้ไข</p><label>วิธีเดินทาง<select data-trip-mode><option value="driving">🚗 ขับรถ</option><option value="walking">🚶 เดินเท้า</option></select></label><div class="trip-actions"><button type="button" class="btn btn-soft" data-trip-locate>ใช้ตำแหน่งปัจจุบัน</button><button type="button" class="btn btn-primary" data-trip-confirm disabled>ยืนยันจุดเริ่มต้น</button></div><div id="${id}" class="trip-map" role="region" aria-label="แผนที่จุดเริ่มต้นและจุดรับอาหาร"></div><p class="trip-legend">🔵 จุดเริ่มต้นของคุณ · 🍱 จุดรับอาหาร</p><p data-trip-status role="status" class="field-note">ยังไม่ได้ระบุจุดเริ่มต้น</p><div data-trip-summary class="trip-summary" role="status">เลือกตำแหน่งและยืนยัน เพื่อดูระยะทางและเวลาโดยประมาณ</div><a data-trip-directions class="btn btn-soft full" target="_blank" rel="noopener noreferrer" hidden>เปิด Google Maps จากจุดนี้</a><button type="button" class="trip-skip" data-trip-skip>จองต่อโดยไม่ตรวจตำแหน่ง</button><p class="field-note trip-attribution">เส้นทาง © OpenStreetMap contributors / <a href="https://routing.openstreetmap.de/about.html" target="_blank" rel="noopener">FOSSGIS / OSRM</a> · <a href="https://www.openstreetmap.org/fixthemap" target="_blank" rel="noopener">แจ้งแก้ไขแผนที่</a></p>`;
  const $=s=>root.querySelector(s),state=originState();
  let picker=null,userMarker=null,circle=null,line=null,controller=null,seq=0,gpsSeq=0,skipped=false;
  const mode=$('[data-trip-mode]'),status=$('[data-trip-status]'),summary=$('[data-trip-summary]'),navigation=$('[data-trip-directions]'),confirm=$('[data-trip-confirm]');
  mode.value='driving';
  function setSummary(text) { summary.textContent=text; if(summaryTarget) summaryTarget.textContent=text; }
  function clearRoute() {seq++;controller?.abort();if(line){picker?.map.removeLayer(line);line=null;}navigation.hidden=true;setSummary('กำลังเตรียมคำนวณระยะทางจากจุดเริ่มต้น');}
  function update(p,source) {
    gpsSeq++;state.set(p,source);skipped=false;clearRoute();confirm.disabled=false;
    if(picker) {
      if(!userMarker) {
        userMarker=L.marker([p.lat,p.lng],{draggable:true,icon:L.divIcon({className:'trip-origin-pin',html:'<span>●</span>',iconSize:[32,32],iconAnchor:[16,16]})}).addTo(picker.map);
        userMarker.on('dragend',()=>{const p=userMarker.getLatLng();update({lat:p.lat,lng:p.lng},'manual');});
      } else userMarker.setLatLng([p.lat,p.lng]);
      if(circle)picker.map.removeLayer(circle);
      circle=source==='gps' && Number.isFinite(p.accuracy)?L.circle([p.lat,p.lng],{radius:p.accuracy,weight:1,color:'#347ce2',fillOpacity:.08,interactive:false}).addTo(picker.map):null;
      picker.map.fitBounds([[p.lat,p.lng],[post.latitude,post.longitude]],{padding:[35,35],maxZoom:16});
    }
    calculate();
    status.textContent=source==='manual'?'ใช้จุดเริ่มต้นที่เลือกเอง กรุณากดยืนยัน':`พบตำแหน่งอุปกรณ์${Number.isFinite(p.accuracy)?` · คลาดเคลื่อนประมาณ ${Math.round(p.accuracy)} เมตร`:''} กรุณาตรวจหมุดและกดยืนยัน`;
  }
  try {
    picker=createMap(id);picker.setPin(post.latitude,post.longitude);
    picker.map.on('click',e=>update({lat:e.latlng.lat,lng:e.latlng.lng},'manual'));
  } catch(e) {status.textContent=e.message+' · ยังตรวจตำแหน่งอุปกรณ์หรือข้ามการตรวจได้';}
  async function refresh() {
    const request=++gpsSeq;$('[data-trip-locate]').disabled=true;status.textContent='กำลังขอตำแหน่งปัจจุบัน…';
    try {const p=await locate();if(request===gpsSeq)update(p,'gps');}
    catch(e){if(request===gpsSeq){status.textContent=e.message+' · เลือกจุดเริ่มต้นบนแผนที่ หรือจองต่อโดยไม่ตรวจตำแหน่ง';setSummary('ยังไม่มีตำแหน่งของคุณ กรุณาอนุญาตตำแหน่งหรือเลือกจุดเริ่มต้นบนแผนที่');}}
    finally {$('[data-trip-locate]').disabled=false;}
  }
  async function calculate() {
    clearRoute();if(!state.point)return;
    const request=seq,origin={...state.point},selectedMode=mode.value;
    navigation.href=directionsUrl(origin,post,selectedMode);navigation.hidden=!state.ready();
    setSummary('กำลังคำนวณเส้นทาง…');controller=new AbortController();const local=controller;
    const timeout=setTimeout(()=>local.abort(),10000);
    try {
      const route=await fetchRoute(origin,post,selectedMode,local.signal);
      if(request!==seq)return;
      const arrival=new Date(Date.now()+route.minutes*60000),until=asDate(post.availableUntil);
      setSummary(`${!state.ready()?'ประมาณจากจุดเริ่มต้นที่ยังไม่ได้ยืนยัน · ':''}${selectedMode==='walking'?'เดินเท้า':'ขับรถ'} ${formatMetres(route.distance)} · ประมาณ ${route.minutes} นาที${Number.isFinite(until.getTime()) && arrival>until?' · อาจถึงหลังหมดเวลารับอาหาร':''} — เวลาโดยประมาณ ไม่รวมจราจรสด/เวลารอ`);
      if(picker){line=L.polyline(route.coordinates,{color:'#347ce2',weight:5}).addTo(picker.map);picker.map.fitBounds(line.getBounds(),{padding:[30,30]});}
    } catch {if(request===seq)setSummary(`คำนวณเส้นทางไม่ได้ · ระยะทางเส้นตรง ${formatMetres(straightDistance(origin,post))} (ไม่ใช่ระยะเดิน/ขับรถ) · ยังไม่มีเวลาเดินทาง`);}
    finally{clearTimeout(timeout);}
  }
  $('[data-trip-locate]').onclick=refresh;
  confirm.onclick=()=>{state.confirm();status.textContent='ยืนยันจุดเริ่มต้นแล้ว';calculate();};
  mode.onchange=()=>{if(state.point)calculate();else clearRoute();};
  navigation.onclick=e=>{
    if(!state.ready()) {e.preventDefault();clearRoute();status.textContent='ตำแหน่งอุปกรณ์เกิน 1 นาที กรุณาตรวจใหม่และยืนยันก่อนนำทาง';refresh();}
  };
  $('[data-trip-skip]').onclick=()=>{skipped=true;status.textContent='ข้ามการตรวจตำแหน่งแล้ว กดปุ่มจองอาหารเพื่อดำเนินต่อ';};
  return {async beforeReserve(){if(skipped||state.ready())return true;root.scrollIntoView({behavior:'smooth',block:'center'});if(!state.point||state.stale())await refresh();confirm.focus();return false;},refresh,dispose(){gpsSeq++;seq++;controller?.abort();picker?.map.remove();}};
}
