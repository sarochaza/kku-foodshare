# Phase 9 Visual Polish

## Changes

- Moved “เกี่ยวกับเรา” to the header beside notifications and gave it a distinct information icon.
- Replaced the signed-in mobile About tab with “โพสต์ของฉัน”; kept the central share button and hid the duplicate owner shortcut on phones.
- Simplified the guest landing page on phones by removing duplicate calls to action, reassurance chips, and floating image cards. Desktop keeps its two-column hero composition, with About available from the header.
- Removed the three decorative heading lines from the account screen.
- Removed the back link and decorative introduction from create/edit post screens, and the decorative heading and description from notifications while keeping all controls and notification data.
- Added three app-wide themes in the signed-in profile menu: blue-green (default), monochrome, and dark background. The selection is saved in browser storage; food photos and map tiles keep their original colors. Active navigation keeps the normal underline indicator in monochrome and dark themes.
- The root URL `/` opens the landing/home page for both guests and signed-in users. The separate `/home` dashboard and authentication flow remain available.
- Added subtle blue-green background drift and disabled it when the device requests reduced motion.
- No authentication flow, reservations, data, or deployment settings were changed.

## Run on Windows PowerShell

Extract the ZIP, then run these commands from the inner `kku-foodshare` folder:

```powershell
cd "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\Principle\KKU-FoodShare-Phase9-Theme-Update\kku-foodshare"
if (!(Test-Path .\.env)) { Copy-Item "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\Principle\kku-foodshare\.env" ".\.env" }
docker compose -p kku-foodshare-phase1 up --build -d
docker compose -p kku-foodshare-phase1 ps
```

The ZIP does not include `.env`. Compose uses the existing project name and database volume.

## Verification

- `node --test code/src/test/js/*.test.mjs` — 19 tests passed.
- `find code/src/main/resources/static/js -type f \( -name '*.js' -o -name '*.mjs' \) -print0 | xargs -0 -n1 node --check` — passed.
- The ZIP archive integrity was checked and it contains no `.env` file.
- `./mvnw -B -Dtest=HomeControllerTest test` could not run because Maven Central did not provide the wrapper distribution in this environment.
- Docker runtime was not started in the workspace; use the run commands below on Windows.
