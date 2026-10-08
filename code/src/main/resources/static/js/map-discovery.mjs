export function hasLocation(coords) {
  return coords?.lat != null && coords?.lng != null &&
    Number.isFinite(Number(coords.lat)) && Number.isFinite(Number(coords.lng)) &&
    Math.abs(Number(coords.lat)) <= 90 && Math.abs(Number(coords.lng)) <= 180;
}

// Every map entry goes to the existing sharing-map page with the current filters.
export function sharingMapUrl(filters = {}) {
  const query = new URLSearchParams({view: 'map'});
  for (const key of ['q', 'category', 'ownership', 'sort']) if (filters[key]) query.set(key, filters[key]);
  if (filters.now) query.set('now', 'true');
  return `/explore?${query}`;
}

export function mapFeedRequest(state, coords) {
  const query = new URLSearchParams({q: state.q || '', category: state.category || '',
    now: Boolean(state.now), ownership: state.ownership || ''});
  // The original /map endpoint always sorts by expiry and ignores GPS. Reuse the
  // existing nearby search, including its 200-pin limit, rather than add an API.
  if (state.sort === 'nearby' && hasLocation(coords)) {
    query.set('sort', 'nearby'); query.set('lat', coords.lat); query.set('lng', coords.lng);
    query.set('page', '0'); query.set('size', '200');
    return `/api/v1/food-posts?${query}`;
  }
  return `/api/v1/food-posts/map?${query}`;
}

function kilometres(a, b) {
  const rad = n => n * Math.PI / 180;
  const h = Math.sin(rad(b.lat - a.lat) / 2) ** 2 + Math.cos(rad(a.lat)) * Math.cos(rad(b.lat)) * Math.sin(rad(b.lng - a.lng) / 2) ** 2;
  return 6371 * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(Math.max(0, 1 - h)));
}

export function focusFoodMap(map, posts, coords, nearby = false) {
  if (!hasLocation(coords)) return;
  const origin = [Number(coords.lat), Number(coords.lng)];
  const points = posts.map(p => ({lat: Number(p.latitude), lng: Number(p.longitude)})).filter(hasLocation);
  if (nearby) {
    const close = points.filter(p => kilometres(coords, p) <= 3).slice(0, 5);
    if (close.length) map.fitBounds([origin, ...close.map(p => [p.lat, p.lng])], {padding: [40, 40], maxZoom: 16});
    else map.setView(origin, 15);
  } else if (points.length) map.fitBounds([...points.map(p => [p.lat, p.lng]), origin], {padding: [40, 40], maxZoom: 15});
  else map.setView(origin, 15);
}
