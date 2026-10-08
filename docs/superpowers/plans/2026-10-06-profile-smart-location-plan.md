# Profile Popover and KKU Smart Location Search Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the full-page-first account access with a compact header profile popover and make pickup-place suggestions immediate, KKU-first, and free to deploy.

**Architecture:** Keep all existing Spring routes and forms. The header becomes an accessible client-side popover that links to existing pages and posts to the existing logout endpoint. The place picker gets a local KKU/Khon Kaen search index for instant suggestions; remote Photon lookup becomes an explicit, cached fallback action instead of search-as-you-type.

**Tech Stack:** Thymeleaf, vanilla ES modules, Leaflet, existing Spring Security logout form, Node built-in test runner, Maven/JUnit.

**Spec:** `docs/superpowers/specs/2026-10-06-profile-smart-location-design.md`

## Global Constraints

- Do not alter authentication, Spring Security logout handling, reservation logic, QR pickup, database schema, Docker Compose, or `.env`.
- Preserve `pickupLocationName`, `latitude`, and `longitude` submission behavior.
- Keep Leaflet/OSM and the blue-green visual theme; do not add a paid API or API key.
- Existing `/account` remains the profile editing route.
- All remote place fallback calls are explicit user actions; suggestions while typing must be local only.

## Review Focus

- A keyboard user can open the profile popover, use its links, and dismiss it with Escape; owned by Task 2 tests.
- Clicking outside the popover closes it without navigating; owned by Task 2 tests.
- A local KKU alias such as `library`, `หอสมุด`, or `complex` returns the intended result before any network call; owned by Task 1 tests.
- A non-local query makes no external call until the explicit fallback button is activated; owned by Task 1 tests.
- A failed fallback or reverse request still leaves a valid map/manual-coordinate path; owned by Task 3 regression tests.

---

### Task 1: Local KKU/Khon Kaen place-search engine

**Files:**
- Modify: `code/src/main/resources/static/js/places.mjs`
- Modify: `code/src/test/js/place-picker.test.mjs`

**Interfaces:**
- Produces: `localPlaces(query: string): Place[]`, `searchRemotePlaces(query: string, signal: AbortSignal, fetcher?: typeof fetch): Promise<Place[]>`, and retained `placePicker(form, map, locate)`.
- Consumes: existing `validCoordinates`, map pin callback, and cached remote request behavior.

- [ ] **Step 1: Write failing local-search tests**

Add assertions that `localPlaces('หอสมุด')`, `localPlaces('library')`, and `localPlaces('complex')` return a KKU result first; assert an unrelated query returns an empty local list without invoking a supplied fetcher.

- [ ] **Step 2: Run the picker test to verify it fails**

Run: `node --test src/test/js/place-picker.test.mjs`

Expected: FAIL because `localPlaces` does not exist.

- [ ] **Step 3: Implement the local catalogue and normalized ranking in `places.mjs`**

Create a small immutable KKU/Khon Kaen place array with Thai/English aliases and coordinates. `localPlaces(query)` must normalize case and whitespace, then rank exact aliases before prefixes and KKU entries before other Khon Kaen entries. Limit visible suggestions to five.

- [ ] **Step 4: Write failing explicit-fallback tests**

Assert that input processing returns local results without calling `searchRemotePlaces`; assert the remote function calls Photon once, country-limits to Thailand, caches a successful result, and preserves the existing KKU bias.

- [ ] **Step 5: Implement `searchRemotePlaces` and make remote lookup explicit**

Rename/reuse the current network search implementation behind `searchRemotePlaces`. In the picker, render local results during input, and render a fallback button only when appropriate. The button invokes `searchRemotePlaces`; no timer may invoke it automatically.

- [ ] **Step 6: Run picker tests to verify they pass**

Run: `node --test src/test/js/place-picker.test.mjs`

Expected: PASS.

- [ ] **Step 7: Commit checkpoint**

If this project is restored in a Git worktree, commit with `feat: add local KKU place suggestions`; otherwise record this as the first ZIP delivery checkpoint.

### Task 2: Compact header profile popover

**Files:**
- Modify: `code/src/main/resources/templates/fragments.html`
- Modify: `code/src/main/resources/static/js/app.js`
- Modify: `code/src/main/resources/static/css/app.css`
- Modify: `code/src/test/java/com/kku/foodshare/WebPagesTest.java`
- Create/Modify: `code/src/test/js/profile-menu.test.mjs`

