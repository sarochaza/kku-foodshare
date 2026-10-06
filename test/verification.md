# Final verification — 2026-10-06

## Executed checks

| Check | Result |
|---|---|
| Java 17 Maven `clean verify` | PASS — 44 tests, 0 failures, 0 errors, 0 skipped |
| Executable Spring Boot JAR | Build successful |
| Regression: stale profile update vs suspension | Observed failure before fix, passes after User versioning |
| Regression: SMTP outage vs account enumeration | Observed failure before fix, passes after exception-contract correction |
| Reservation integration suite | 15 cases, including concurrent last-stock race, idempotency, IDOR, cancel/return, pickup-code lock, upload and admin/suspension |
| Browser journey | PASS — registration, login, image/post/pin, search/map, reservation/quantity, pickup, profile, notifications, map/GPS errors, modal cancellation |
| Responsive layouts | 20 page/viewport checks at 360, 390, 768, 1366 px; no horizontal overflow |
| Browser JavaScript errors | 0 |
| Online map | 6 real OpenStreetMap tiles loaded; coordinate marker and mobile editor checked |
| OpenAPI | `/v3/api-docs` HTTP 200 and catalog path present |
| Flyway V1/V2 + Hibernate schema validation | PASS with PGlite PostgreSQL engine |
| Persistence after stopping application and reopening DB | Collected reservation/stock retained; uploaded image still exists |
| Setup shell script | Random secrets generated; rerun preserves existing `.env` |
| JavaScript and shell syntax | PASS |

Detailed JUnit totals are in `junit-results.csv`. Browser evidence is in `../img/browser-result.json` and screenshots. Test records are clearly labelled and are not bundled as production database contents. The test food post was closed after the browser journey.

## Scope and limits

H2 runs JUnit integration tests. Browser/database smoke uses PGlite PostgreSQL 18.3 engine via JDBC because a standard PostgreSQL daemon and Docker daemon were unavailable in this execution environment. The local harness uses one runtime connection, simple query protocol and Flyway session locking to fit PGlite's multiplexer; these test-only options are not placed in production configuration. Standard PostgreSQL 17 concurrency and Docker build are configured in CI but have not been executed here.

Browser external map requests used the environment's allowed network proxy for online-tile verification. Failure recovery was tested separately with blocked tiles. No fabricated tile image or fake API response was supplied.

Google OAuth, real SMTP delivery, public DNS/TLS and a production Docker deployment require the project owner's accounts/credentials and have not been verified against those external accounts. PowerShell setup was inspected but not executed in this Linux environment.

This package is deployment-ready source/configuration, not evidence of an already-live production deployment.
