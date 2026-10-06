# Phase 8 — Social Feed

## What changed

- Home now renders shareable food posts as an image-forward feed while leaving filters, live map, current location and reservation links intact.
- A post can retain its old single image or have up to five JPG/PNG images. Images have stable display order and can be removed by the post owner through the image API.
- Post detail has a responsive image gallery and a flat comment area. Guests can read comments; signed-in members can add them. Authors, post owners and admins can remove unsuitable comments.
- The existing report flow now also accepts comment reports and stops duplicate open reports for the same comment.
- The notification bell displays the real unread count. Members can save category and keyword/location interests; matching new posts create one in-app notification per post/member.

## Refinement after UI review

- Removed the Facebook-like “what would you like to share?” feed prompt. Food cards remain focused on the food, pickup, stock and reservation action.
- Reduced the visible home filters and made “รับได้ตอนนี้” a compact separate control; all original filter and sorting behavior remains available.
- Moved the comment thread immediately after the image gallery on post detail. Home cards show a compact first-comment preview that links to the full thread.
- Comments now notify the post owner with the commenter’s profile image, name and comment preview. Notification preferences collapse into one small expandable row and notification rows are compact.
- Notifications are grouped in the UI as reservations, comments, interested food and other updates. Mobile layouts compact reservation cards, booking panels and owner-management controls without changing reservation or stock logic.
- Home uses an image-led equal-card gallery layout: three columns on desktop, responsive columns on smaller screens, and a fixed square image area with `object-fit: cover` for every post regardless of source image dimensions.
- Search now uses the equal-card gallery layout and includes a compact owner filter: all posts, my posts, or other members’ posts. The map view has a full map plus a compact nearby-food side list. Each card displays the owner avatar and links to a public member page listing that member’s non-cancelled sharing history.
- Search and map initialization now tolerate a page that does not include optional map-only controls, so a missing map expansion button cannot stop the food feed from loading.

## Database migration

`V5__social_feed.sql` preserves existing data. It removes the old one-image unique constraint, adds image order, comment and preference tables, and optional notification/report fields.

## Run locally

From the folder that contains `compose.yaml`:

```powershell
$envFile = Get-ChildItem "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\KKU-FoodShare-Phase6-FairBooking-Extension" -Filter .env -Recurse | Select-Object -First 1
Copy-Item $envFile.FullName ".\.env" -Force
docker compose -p kku-foodshare-phase1 up --build -d
```

Open `http://127.0.0.1:8081`.

Do not commit or include `.env` in a hand-off ZIP.

## Verification performed

- `node --check src/main/resources/static/js/app.js`
- `node --check src/main/resources/static/js/ui.js`
- `node --test src/test/js/*.test.mjs` — 9 test files passing

The current packaging environment does not contain Maven, so `mvn -o -B test` and the package command must be run in the normal project/Docker environment before release.
