# KKU FoodShare Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans inline, task by task. A fresh reviewer checks the complete implementation at the end.

**Goal:** Finish the approved food sharing and reservation website with polished responsive UI and a reproducible deployment package.

**Architecture:** Spring Boot/Thymeleaf application with session authentication, REST DTOs, PostgreSQL transactions and persistent image storage. Preserve the existing account implementation where sound, extend through Controller → Service → Repository boundaries.

**Tech Stack:** Java 17, Maven, Spring Boot, Spring Security, JPA, PostgreSQL, Flyway, Thymeleaf, Leaflet, JUnit, Mockito, Docker.

**Spec:** ../specs/2026-10-05-kku-foodshare-design.md

## Global Constraints

- Thai UI, free food sharing, no payment/delivery/native application.
- A member may post and reserve, but cannot reserve their own post.
- Reservation states: RESERVED, COLLECTED, CANCELLED, EXPIRED; terminal states cannot revert.
- Quantity is a positive integer; all mutation paths lock the food post first.
- Original ZIP is preserved; work is in its extracted isolated copy, not a shared branch.
- No production secrets, simulated success, invented contributors or test results.
- Database is PostgreSQL; H2 can only be a supplemental test tool, never the deployed database.

## Review Focus

1. Two users racing for the last item must produce exactly one reservation.
2. Cancellation/retry/expiry must never inflate remaining quantity.
3. Account or resource IDs supplied by another member must not bypass authorization.
4. Maps and forms remain usable when location permission or tile delivery fails.
5. Uploads cannot escape storage paths or expose dangerous content; database restart retains records.

## Task 1: Reproducible baseline and database

**Files:** code/pom.xml; code/mvnw; code/src/main/resources/application*.properties; db/migration/V1__foodshare.sql; test configuration; Dockerfile; docker-compose.yml.

**Interfaces:** PostgreSQL datasource from env; Clock bean UTC; authenticated email resolves one active user; Flyway owns schema.

- [ ] Run the original test/build command and record observed failure.
- [ ] Repair wrapper line endings and select compatible, resolvable dependencies.
- [ ] Add database schema and environment-based profiles; Google and SMTP are optional integrations.
- [ ] Verify compilation and migration on an available real PostgreSQL instance; log any environment blocker explicitly.

## Task 2: Posting, storage, and discovery

**Files:** FoodPost entity/repository/service/mapper; FoodPostRequest and FoodPostResponse; FoodPostApiController; image storage service; discovery strategies.

**Interfaces:** `FoodPostService.create(String email, FoodPostRequest request)`; `update(String email, long id, FoodPostRequest request)`; `delete(String email, long id)`; `search(...)`; DTO carries available quantity and real image URL.

- [ ] Write API/service tests: valid create persists coordinates; invalid times/quantity fail; stranger cannot edit/delete; unsafe image fails.
- [ ] Observe the missing behavior failing, then implement validated CRUD, bounded paging and owner checks.
- [ ] Implement map bounds, category/search/expiry/nearby discovery, storage validation and orphan cleanup.
- [ ] Run the posting and storage tests; verify through HTTP against the running application when available.

## Task 3: Reservation state and notifications

**Files:** Reservation entity/repository/DTO/controller/service; reservation state policies; notification events/listeners; expiry job.

**Interfaces:** `reserve(email, postId, quantity, key)`; `changeQuantity(email, reservationId, quantity)`; `cancel(email, reservationId)`; `collect(email, reservationId, code)`; all return owner-safe DTOs.

- [ ] Write tests: 5 minus 2 leaves 3; cancel returns 5 once; last-item race has one winner; own-post booking forbidden; repeated collect does not increase totals; expired collection denied.
- [ ] Observe failures, implement lock-first transactions, idempotency and state transitions.
- [ ] Add hashed pickup codes with rate limiting, private retrieval for the recipient, and durable notifications.
- [ ] Test malformed identifiers, authorization boundaries, updates and owner-driven cancellation.

## Task 4: Responsive UI and complete flows

**Files:** shared fragments and theme; home/explore/detail/editor/reservations/my-posts/account/auth templates; focused JS modules; optimized existing media.

**Interfaces:** templates consume page DTOs and REST APIs above; CSRF token supplied to every mutation; map editor writes lat/lng form values.

- [ ] Add browser flow checks for account → create → map pin → reserve → collect and denial of location permission.
- [ ] Replace static cards/stats and dead links with real data and functional actions.
- [ ] Build consistent blue/green visual system with expressive food cards, spacious layout, responsive navigation and accessible forms.
- [ ] Inspect screenshots at 360/390/768/1366 widths, fix overflow and verify the main journey with two accounts.

## Task 5: Moderation, API documentation, and delivery

**Files:** Report/Audit entities and services; admin views/API; exception advice; OpenAPI configuration; README; doc/*; test/*; CI.

**Interfaces:** admin service authorizes active ADMIN, records actions and cancels affected live reservations; API error shape is consistent.

- [ ] Write tests that normal members cannot moderate, suspension blocks subsequent protected actions, and owner-only histories remain private.
- [ ] Implement moderation and reporting plus Swagger for actual contracts.
- [ ] Write diagrams, data dictionary, SOLID/pattern mapping, setup/deploy instructions, test report and presentation outline from final code.
- [ ] Run full tests/build/security flow and image/layout checks; obtain independent review and fix important findings with regression tests.
- [ ] Package the updated ZIP, preserve its original Library identity, and report precise verification status and external configuration requirements.
