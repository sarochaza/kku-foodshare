import test from 'node:test';
import assert from 'node:assert/strict';
import { validCoordinates, searchPlaces, reversePlace } from '../../main/resources/static/js/places.mjs';
const feature = (name, lat=16.47, lng=102.82, countrycode='TH') => ({geometry:{coordinates:[lng,lat]},properties:{name,city:'ขอนแก่น',countrycode}});
test('coordinates reject blank, nonfinite, and out of range values', () => {
  for (const pair of [['',''],[' ',102],[null,102],[NaN,102],[91,102],[16,181],[Infinity,102]]) assert.equal(validCoordinates(...pair),false);
  assert.equal(validCoordinates('16.4745','102.8237'),true);
  assert.equal(validCoordinates(0,0),true);
});
test('search ignores short queries, limits Thailand results and prioritizes KKU', async () => {
  let calls=0;
  const fetcher=async url=>{calls++; const u=new URL(url); assert.equal(u.searchParams.get('countrycode'),'TH');assert.equal(u.searchParams.get('lat'),'16.4745');return {ok:true,json:async()=>({features:[feature('กรุงเทพ',13.75,100.5),feature('หอสมุด มข.'),feature('foreign',16,102,'LA')]})};};
  assert.deepEqual(await searchPlaces('ab',undefined,fetcher),[]);assert.equal(calls,0);
  const result=await searchPlaces('หอสมุด',undefined,fetcher);assert.equal(result.length,2);assert.equal(result[0].name,'หอสมุด มข.');assert.equal(result[0].lat,16.47);
});
test('reverse returns local area and handles empty results', async()=>{
  assert.equal((await reversePlace(16.47,102.82,undefined,async()=>({ok:true,json:async()=>({features:[feature('หอสมุด')]})}))).name,'หอสมุด');
  assert.equal(await reversePlace(16.47,102.82,undefined,async()=>({ok:true,json:async()=>({features:[]})})),null);
});
test('network/service failure rejects so UI can offer manual fallback', async()=>{
  await assert.rejects(searchPlaces('ขอนแก่น',undefined,async()=>({ok:false})),/ค้นหา/);
});
test('repeated place query reuses cache without another network round trip',async()=>{
 let calls=0;const fetcher=async()=>{calls++;return {ok:true,json:async()=>({features:[feature('สถานที่แคช')]})};};
 await searchPlaces('สถานที่แคช',undefined,fetcher);await searchPlaces('สถานที่แคช',undefined,fetcher);assert.equal(calls,1);
});
