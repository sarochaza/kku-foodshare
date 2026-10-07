import test from 'node:test';
import assert from 'node:assert/strict';
import {straightDistance, routeUrl, routeSummary, directionsUrl, mapsUrl, originState, fetchRoute, setDefaultOrigin, roadDistancesUrl, parseRoadDistances} from '../../main/resources/static/js/routes.mjs';
test('walking and driving use different real routing profiles and longitude-first coordinates',()=>{
 const a={lat:16.47,lng:102.82},b={latitude:16.48,longitude:102.83};
 assert.match(routeUrl(a,b,'walking'),/routed-foot\/route\/v1\/driving\/102.82,16.47;102.83,16.48/);
 assert.match(routeUrl(a,b,'driving'),/routed-car/);assert.throws(()=>routeUrl(a,b,'bike'));
});
test('navigation contains confirmed origin, destination and selected travel mode',()=>{
 const url=new URL(directionsUrl({lat:16.47,lng:102.82},{latitude:16.48,longitude:102.83},'walking'));
 assert.equal(url.searchParams.get('origin'),'16.47,102.82');assert.equal(url.searchParams.get('travelmode'),'walking');assert.equal(url.searchParams.get('destination'),'16.48,102.83');
});
test('Google Maps directions preserve the confirmed reserver origin and exact pickup destination',()=>{
 const post={latitude:16.481234,longitude:102.817654};
 const url=new URL(mapsUrl(post,{lat:16.472345,lng:102.825678},'driving'));
 assert.equal(url.pathname,'/maps/dir/');
 assert.equal(url.searchParams.get('origin'),'16.472345,102.825678');
 assert.equal(url.searchParams.get('destination'),'16.481234,102.817654');
 assert.equal(url.searchParams.get('travelmode'),'driving');
});
test('Google Maps directions use the device current location when no reserver origin was confirmed',()=>{
 setDefaultOrigin(null);
 const url=new URL(mapsUrl({latitude:16.4768,longitude:102.82325}));
 assert.equal(url.pathname,'/maps/dir/');
 assert.equal(url.searchParams.get('origin'),null);
 assert.equal(url.searchParams.get('destination'),'16.4768,102.82325');
 assert.equal(url.searchParams.get('travelmode'),'driving');
});
test('post Google Maps links use the automatically detected current location and exact post coordinates',()=>{
 setDefaultOrigin({lat:16.470123,lng:102.821234});
 const url=new URL(mapsUrl({latitude:16.481234,longitude:102.817654}));
 assert.equal(url.searchParams.get('origin'),'16.470123,102.821234');
 assert.equal(url.searchParams.get('destination'),'16.481234,102.817654');
 assert.equal(url.searchParams.get('travelmode'),'driving');
 setDefaultOrigin(null);
});
test('distance is metres; seconds round up to minutes; invalid route is rejected',()=>{
 assert.deepEqual(routeSummary({distance:950,duration:750,geometry:{type:'LineString',coordinates:[[102.82,16.47],[102.83,16.48]]}}),{distance:950,minutes:13,coordinates:[[16.47,102.82],[16.48,102.83]]});
 assert.throws(()=>routeSummary({distance:NaN,duration:500}));assert.throws(()=>routeSummary({distance:500,duration:-1}));
 assert.equal(straightDistance({lat:16.47,lng:102.82},{latitude:16.47,longitude:102.82}),0);
});
test('a session-locked GPS origin stays consistent across posts until explicitly changed',()=>{
 const s=originState();s.set({lat:16.47,lng:102.82},'gps',1000);s.confirm();assert.equal(s.ready(2000),true);assert.equal(s.ready(999999999),true);
 s.set({lat:16.48,lng:102.83},'manual',63000);assert.equal(s.ready(64000),false);s.confirm();assert.equal(s.ready(999999),true);
});
test('routing failure and no-route reject for labelled straight-line fallback',async()=>{
 await assert.rejects(fetchRoute({lat:16.47,lng:102.82},{latitude:16.48,longitude:102.83},'walking',undefined,async()=>({ok:true,json:async()=>({code:'NoRoute'})})),/เส้นทาง/);
});
test('road distance matrix requests car travel distances from the current origin to posts',()=>{
 const origin={lat:16.47,lng:102.82},posts=[{latitude:16.48,longitude:102.83},{latitude:16.49,longitude:102.84}];
 const url=new URL(roadDistancesUrl(origin,posts));
 assert.match(url.pathname,/routed-car\/table\/v1\/driving/);
 assert.equal(url.searchParams.get('sources'),'0');
 assert.equal(url.searchParams.get('destinations'),'1;2');
 assert.equal(url.searchParams.get('annotations'),'distance');
 assert.deepEqual(parseRoadDistances({code:'Ok',distances:[[1200,2300]]},2),[1200,2300]);
});
test('road distance matrix rejects missing routes rather than substituting straight-line distance',()=>{
 assert.throws(()=>parseRoadDistances({code:'NoRoute',distances:[[null]]},1),/ระยะทางถนน/);
 assert.throws(()=>parseRoadDistances({code:'Ok',distances:[[null]]},1),/ระยะทางถนน/);
});
