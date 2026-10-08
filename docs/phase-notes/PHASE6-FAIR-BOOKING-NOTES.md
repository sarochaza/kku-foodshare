# KKU FoodShare — Phase 6: Fair Booking & Time Extension

Phase 6 continues directly from the Phase 5 project. It keeps the existing PostgreSQL data, authentication, reservation, QR handover, Owner Management, map, place picker, and travel preview flows.

## What changed

### Fair booking limit

- A creator can optionally enable **“จำกัดจำนวนที่จองได้ต่อคน”** while creating or editing a post.
- Leaving it off means the old behaviour remains: there is no per-person limit.
- The limit must be from 1 to the post's total quantity.
- Reservation creation and reservation editing both enforce the limit on the server, not only in the browser.
- A creator cannot lower a limit below a quantity that an existing active reservation already has.
- The post detail and My Reservations quantity controls show the effective maximum, and the owner workspace shows the configured limit.

### Expired post with food left

- In **โพสต์ของฉัน**, an expired post with remaining food shows a clear prompt asking whether to extend pickup time.
- The owner can choose +30 minutes, +1 hour, +2 hours, or a custom local date/time.
- Only the owner can extend, only after the old pickup time has passed, and only when food remains.
- Reservations left over from the expired window are expired before the post is reopened; they are never revived accidentally.
- A post with no food left, a live post, or a cancelled post cannot be extended.
- Choosing **ไว้ก่อน** simply hides the editor; the extend option remains available later.

## Database compatibility

`V4__fair_booking_and_extension.sql` adds nullable `food_posts.max_per_person`.

- Existing posts receive `NULL`, which means **unlimited** and preserves their current behaviour.
- No existing reservation, food post, user, QR code, or location data is removed.

## Files changed

- `code/src/main/java/com/kku/foodshare/domain/entity/FoodPost.java`
- `code/src/main/java/com/kku/foodshare/dto/request/FoodPostRequest.java`
- `code/src/main/java/com/kku/foodshare/dto/request/ExtendPostRequest.java`
- `code/src/main/java/com/kku/foodshare/dto/response/PostView.java`
- `code/src/main/java/com/kku/foodshare/repository/ReservationRepository.java`
- `code/src/main/java/com/kku/foodshare/service/FoodCatalogService.java`
- `code/src/main/java/com/kku/foodshare/service/impl/FoodCatalogServiceImpl.java`
- `code/src/main/java/com/kku/foodshare/service/impl/ReservationServiceImpl.java`
- `code/src/main/java/com/kku/foodshare/controller/api/FoodCatalogController.java`
- `code/src/main/java/com/kku/foodshare/mapper/PostViewMapper.java`
- `code/src/main/resources/db/migration/V4__fair_booking_and_extension.sql`
- `code/src/main/resources/templates/editor.html`
- `code/src/main/resources/static/js/app.js`
- `code/src/main/resources/static/js/quick-actions.js`
- `code/src/main/resources/static/css/app.css`
- `code/src/test/java/com/kku/foodshare/FoodJourneyTest.java`
- `code/src/test/js/quick-actions.test.mjs`

## Build and run

From `kku-foodshare` (the folder that contains `compose.yaml`), keep your existing `.env` there and run:

```powershell
docker compose -p kku-foodshare-phase1 up --build -d
docker compose -p kku-foodshare-phase1 ps
```

The app remains available at:

- `http://127.0.0.1:8081`
- `http://localhost:8081`

Wait until the `app` service is `healthy`. Flyway applies V4 automatically and keeps existing database rows.

## Verification performed

```bash
cd code
./mvnw -o -B test
node --check src/main/resources/static/js/app.js
node --check src/main/resources/static/js/quick-actions.js
node --test src/test/js/*.test.mjs
```

Results in this delivery:

- Java: 59 tests passed.
- JavaScript: 7 test files passed.
- Added journey coverage verifies the per-person cap, cap editing safety, owner-only extension, live/empty post rejection, and successful reopening of an expired post with stock.
