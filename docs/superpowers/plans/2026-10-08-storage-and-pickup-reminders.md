# Permanent food images and pickup reminders

**Goal:** Complete the three improvements the user accepted: durable food-post images, a usable password-reset deployment with Brevo, and an in-app warning before pickup closes.

**Approved scope:** Continue Phase 10.1. Preserve existing authentication, stock, reservation states, QR scanning, maps, themes, database records, and Compose project `kku-foodshare-phase1`. The user approved these three improvements; provider account registration will be guided during deployment.

**Design:** Extend the existing `ImageStorage` interface with an optional public delivery URL. A conditional Cloudinary adapter keeps the existing image validation/resize and filename contract, uses signed server-side HTTPS upload/delete, and serves older local images through the current adapter. No schema change. A pickup-reminder service writes into existing notifications with a stable reservation dedupe key, checks only active uncollected reservations due within 30 minutes, and locks only the reservation while checking/saving. A scheduled job and signed-in inbox/badge reads invoke the same service. Brevo's existing HTTPS email adapter and password reset flow remain in place, with an end-to-end transport test and a concrete registration/configuration guide.

**Execution:** Native implementation in the existing isolated checkout. Deliver code and tests in a new ZIP; leave the live Render service and Neon database untouched.

## Constraints and checks

- Default image provider stays local. Cloudinary credentials stay in environment variables, never JavaScript, Git, or the ZIP.
- Local/Cloudinary mixed post images must continue loading; cloud media links must still work after the app starts again.
- Invalid image bytes retain current JPG/PNG/5 MB/dimension checks. Provider failure must report an upload error, not pretend the picture was permanently saved locally.
- Rollback/remove uses the current transaction cleanup callback. Delete API failures are logged without exposing credentials.
- Only `RESERVED`, enabled users, open posts, and `now < availableUntil <= now + 30 minutes` qualify. Zero public stock still qualifies if someone has a reservation.
- One reminder per reservation, including concurrent scheduler/inbox requests. Never change stock or booking state.
- Render Free pauses when idle. The notification is in the existing web inbox; online inbox reads also check due reminders. This does not promise an operating-system push while the site is asleep.
- Brevo tests use a local HTTP provider fixture, not a real user inbox. Actual delivery remains a deployment check after sender verification/API-key setup.

## Task 1: Durable food-post images

Files: `service/storage/ImageStorage.java`, new `CloudinaryImageStorage.java`, new `config/ImageStorageConfiguration.java`, `controller/web/MediaController.java`, application properties, both base Compose files, `.env.example`, new storage transport/configuration tests.

- [x] Add HTTP-boundary tests for signed upload, sanitized/resized JPEG, restart-safe redirect, legacy local images, removal, invalid input, and provider failure.
- [x] Run targeted tests and verify failure from the missing storage capability.
- [x] Implement the conditional adapter, media redirect, and opt-in environment configuration without changing schema or the post-image API.
- [x] Run the storage tests and existing image/upload/QR regression tests; reject unknown provider values after the review finding.

## Task 2: Pickup reminder

Files: `repository/ReservationRepository.java`, new `service/impl/PickupReminderService.java`, new `PickupReminderJob.java`, `NotificationServiceImpl.java`, reservation deep-link handling in `app.js`, new `PickupReminderIntegrationTest.java`.

- [x] Test the 30-minute boundary, expired/collected/cancelled cases, disabled accounts, zero public stock, per-user inbox/badge, concurrent duplicate protection, and unchanged stock/QR pickup state.
- [x] Verify the missing reminder behavior before implementing.
- [x] Add bounded queries, reservation-row locking, notification writes, scheduler, and inbox catch-up.
- [x] Give each reservation card a stable DOM target and open the correct reservation from the reminder even on a later page.
- [x] Run Java and browser tests for the reminder journey and existing QR/owner management behavior.

## Task 3: Deployment and delivery

Files: new `PasswordResetBrevoJourneyTest.java`, `PHASE11-STORAGE-EMAIL-REMINDER-NOTES.md`, existing notes retained.

- [x] Verify the real password-reset flow through a local Brevo-compatible HTTP transport, one-use token, and old/new login.
- [x] Provide Cloudinary Free and Brevo registration/configuration instructions with official sources and no credentials in the examples.
- [x] Run the full Java/JavaScript suites, mobile/desktop checks, syntax check, package build, and diff check; fix a reproduced pre-existing timestamp-format assertion in FoodJourneyTest without altering extension behavior.
- [x] Package one `kku-foodshare/` directory, exclude `.env`, build output, dependency folders, and user uploads; include guarded PowerShell copy-env/build commands in the delivery notes.
