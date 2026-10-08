# KKU FoodShare – Phase 9 UI compactness

- Compact the public homepage hero, login, and registration layouts for normal browser zoom.
- Replace category and post-owner chips on the dashboard and explore pages with accessible dropdowns; keep the availability checkbox and filter behavior.
- Remove unused About-page bottom-callout styles. The callout was already absent from the latest HTML.
- Keep `/` public and rendering the homepage; retain the existing authentication rules.

## Verification

- JavaScript regression tests: `node --test code/src/test/js/*.test.mjs`
- JavaScript syntax: `find code/src/main/resources/static/js -type f \( -name '*.js' -o -name '*.mjs' \) -print0 | xargs -0 -n1 node --check`

## Run on Windows PowerShell

From the extracted `kku-foodshare` folder, copy the local `.env` file if needed, then run:

```powershell
Copy-Item "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\Principle\kku-foodshare\.env" ".\.env" -Force
docker compose -p kku-foodshare-phase1 up --build -d
docker compose -p kku-foodshare-phase1 ps
```
