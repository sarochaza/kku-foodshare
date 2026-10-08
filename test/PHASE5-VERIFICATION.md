# Phase 5 verification — 6 October 2026

Tested the actual modified project, continuing Phase4.1 QuickPreview. Test records were created only in an isolated in-memory H2 database, not the user's database.

## Automated checks

| Check | Result |
|---|---|
| Original and new Maven/JUnit suite | 55 passed, 0 failed/errors/skipped |
| Production JAR package | BUILD SUCCESS |
| Original and new Node regression suite | 28 passed |
| JavaScript syntax | app.js, quick-actions.js and browser scripts passed |
| Production StockPolicy executable checks | Java17 compile and exhaustive small-stock invariants passed |
| New quick-actions browser journey | 14 checks passed; no pageerrors |
| Original browser journey | 17 checks passed, 20 responsive combinations, no pageerrors |
| New responsive layouts | 13 combinations passed (320–1366px) |
| Homepage hidden filter regression | 360px width, onboarding and scroll-to-last-filter passed |

Backend tests cover self-only active booking lookup, edit on a full post, same reservation/code preservation, owner-only adjustments, stale/duplicate request rejection, offline corrections, combined offline+QR completion, reopen after adding food, edit floor including offline stock, discovery exclusion and concurrent reservation/offline distribution. Migration test applies the actual V3 SQL to a legacy-shaped table in H2 PostgreSQL mode and verifies row preservation/default0/combined constraints.

New UI tests cover stock preview boundaries, steppers, late refresh rejection, capturing the confirmed snapshot version and lost-create-response/cancel/rebook keys. The last two were observed failing before fixing the reviewed issues.

Browser tests use real HTML/JavaScript/Leaflet and a real Spring Boot server with H2. External tile/geocoder/route fixtures are deterministic. They do not establish live external-provider availability. Browser used Chromium131 and Playwright1.62.1; Node24 and Java17. Docker is unavailable in the execution environment; the user's Compose/PostgreSQL deployment was not run here. Deployment files are byte-identical to Phase4.1.

Evidence:

- `PHASE5-junit-results.csv`
- `../img/phase5/browser-result.json` (new journey)
- `../img/browser-result.json` (original journey)
- `../img/phase5/owner-stock-mobile.png`
- `../img/phase5/inline-reservation-mobile.png`

## Run again

From `kku-foodshare/code` with Java17 installed:

```powershell
.\mvnw.cmd test
.\mvnw.cmd -DskipTests package
```

Linux:

```bash
./mvnw test
./mvnw -DskipTests package
```

From `kku-foodshare` with Node installed:

```bash
node --test code/src/test/js/*.test.mjs
node --check code/src/main/resources/static/js/app.js
node --check code/src/main/resources/static/js/quick-actions.js
```

Browser journeys create labelled users and posts. Run against an isolated test/staging database only. Install the existing test dependencies and browser:

```powershell
cd test
npm install
npx playwright install chromium
$env:TEST_BASE_URL="http://127.0.0.1:8081"
node quick-actions-journey.cjs
node home-overflow-check.cjs
node browser-journey.cjs
```

The quick-actions journey closes its test post in finally. The original journey closes its test post after success. Test accounts/post history remain for inspection in the test database. The scripts use existing registration, login, CSRF and API paths; no testing endpoint or production bypass was added.

## User deployment

Run inside the new version's `kku-foodshare`, with the same `.env` as the current QuickPreview version:

```powershell
docker compose -p kku-foodshare-phase1 up --build -d
docker compose -p kku-foodshare-phase1 ps
```

Open http://127.0.0.1:8081 or http://localhost:8081. Refresh with Ctrl+F5 after the application is healthy. Keep the project name and original secrets; do not use down -v. Flyway applies V3 automatically without deleting existing data.
