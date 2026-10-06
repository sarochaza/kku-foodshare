import test from 'node:test';
import assert from 'node:assert/strict';
import {straightDistance, routeUrl, routeSummary, directionsUrl, originState, fetchRoute} from '../../main/resources/static/js/routes.mjs';
test('walking and driving use different real routing profiles and longitude-first coordinates',()=>{
 const a={lat:16.47,lng:102.82},b={latitude:16.48,longitude:102.83};
 assert.match(routeUrl(a,b,'walking'),/routed-foot\/route\/v1\/driving\/102.82,16.47;102.83,16.48/);
 assert.match(routeUrl(a,b,'driving'),/routed-car/);assert.throws(()=>routeUrl(a,b,'bike'));
});
test('navigation contains confirmed origin, destination and selected travel mode',()=>{
 const url=new URL(directionsUrl({lat:16.47,lng:102.82},{latitude:16.48,longitude:102.83},'walking'));
 assert.equal(url.searchParams.get('origin'),'16.47,102.82');assert.equal(url.searchParams.get('travelmode'),'walking');assert.equal(url.searchParams.get('destination'),'16.48,102.83');
});
test('distance is metres; seconds round up to minutes; invalid route is rejected',()=>{
 assert.deepEqual(routeSummary({distance:950,duration:750,geometry:{type:'LineString',coordinates:[[102.82,16.47],[102.83,16.48]]}}),{distance:950,minutes:13,coordinates:[[16.47,102.82],[16.48,102.83]]});
 assert.throws(()=>routeSummary({distance:NaN,duration:500}));assert.throws(()=>routeSummary({distance:500,duration:-1}));
 assert.equal(straightDistance({lat:16.47,lng:102.82},{latitude:16.47,longitude:102.82}),0);
});
test('GPS must be confirmed; stale GPS and manual changes invalidate prior confirmation',()=>{
 const s=originState();s.set({lat:16.47,lng:102.82},'gps',1000);assert.equal(s.ready(1000),false);s.confirm();assert.equal(s.ready(2000),true);assert.equal(s.ready(62000),false);
 s.set({lat:16.48,lng:102.83},'manual',63000);assert.equal(s.ready(64000),false);s.confirm();assert.equal(s.ready(999999),true);
});
test('routing failure and no-route reject for labelled straight-line fallback',async()=>{
 await assert.rejects(fetchRoute({lat:16.47,lng:102.82},{latitude:16.48,longitude:102.83},'walking',undefined,async()=>({ok:true,json:async()=>({code:'NoRoute'})})),/เส้นทาง/);
});
