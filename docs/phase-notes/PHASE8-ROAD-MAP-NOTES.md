# Phase 8: Road distance and map preview

## Changes

- Food cards on the home and explore pages request car-road distances from the current session location through the existing OpenStreetMap routing service. Distances for several cards are requested in one OSRM Table request and cached briefly in the browser.
- The cards no longer present API straight-line distance as if it were road distance. If the routing service is unavailable, the UI says road distance is unavailable.
- Selecting a map marker or item in Explore opens a food preview with photo, title, pickup point, remaining quantity, road-distance status, and a details link.
- The map control expands the map and changes to “ดูข้อมูล” while expanded. Pressing it restores the results panel.
- Mobile preview styles keep the map card within the map viewport.

## Notes

- Uses the same car routing profile as the route panel; no API key, backend endpoint, database change, or deployment configuration change is required.
- Road estimates need permission for the browser to provide the current location. Map viewing and selecting food pins continue to work without it.
- OSM routing availability depends on its public routing service and internet connectivity.