**Interfaces:**
- Consumes: the existing model variable `viewer`, `/account/photo`, current routes, CSRF meta tags, and `POST /logout`.
- Produces: `initProfileMenu(): void` in `app.js`, invoked globally only when the header menu exists.

- [ ] **Step 1: Write failing template and behavior tests**

Add page assertions for a profile-menu button, a hidden menu with `/reservations`, `/account/posts`, `/notifications`, `/account`, and a POST logout form. Add DOM-stub tests that assert toggle, Escape close, and outside-click close.

- [ ] **Step 2: Run the focused tests to verify they fail**

Run: `mvn -o -B -Dtest=WebPagesTest test` and `node --test src/test/js/profile-menu.test.mjs`

Expected: FAIL because the popover markup and initializer are absent.

- [ ] **Step 3: Implement semantic popover markup in `fragments.html`**

Replace only the signed-in desktop avatar link with a button and hidden menu. Include photo, member name, four existing-route links, and the existing protected logout form. Keep mobile navigation and the `/account` page unchanged.

- [ ] **Step 4: Implement `initProfileMenu(): void` in `app.js`**

Wire button `aria-expanded`, menu visibility, Escape, click-outside dismissal, and focus return to the avatar button. Do not add a backend API or change logout submission.

- [ ] **Step 5: Add responsive blue-green popover CSS**

Give the menu an anchored desktop position, touch-sized links, a mobile-safe viewport width, visible focus state, and a distinct but non-destructive logout action. Do not hide the existing mobile bottom navigation.

- [ ] **Step 6: Run focused tests to verify they pass**

Run: `mvn -o -B -Dtest=WebPagesTest test` and `node --test src/test/js/profile-menu.test.mjs`

Expected: PASS.

- [ ] **Step 7: Commit checkpoint**

If Git is available, commit with `feat: add quick profile menu`; otherwise record it as the second ZIP delivery checkpoint.

### Task 3: Picker UX integration and regression verification

**Files:**
- Modify: `code/src/main/resources/templates/editor.html`
- Modify: `code/src/main/resources/static/css/app.css`
- Modify: `code/src/test/js/place-picker.test.mjs`
- Modify: `test/quick-actions-journey.cjs` only if its existing browser fixtures require updated picker selectors

**Interfaces:**
- Consumes: Task 1 `localPlaces` and `searchRemotePlaces`, current map callback, current hidden inputs, and the existing advanced-coordinate controls.
- Produces: an editor where local place selection, remote fallback selection, map changes, and manual coordinates all retain the same post payload.

- [ ] **Step 1: Write failing integration assertions**

Add test cases for local suggestion selection updating `pickupLocationName`, hidden latitude/longitude, and map pin; fallback failure retaining map/manual controls; and reverse-name failure not clearing a manually edited pickup name.

- [ ] **Step 2: Run the picker integration test to verify it fails**

Run: `node --test src/test/js/place-picker.test.mjs`

Expected: FAIL before the new picker states and explicit-fallback control are wired.

- [ ] **Step 3: Update picker copy and interaction states**

Adjust editor help text and result rendering to say that KKU/Khon Kaen suggestions are instant, and expose the explicit fallback button. Keep result-list keyboard navigation, touch targets, debounce only for local rendering, and all current map/manual fallback controls.

- [ ] **Step 4: Add narrow-screen CSS constraints**

Ensure the result list, fallback control, and profile menu fit at 320 px without horizontal overflow and remain at least 44 px high where interactive.

- [ ] **Step 5: Run full automated verification**

Run: `mvn -o -B test && node --check src/main/resources/static/js/app.js && node --check src/main/resources/static/js/places.mjs && node --test src/test/js/*.test.mjs`

Expected: all Maven tests and all JavaScript test files pass.

- [ ] **Step 6: Build the deliverable**

Run: `mvn -o -B -DskipTests package`

Expected: Spring Boot JAR packages successfully; then prepare a Phase 7 ZIP without `.env`, `target`, uploads, or test dependency folders.

- [ ] **Step 7: Commit checkpoint**

If Git is available, commit with `feat: streamline profile and location search`; otherwise package the tested source as the final ZIP checkpoint.

## Self-review

- Spec coverage: Task 1 covers KKU-first local search and explicit free fallback; Task 2 covers quick profile access and logout; Task 3 covers data contract, errors, mobile fit, and regressions.
- Type consistency: Task 1 exports the exact functions consumed by Task 3; Task 2 owns only `initProfileMenu` and existing routes.
- Scope: no new API, database change, Docker change, or authentication change is included.
- Review focus: all five listed interaction risks are assigned to explicit tests.
