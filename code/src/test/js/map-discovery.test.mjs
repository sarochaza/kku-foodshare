import test from 'node:test';
import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
import {sharingMapUrl, mapFeedRequest, focusFoodMap, hasLocation} from '../../main/resources/static/js/map-discovery.mjs';
const app = await readFile(new URL('../../main/resources/static/js/app.js', import.meta.url), 'utf8');

test('home icon uses the sharing map route and preserves filters', async () => {
  const url = new URL(sharingMapUrl({q:'หอสมุด',category:'FOOD',now:true,ownership:'others'}), 'https://foodshare.local');
  assert.equal(url.pathname, '/explore'); assert.equal(url.searchParams.get('view'), 'map');
  assert.equal(url.searchParams.get('q'), 'หอสมุด'); assert.equal(url.searchParams.get('ownership'), 'others');
  assert.equal(url.searchParams.get('now'), 'true');
  const home = await readFile(new URL('../../main/resources/templates/home.html', import.meta.url), 'utf8');
  assert.match(home, /data-sharing-map href="\/explore\?view=map"/);
});
test('signed-in dashboard and explore use one rich map fragment, including preview and sidebar', async () => {
  for (const page of ['dashboard','explore']) {
    const html=await readFile(new URL(`../../main/resources/templates/${page}.html`,import.meta.url),'utf8');
    assert.match(html,/th:replace="~\{fragments :: sharingMap\}"/);
    assert.doesNotMatch(html,/id="explore-map-shell"/);
    assert.match(html,/data-owner="mine"/);
  }
  const fragment=await readFile(new URL('../../main/resources/templates/fragments.html',import.meta.url),'utf8');
  assert.match(fragment,/th:fragment="sharingMap".*class="map-shell map-shell-rich"/);
  for(const id of ['explore-map-aside','explore-map-full','explore-map-preview','explore-map-list'])assert.ok(fragment.includes(`id="${id}"`));
});
test('nearby map reuses actual server sorting with coordinates and current filters', () => {
  const url = new URL(mapFeedRequest({q:'rice',category:'FOOD',ownership:'mine',now:true,sort:'nearby'}, {lat:16.47,lng:102.82}), 'https://foodshare.local');
  assert.equal(url.pathname, '/api/v1/food-posts');
  for (const [key,value] of Object.entries({sort:'nearby',lat:'16.47',lng:'102.82',size:'200',page:'0',q:'rice',category:'FOOD',ownership:'mine',now:'true'})) assert.equal(url.searchParams.get(key), value);
  assert.match(mapFeedRequest({sort:'expiry'}, {}), /^\/api\/v1\/food-posts\/map\?/);
  assert.match(mapFeedRequest({sort:'nearby'}, {lat:null,lng:null}), /^\/api\/v1\/food-posts\/map\?/);
});
test('nearby viewport excludes distant pins and centres on the user when no close food exists', () => {
  const bounds = [], views = [], map = {fitBounds: points => bounds.push(points), setView: (point, zoom) => views.push({point, zoom})};
  const origin = {lat:16.47,lng:102.82};
  focusFoodMap(map, [{latitude:16.471,longitude:102.821},{latitude:13.75,longitude:100.5}], origin, true);
  assert.deepEqual(bounds[0], [[16.47,102.82],[16.471,102.821]]);
  focusFoodMap(map, [{latitude:13.75,longitude:100.5}], origin, true);
  assert.deepEqual(views[0], {point:[16.47,102.82],zoom:15});
  assert.equal(hasLocation({lat:0,lng:0}), true);assert.equal(hasLocation({lat:Infinity,lng:102}), false);assert.equal(hasLocation({lat:null,lng:null}), false);
});
function harness({gps = async () => {throw Error('GPS unavailable');}} = {}) {
  const nodes = new Map(), calls = [], classes = new Set(['map-expanded']), events = {}, mapEvents = {}, views = [];
  const $ = selector => {
    if (!nodes.has(selector)) nodes.set(selector, {innerHTML:'',value:'',textContent:'',hidden:false,dataset:{},
      classList:{toggle(name, yes) {if (yes) classes.add(name);else classes.delete(name);},remove(name){classes.delete(name);},add(name){classes.add(name);}},
      setAttribute(name,value){this[name]=value;}, addEventListener(name,fn){this[name]=fn;}, scrollIntoView(){}});
    return nodes.get(selector);
  };
  const map = {invalidateSize(){},setView(point,zoom){views.push({point,zoom});},fitBounds(points){views.push({points});},
    on(name,fn){mapEvents[name]=fn;},off(name,fn){if(mapEvents[name]===fn)delete mapEvents[name];}};
  const source = app.slice(app.indexOf('async function explore()'), app.indexOf('async function memberProfile('));
  const location = {search:'',href:'https://foodshare.local/explore'};
  const helpers = {$,$$:()=>[], location,history:{state:{},replaceState(_state,_unused,url){location.href=String(url);}},
    sharingMapUrl,mapFeedRequest,focusFoodMap,hasLocation,locate:gps,setDefaultOrigin(){},
    signedIn:()=>true,busy:async (_b,fn)=>fn(),toast(){},
    createMap:()=>({map}),markers(){},currentLocationMarker(){},hydrateRoadDistances(){},
    categories:{},empty:()=>'',feedCard:()=>'',wireFeedMenus(){},paginate(){},
    errorBox(_el,error){throw error;},requestAnimationFrame:fn=>fn(),CustomEvent:class{constructor(name,options){this.type=name;this.detail=options.detail;}},
    window:{dispatchEvent:e=>{events[e.type]=e.detail;}},
    api:async url=>{calls.push(url);return {items:[],page:0,totalPages:0,totalElements:0};}};
  const boot = new Function(...Object.keys(helpers), `${source}; return explore;`)(...Object.values(helpers));
  return {boot,$,calls,classes,mapEvents,views,events};
}
test('map icon resets expansion so map and aside match a fresh sharing-map entry', async () => {
  const h=harness();await h.boot();await h.$('#map-view').click();await new Promise(resolve=>setImmediate(resolve));
  assert.equal(h.classes.has('map-expanded'), false);assert.equal(h.$('#explore-map-shell').hidden, false);
  assert.equal(h.$('#explore-map-full')['aria-expanded'], 'false');
  assert.equal(new URL(h.calls.at(-1),'https://foodshare.local').pathname, '/api/v1/food-posts/map');
  assert.equal(h.events['foodshare:viewchange'].map, true);
});
test('explicit nearby refreshes GPS and sends coordinates for both food list and map', async () => {
  const options=[];
  const h=harness({gps:async opt=>{options.push(opt);return {lat:16.47,lng:102.82,accuracy:10};}});
  await h.boot();await new Promise(resolve=>setImmediate(resolve));await h.$('#nearby-button').onclick();
  assert.equal(options.at(-1).fresh, true);
  const last=h.calls.slice(-2).map(url=>new URL(url,'https://foodshare.local'));
  assert.ok(last.every(url=>url.searchParams.get('sort')==='nearby' && url.searchParams.get('lat')==='16.47'));
  assert.equal(last[1].searchParams.get('size'),'200');assert.equal(h.$('#sort').value,'nearby');
  assert.match(h.$('#nearby-status').textContent,/พบตำแหน่งแล้ว/);
});
test('denied GPS offers manual origin and a map click activates nearby sorting', async () => {
  const h=harness();await h.boot();await h.$('#nearby-button').onclick();assert.equal(h.$('#manual-location-button').hidden,false);
  await h.$('#manual-location-button').click();await h.mapEvents.click({latlng:{lat:16.48,lng:102.83}});
  assert.equal(h.$('#sort').value,'nearby');
  const query=new URL(h.calls.at(-1),'https://foodshare.local').searchParams;
  assert.equal(query.get('lat'),'16.48');assert.equal(query.get('lng'),'102.83');
  assert.match(h.$('#nearby-status').textContent,/ใช้จุดที่คุณเลือก/);assert.equal(h.mapEvents.click,undefined);
});
