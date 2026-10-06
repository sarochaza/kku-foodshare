# Phase 8 — Maps location correction

## What changed

- The Google Maps action on a post now changes from a pickup-only search to a real Google Maps directions URL after the reserver confirms the start pin.
- The URL preserves the selected mode and sends the exact confirmed origin (`lat,lng`) plus the exact food-post pickup destination (`latitude,longitude`).
- Before an origin is confirmed, the action opens a real Google Maps directions route to the exact pickup coordinates and leaves `origin` unspecified so Google Maps can use the device's current location. It never substitutes the map's center or a stale coordinate for the reserver.
- Reservation still requires the existing fresh-location/confirm flow (or the explicit existing skip choice). QR, stock, reservation API, map pins, filters, database, and deployment files were not changed.

## Regression coverage

- Added a route test that checks the Google Maps URL includes confirmed reserver origin, exact pickup destination, and driving mode.
- Front-end JavaScript syntax checks and all Node test files pass.
- Maven regression tests could not be executed in the packaging environment because Maven Central DNS was unavailable while resolving the project parent POM. This is an environment network limitation, not a test failure.
