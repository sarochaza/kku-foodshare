# Third-party assets

- **IBM Plex Sans Thai** — self-hosted TrueType files in `code/src/main/resources/static/fonts/`, distributed under SIL Open Font License 1.1. License: `fonts/OFL.txt`. Source: https://github.com/google/fonts/tree/main/ofl/ibmplexsansthai
- **Leaflet 1.9.4** — self-hosted JavaScript/CSS/marker assets in `static/vendor/leaflet/`. License: `static/vendor/leaflet/LICENSE.txt` (BSD 2-Clause). Source: https://leafletjs.com/
- **OpenStreetMap tiles** — fetched online, not bundled or bulk-prefetched. Credit is displayed on each map. Tile service policy: https://operations.osmfoundation.org/policies/tiles/ . Availability is best effort; use a suitable tile provider for production traffic and keep required attribution.
- Food photographs, mascot illustrations and existing profile assets were supplied in the original project archive. Optimized WebP copies preserve those assets. No new attribution or ownership claim is invented for them.
- Application icons are inline SVG paths in `templates/fragments.html`; no external icon CDN is needed.

Maven resolves framework libraries according to `code/pom.xml`; their licenses remain with their publishers. Browser test dependencies are development-only and are not loaded by the deployed app.
