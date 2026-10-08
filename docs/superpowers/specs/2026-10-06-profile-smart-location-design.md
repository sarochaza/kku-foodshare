# Profile popover and KKU Smart Location Search

## Purpose

Make the signed-in experience quicker for a KKU FoodShare member without changing authentication, reservation, QR pickup, food-post data, or deployment topology. The design keeps the current blue-green theme and does not introduce a paid map provider or an API key.

## Confirmed decisions

- The profile control is a compact popover opened from the existing avatar in the desktop header, rather than a compulsory full account page.
- The popover contains the member photo, display name, quick links to My Reservations, My Posts, Notifications, and Edit Profile, plus a clearly separated Logout form.
- The existing `/account` page remains available as the editing destination; no profile data endpoint, authentication rule, or logout behavior changes.
- The place picker remains Leaflet/OSM based.
- Place suggestions prioritize a maintained client-side KKU/Khon Kaen catalogue, with Thai and English aliases.
- Remote OSM search is an explicit fallback action, not a request on every keystroke. Existing map click, marker drag, current location, reverse naming, hidden coordinates, and advanced-coordinate fallback stay intact.

## Component design

### Header profile popover

`fragments.html` replaces the signed-in avatar link with an accessible button and a compact popover. It supports click, Escape, outside click, and focus movement. Links keep the existing routes: `/reservations`, `/account/posts`, `/notifications`, and `/account`. Logout remains the existing protected `POST /logout` form.

On small screens, the bottom navigation continues to expose Profile and Reservations. The account page remains a full editing page, so photo upload and display-name editing need no duplicate form or new backend API.

### KKU Smart Location Search

`places.mjs` receives a small, versioned local catalogue of high-value KKU and Khon Kaen places, each with coordinates, display label, and search aliases. It returns local matches immediately after two meaningful characters using normalized Thai/English matching and rank ordering: exact match, prefix match, KKU campus match, Khon Kaen match, then other local matches.

The visible picker shows these local recommendations as the user types. If none fit, it presents a deliberate “ค้นหาสถานที่เพิ่มเติม” action. That action makes one remote request, country-limited to Thailand and biased toward KKU; its result is cached. This avoids treating an external public geocoder as an unrestricted autocomplete service. Reverse lookup is throttled and cached by rounded coordinates, while failures retain the existing editable pickup name and Advanced Coordinates fallback.

## Data flow and safety

- Selecting any local or remote suggestion calls the existing pin routine, updates the map/marker, hidden `latitude` and `longitude`, pickup location name, and confirmation text.
- A map click, marker drag, or current location still sets valid coordinates first. Reverse lookup is best-effort only and never overwrites a user-edited pickup name.
- No database migration or backend endpoint is required. The existing `pickupLocationName`, `latitude`, and `longitude` contract is unchanged.
- Existing Photon access is kept only as a button-initiated fallback until a self-hosted or contracted provider is chosen. It is never called from every input event.

## Error handling

- Empty/local no-match: explain that the user can search further, select on the map, use current location, or use Advanced Coordinates.
- Remote outage: show an actionable Thai message and leave local/map/manual choices available.
- Geolocation denial: retain the current Thai error state and map/manual choices.
- Popover: close safely on navigation, logout submission, outside click, and Escape; no state is persisted.

## Tests

- JavaScript unit tests for normalized local matching, KKU-first rank order, explicit remote fallback, cache use, and no automatic external request while typing.
- Template/browser checks for profile-popover links and logout form.
- Existing location, reservation, QR, owner-management, and route tests must pass.
- Responsive checks at mobile widths verify popover and result list do not overflow and remain touch sized.

## Files expected to change

- `code/src/main/resources/templates/fragments.html`
- `code/src/main/resources/static/js/app.js`
- `code/src/main/resources/static/js/places.mjs`
- `code/src/main/resources/static/css/app.css`
- Relevant JavaScript and web-page regression tests

No authentication controller, reservation service, database schema, Docker Compose configuration, or `.env` file is changed.
