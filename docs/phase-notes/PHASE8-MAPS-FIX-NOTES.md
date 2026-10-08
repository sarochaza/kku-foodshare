# Phase 8 — Map and post detail refinements

## What changed

- The home page requests device location automatically. The best stable GPS fix is stored for the browser tab and reused across post cards and route previews; it does not silently switch to another GPS reading. The retry action requests a fresh fix only when the user asks for it.
- Home food cards now visibly show the distance from that locked current location when the API provides it, so the position affects the post list as well as the map-side list.
- Food, search, explore, and reservation cards now lead to the post detail route preview instead of opening Google Maps with a potentially different origin. The detail map shows the current origin, exact pickup pin, selected travel mode, road distance, and estimated travel time before its Google Maps button opens directions from those same coordinates.
- The detail page uses one compact content panel in this order: food title, photo gallery, description, and pickup place name. The duplicate pickup/directions panel has been removed; the route map is shown once.
- Home map food pins still open their preview card with the food image, name, pickup place, and remaining quantity.
- Place search still prefers KKU-area matches, waits 600 ms before remote Photon lookup, caps the suggestion list, and keeps map/manual-coordinate fallbacks for offline use.
- Authentication, reservation and stock logic, QR scanner, database schema, and deployment configuration were not changed.

## Verification

- `node --test code/src/test/js/*.test.mjs`: all 9 JavaScript test files passed.
- `node --check` passed for the changed JavaScript modules.
- Maven backend tests could not run in this environment: Maven attempted to write its dependency cache under the read-only `/root/.m2` path. No backend files were changed.
