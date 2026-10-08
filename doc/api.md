# REST API contract

Base path `/api/v1` uses UTF-8 JSON and the same session as the web login. Mutating requests need the session CSRF token from HTML meta `_csrf` and `_csrf_header`. Public routes and protected routes are configured in SecurityConfig; ownership/state checks also run in services.

## Main CRUD resources

| Resource | Create | Read | Update | Delete |
|---|---|---|---|---|
| Food posts | POST /food-posts → 201 + Location | GET /food-posts and /food-posts/{id} | PUT /food-posts/{id} | DELETE /food-posts/{id} → 204; closes post and cancels pending bookings |
| Reservations | POST /food-posts/{id}/reservations → 201 | GET /reservations/{id}, /me/reservations | PUT /reservations/{id} | DELETE /reservations/{id} → 204; cancels without deleting history |
| Comments | POST /food-posts/{postId}/comments → 201 + Location | GET /food-posts/{postId}/comments and /comments/{id} | PUT /comments/{id} → 200; author only | DELETE /comments/{id} → 204; soft delete and retain replies |

Comment create accepts `{body:"ข้อความ",parentCommentId:123}` (omit parentCommentId for a root comment).
Update accepts only `{body:"ข้อความใหม่"}` and preserves author, post, creation time and reply links.
Body is required, nonblank and at most 800 characters. `canEdit` is true only for the author;
`canDelete` remains true for the author, post owner or admin. Deleted/missing comments return 404.
Comment list returns 30 items per page, ordered by createdAt then id ascending, and uses the existing PageView format.
See [Comment CRUD and Docker test guide](comment-crud.md).

Create reservation body is `{quantity:2}` with an `Idempotency-Key`. Retrying the same request/key returns the existing booking; using the key for a different post or quantity returns 409. Pickup collection body is `{code:"123456"}` and requires the food-post owner.

## Validation, pagination, sorting and errors

FoodPostRequest uses Bean Validation and service-level quantity/time/location rules. Dates for pickup are Asia/Bangkok local time. Reset token timestamps use Instant internally. Post create/edit can set maxPerPerson; null means no configured per-person cap.

`GET /food-posts?q=ข้าว&category=FOOD&sort=expiry&now=true&page=0&size=12` supports pagination and `expiry/latest/nearby` sorting. Nearby requires lat/lng. Page starts at 0 and size is at most 200. Response uses `{items:[],page:0,totalPages:1,totalElements:0}`.

ApiExceptionHandler returns `{status,message,fields}`. Statuses include 400 malformed/validation, 401 login required, 403 permission/CSRF, 404 missing resource, 409 stock/state/version conflict, 413 oversized upload, 422 unreadable QR, 429 throttling and 500 unexpected failure. Security boundary responses can omit fields; web errors render HTML.

Gallery uploads use multipart `file` on the image endpoint, preserving existing JPG/PNG size/pixel validation. Removing an image checks ownership. Profile GET `/members/{id}` returns only `{id,name}` and returns 404 for missing or inactive accounts. API DTOs avoid sending password/reset-token/pickup-code internals to strangers.

## Swagger/OpenAPI

`/swagger-ui/index.html` (or `/swagger-ui.html`) and `/v3/api-docs` are configured publicly. For protected mutations in Swagger, use an authenticated session with its CSRF token. A configured route is not proof that the submitted commit is deployed; check the public URLs before presentation.

## Endpoint inventory

Paths below are extracted from controller annotations of this source version. Controller names identify where to inspect status codes, validation and service calls. Legacy `/api/food-posts/map` is retained for compatibility with existing clients.

| Method | Path | Controller |
|---|---|---|
| GET | `/api/v1/food-posts/{postId}/comments` | `CommentController` |
| POST | `/api/v1/food-posts/{postId}/comments` | `CommentController` |
| GET | `/api/v1/comments/{id}` | `CommentController` |
| PUT | `/api/v1/comments/{id}` | `CommentController` |
| DELETE | `/api/v1/comments/{id}` | `CommentController` |
| GET | `/api/v1/food-posts` | `FoodCatalogController` |
| GET | `/api/v1/food-posts/map` | `FoodCatalogController` |
| GET | `/api/v1/food-posts/{id}` | `FoodCatalogController` |
| POST | `/api/v1/food-posts` | `FoodCatalogController` |
| PUT | `/api/v1/food-posts/{id}` | `FoodCatalogController` |
| POST | `/api/v1/food-posts/{id}/extend` | `FoodCatalogController` |
| DELETE | `/api/v1/food-posts/{id}` | `FoodCatalogController` |
| POST | `/api/v1/food-posts/{id}/images` | `FoodCatalogController` |
| DELETE | `/api/v1/food-posts/{id}/images/{imageId}` | `FoodCatalogController` |
| GET | `/api/v1/me/posts` | `FoodCatalogController` |
| GET | `/api/v1/members/{ownerId}/posts` | `FoodCatalogController` |
| GET | `/api/v1/me/posts/management-summary` | `FoodCatalogController` |
| GET | `/api/v1/stats` | `FoodCatalogController` |
| GET | `/api/food-posts/map` | `FoodPostApiController` |
| GET | `/api/v1/members/{id}/photo` | `MemberPhotoController` |
| GET | `/api/v1/members/{id}` | `MemberProfileController` |
| POST | `/api/v1/reports` | `ModerationController` |
| GET | `/api/v1/admin/pending-reports` | `ModerationController` |
| GET | `/api/v1/admin/posts` | `ModerationController` |
| GET | `/api/v1/admin/reports` | `ModerationController` |
| PATCH | `/api/v1/admin/reports/{id}` | `ModerationController` |
| GET | `/api/v1/admin/users` | `ModerationController` |
| PATCH | `/api/v1/admin/users/{id}` | `ModerationController` |
| GET | `/api/v1/food-posts/{id}/stock` | `OwnerStockController` |
| POST | `/api/v1/food-posts/{id}/stock` | `OwnerStockController` |
| POST | `/api/v1/pickup/scan` | `PickupScanController` |
| GET | `/api/v1/food-posts/{id}/owner-photo` | `PostProfileController` |
| PATCH | `/api/v1/me/profile` | `ProfileSettingsController` |
| POST | `/api/v1/me/onboarding` | `ProfileSettingsController` |
| POST | `/api/v1/food-posts/{id}/reservations` | `ReservationController` |
| GET | `/api/v1/food-posts/{id}/reservations` | `ReservationController` |
| GET | `/api/v1/food-posts/{id}/my-reservation` | `ReservationController` |
| GET | `/api/v1/reservations/{id}` | `ReservationController` |
| PUT | `/api/v1/reservations/{id}` | `ReservationController` |
| DELETE | `/api/v1/reservations/{id}` | `ReservationController` |
| POST | `/api/v1/reservations/{id}/collection` | `ReservationController` |
| GET | `/api/v1/me/reservations` | `ReservationController` |
| GET | `/api/v1/me/notifications` | `ReservationController` |
| POST | `/api/v1/me/notifications/{id}/read` | `ReservationController` |
| GET | `/api/v1/me/notifications/unread` | `ReservationController` |
| GET | `/api/v1/me/notification-preferences` | `ReservationController` |
| PUT | `/api/v1/me/notification-preferences` | `ReservationController` |
| GET | `/api/v1/reservations/{id}/member-photo` | `ReservationMediaController` |
| PUT | `/api/v1/food-posts/{id}/saved` | `SavedPostController` |
| DELETE | `/api/v1/food-posts/{id}/saved` | `SavedPostController` |
| GET | `/api/v1/me/saved-posts` | `SavedPostController` |
