# KKU FoodShare — API Reference

Reference date: 2026-10-09. Based on the Phase 12 source available for this review.

This document describes the implemented FoodShare API. Examples are illustrative; they are not captured production responses. Test evidence and remaining runtime checks are listed in [Verification](#verification).

## Base URL and formats

| Environment | API base URL |
|---|---|
| Local application / default Compose port | `http://localhost:8080/api/v1` |
| Local Docker when `APP_PORT=8081` | `http://localhost:8081/api/v1` |
| Public deployment URL supplied by the project owner | `https://kku-foodshare.onrender.com/api/v1` |

Use the port shown by `docker compose ps`. Compose publishes `APP_PORT`, defaulting to 8080. The public deployment's running revision has not been independently confirmed in this review.

- Resource IDs are numeric Java `long` / `Long` values, not UUIDs.
- JSON request and response names use camelCase. JSON strings use UTF-8.
- API timestamps shown here use ISO local date-time, for example `2026-10-10T12:00:00`. Pickup times and the application clock use **Asia/Bangkok**. These fields are `LocalDateTime`, without a `Z` or UTC offset; do not reinterpret them as UTC.
- Requests with a JSON body use `Content-Type: application/json`. Upload routes use `multipart/form-data`.
- Success `204` has no response body. Create-food-post, create-comment and create-reservation responses include a `Location` header.
- Access and business-state checks apply in addition to validation.

## Access

| Name | Meaning |
|---|---|
| Public | No login needed for the specifically permitted GET routes. |
| Signed in | An authenticated, enabled account; roles are `USER` and `ADMIN`. |
| Post owner | The enabled account that owns the food post. |
| Comment author | The enabled account that wrote the comment. |
| Comment moderator | Comment author, food-post owner, or `ADMIN`; applies to deletion. |
| Reservation member | The enabled account that owns the reservation. |
| Reservation participant | Reservation member or food-post owner. |
| Admin | An enabled account with role `ADMIN`. |
| Current account | The signed-in account; `/me` routes operate on that account. |

All API writes require CSRF. Being an admin does not automatically grant another member's comment-editing, post-editing or reservation-reading permissions.

The four public API routes are `GET /food-posts`, `GET /food-posts/map`, `GET /food-posts/{id}` and `GET /stats`. Other API routes require login. A cancelled food post can only be read through the detail endpoint by its owner or an admin; other readers receive 404.

## Session and CSRF

FoodShare uses Spring Security sessions. Login is a form request on the application root, not a JSON API request. There is no JWT login or `/api/v1/csrf` endpoint in the reviewed source.

1. GET the application-root `/login` page and retain its session cookie.
2. Read the rendered HTML meta values `_csrf` and `_csrf_header`.
3. POST to application-root `/login` using form fields **email** and **password**, the cookie, and the CSRF header.
4. A successful form login redirects to `/home`. Fetch `/home` with the updated cookie and read its fresh CSRF meta values.
5. Send the session cookie on protected requests. Send the current CSRF token on POST, PUT, PATCH and DELETE.
6. POST to application-root `/logout` with the cookie and CSRF token to sign out.

A CSRF token is not necessarily single-use. Refresh it after authentication or when the session changes. Use the actual `_csrf_header` meta value; it is normally `X-CSRF-TOKEN`.

### Windows PowerShell example

Use `curl.exe` so Windows PowerShell does not resolve `curl` to a different command. Run these commands sequentially. Replace the email, password and token placeholders; read tokens from the downloaded HTML or browser developer tools. Cookie files contain session credentials and should stay outside Git.

```powershell
$foodshareBase = "http://localhost:8080"

# Obtain cookie and pre-login HTML token.
curl.exe -sS -c cookies.txt -o login.html "$foodshareBase/login"

# Replace the token with the rendered _csrf meta value from login.html.
curl.exe -i -b cookies.txt -c cookies.txt -H "X-CSRF-TOKEN: replace-with-login-token" --data-urlencode "email=replace-with-your-email" --data-urlencode "password=replace-with-your-password" "$foodshareBase/login"

# Fetch the authenticated page and read its fresh _csrf / _csrf_header values.
curl.exe -sS -b cookies.txt -c cookies.txt -o home.html "$foodshareBase/home"

# Protected read; use a real accessible post ID.
curl.exe -i -b cookies.txt "$foodshareBase/api/v1/food-posts/123/comments?page=0"
```

For JSON writes, use a UTF-8 file to avoid PowerShell/native-command quoting differences:

```powershell
$foodshareJson = '{"body":"ความคิดเห็นสำหรับทดสอบ API"}'
[System.IO.File]::WriteAllText((Join-Path $PWD "comment-request.json"), $foodshareJson, [System.Text.UTF8Encoding]::new($false))

curl.exe -i -b cookies.txt -H "X-CSRF-TOKEN: replace-with-fresh-token" -H "Content-Type: application/json" --data-binary "@comment-request.json" "$foodshareBase/api/v1/food-posts/123/comments"
```

Use the returned comment ID for later read, update and delete requests. This command creates a real comment; use a local test post and remove the test comment afterward.

## Routes

Paths below are relative to `/api/v1`. The Input column links to the request fields or query format. Response types are defined in [Responses](#responses).

### Food posts

| Method | Path | Access | Input | Success |
|---|---|---|---|---|
| GET | `/food-posts` | Public; `ownership=mine` needs login | [Post search](#post-search-query) | 200, `PageView<PostView>` |
| GET | `/food-posts/map` | Public; `ownership=mine` needs login | [Map search](#map-search-query) | 200, `PageView<PostView>` |
| GET | `/food-posts/{id}` | Public, with cancelled-post restriction | — | 200, PostView |
| POST | `/food-posts` | Signed in | [FoodPostRequest](#foodpostrequest) | 201 + Location, PostView |
| PUT | `/food-posts/{id}` | Post owner | [FoodPostRequest](#foodpostrequest) | 200, PostView |
| POST | `/food-posts/{id}/extend` | Post owner | [ExtendPostRequest](#extendpostrequest) | 200, PostView |
| DELETE | `/food-posts/{id}` | Post owner | — | 204 |
| POST | `/food-posts/{id}/images` | Post owner | [Image upload](#image-upload) | 200, PostView |
| DELETE | `/food-posts/{id}/images/{imageId}` | Post owner | — | 204 |
| GET | `/me/posts` | Current account | [Page query](#page-query) | 200, `PageView<PostView>` |
| GET | `/members/{ownerId}/posts` | Signed in | [Page query](#page-query) | 200, `PageView<PostView>` |
| GET | `/me/posts/management-summary` | Current account | — | 200, ManagementSummary |
| GET | `/stats` | Public | — | 200, Stats |
| GET | `/food-posts/{id}/owner-photo` | Signed in | — | 200 image, or 302 default image |

Deleting a food post closes it as `CANCELLED` and cancels pending bookings through the post-closed event. It does not erase the post's history.

### Owner stock

| Method | Path | Access | Input | Success |
|---|---|---|---|---|
| GET | `/food-posts/{id}/stock` | Post owner | — | 200, StockSnapshot |
| POST | `/food-posts/{id}/stock` | Post owner | [Stock Change](#stock-change) | 200, StockSnapshot |

Read the current stock `version` before changing stock. A stale `expectedVersion` produces a conflict.

### Comments

| Method | Path | Access | Input | Success |
|---|---|---|---|---|
| POST | `/food-posts/{postId}/comments` | Signed in | [CreateCommentRequest](#createcommentrequest) | 201 + Location, CommentView |
| GET | `/food-posts/{postId}/comments` | Signed in | [Page query](#page-query) | 200, `PageView<CommentView>` |
| GET | `/comments/{id}` | Signed in | — | 200, CommentView |
| PUT | `/comments/{id}` | Comment author | [UpdateCommentRequest](#updatecommentrequest) | 200, CommentView |
| DELETE | `/comments/{id}` | Comment moderator | — | 204 |

Updating changes only the text. It preserves the author, food post, creation time and reply links. Deletion is a soft delete; remaining replies keep their thread relationship. Reading or updating a missing/deleted comment returns 404.

### Reservations and pickup

| Method | Path | Access | Input | Success |
|---|---|---|---|---|
| POST | `/food-posts/{id}/reservations` | Signed in, except own food post | [Quantity + Idempotency-Key](#reservation-quantity) | 201 + Location, ReservationView |
| GET | `/food-posts/{id}/reservations` | Post owner | — | 200, ReservationView[] |
| GET | `/food-posts/{id}/my-reservation` | Current account | — | 200, ReservationView; 204 if none |
| GET | `/reservations/{id}` | Reservation participant | — | 200, ReservationView |
| PUT | `/reservations/{id}` | Reservation member | [Quantity](#reservation-quantity) | 200, ReservationView |
| DELETE | `/reservations/{id}` | Reservation participant | — | 204 |
| POST | `/reservations/{id}/collection` | Post owner | [Pickup Code](#pickup-code) | 200, ReservationView |
| GET | `/me/reservations` | Current account | [Page query](#page-query) | 200, `PageView<ReservationView>` |
| GET | `/reservations/{id}/member-photo` | Reservation participant | — | 200 image, or 302 default image |
| POST | `/pickup/scan` | Signed in | [QR frame upload](#qr-frame-upload) | 200, ScanResult |

Reservation deletion cancels the reservation and retains history. Either the member or food-post owner may cancel it. Quantity editing is member-only.

The idempotency header applies to reservation creation. Retrying the same key, member, post and quantity returns the existing reservation (the controller still sends 201). Reusing the key with a different post or quantity returns 409.

Pickup confirmation requires the food-post owner, an eligible reservation and pickup time, plus the member's six-digit code. The owner does not receive the code through ReservationView. A successful QR decode only reads a QR value; it does not collect a reservation automatically.

### Saved posts

| Method | Path | Access | Input | Success |
|---|---|---|---|---|
| PUT | `/food-posts/{id}/saved` | Current account | — | 204 |
| DELETE | `/food-posts/{id}/saved` | Current account | — | 204 |
| GET | `/me/saved-posts` | Current account | [Page query](#page-query) | 200, `PageView<PostView>` |

### Members and onboarding

| Method | Path | Access | Input | Success |
|---|---|---|---|---|
| GET | `/members/{id}` | Signed in | — | 200, MemberProfile |
| GET | `/members/{id}/photo` | Signed in | — | 200 image, or 302 default image |
| PATCH | `/me/profile` | Current account | [Profile Name](#profile-name) | 204 |
| POST | `/me/onboarding` | Current account | — | 204 |

MemberProfile contains only `id` and `name`; missing/inactive members return 404. The profile mutation renames the current account. Completing onboarding records completion for the current account.

### Notifications

| Method | Path | Access | Input | Success |
|---|---|---|---|---|
| GET | `/me/notifications` | Current account | [Page query](#page-query) | 200, `PageView<NotificationView>` |
| POST | `/me/notifications/{id}/read` | Current account, own notification | — | 204 |
| GET | `/me/notifications/unread` | Current account | — | 200, Count |
| GET | `/me/notification-preferences` | Current account | — | 200, Preferences |
| PUT | `/me/notification-preferences` | Current account | [PreferenceInput](#preferenceinput) | 200, Preferences |

### Reports and administration

| Method | Path | Access | Input | Success |
|---|---|---|---|---|
| POST | `/reports` | Signed in | [ReportInput](#reportinput) | 201, ReportView |
| GET | `/admin/pending-reports` | Admin | — | 200, Count |
| GET | `/admin/posts` | Admin | [Admin post search](#admin-post-search-query) | 200, `PageView<AdminPostView>` |
| GET | `/admin/reports` | Admin | [Page query](#page-query) | 200, `PageView<ReportView>` |
| PATCH | `/admin/reports/{id}` | Admin | [Resolve](#resolve) | 200, ReportView |
| GET | `/admin/users` | Admin | [Page query](#page-query) | 200, `PageView<AdminUserView>` |
| PATCH | `/admin/users/{id}` | Admin | [Active](#active) | 204 |

A resolved report cannot be resolved again (409). Setting `closePost=true` closes the reported post and triggers cancellation of pending reservations. The account-status operation rejects targeting the acting admin's own account (409).

### Legacy route

This path is outside the `/api/v1` base:

| Method | Full path | Access | Input | Success |
|---|---|---|---|---|
| GET | `/api/food-posts/map` | Signed in | — | 200, MapFoodPostResponse[] |

Prefer the v1 map route for new clients. The legacy route returns an array rather than PageView.

### Account web forms

These are application-root HTML/form routes, not JSON REST resources:

| Method | Path | Access | Format / result |
|---|---|---|---|
| GET / POST | `/login` | Public; POST needs CSRF | HTML / form `email,password`; success redirect to `/home` |
| POST | `/logout` | Session; needs CSRF | Success redirect to `/` |
| GET / POST | `/register` | Public; POST needs CSRF | HTML / RegisterRequest form; success redirect to `/login` |
| GET / POST | `/forgot-password` | Public; POST needs CSRF | HTML / email form; success redirect to `/forgot-password?sent` |
| GET / POST | `/reset-password` | Public; POST needs CSRF | HTML / token,password,confirmPassword; success redirect to `/login?resetSuccess` |

Validation errors in these form controllers redisplay HTML. They do not use the REST error envelope. Registration fields are `email` (valid email, max 254), `password` (8–72 characters, with service checks) and `displayName` (nonblank, max 80). Reset password requires matching confirmation, a valid unused token, and a password of 8–72 characters and at most 72 UTF-8 bytes.

## Input contracts

All JSON field names are exact source names. “Required” follows DTO validation; business constraints are listed separately.

### FoodPostRequest

Used for both create and update; PUT expects the full request.

| Field | Type | Required | Validation |
|---|---|---|---|
| title | string | Yes | Nonblank, max 150 characters |
| description | string | Yes | Nonblank, max 1000 |
| category | enum string | Yes | `FOOD`, `DRINK`, `SNACK` |
| quantity | integer | Yes | 1–10000 |
| unit | string | Yes | Nonblank, max 50 |
| pickupLocationName | string | Yes | Nonblank, max 255 |
| latitude | decimal | Yes | -90 through 90 |
| longitude | decimal | Yes | -180 through 180 |
| availableFrom | ISO local date-time | Yes | Pickup start |
| availableUntil | ISO local date-time | Yes | Later than start and current application time |
| allergens | string | No | Max 500 |
| maxPerPerson | integer / null | No | 1–10000; no greater than quantity; null means no configured cap |

Further update rules: the post must remain editable; total quantity cannot fall below reserved + collected + offline quantities. Existing reservations restrict changes to pickup time/location and lowering the per-person limit. Business-state conflicts return 409.

Illustrative request; set pickup dates in the future when testing:

```json
{
  "title": "ข้าวกล่องแบ่งปัน",
  "description": "อาหารทำใหม่ รับตามเวลาที่ระบุ",
  "category": "FOOD",
  "quantity": 5,
  "unit": "กล่อง",
  "pickupLocationName": "มหาวิทยาลัยขอนแก่น",
  "latitude": 16.47,
  "longitude": 102.82,
  "availableFrom": "2026-10-10T12:00:00",
  "availableUntil": "2026-10-10T14:00:00",
  "allergens": "ไข่",
  "maxPerPerson": 2
}
```

### ExtendPostRequest

| Field | Type | Required | Validation |
|---|---|---|---|
| availableUntil | ISO local date-time | Yes | New end must be later than now |

The food-post owner may extend an expired, non-cancelled post with remaining stock. Old reservations expire first; extension does not revive them.

### CreateCommentRequest

| Field | Type | Required | Validation |
|---|---|---|---|
| body | string | Yes | Nonblank, max 800 |
| parentCommentId | integer / null | No | Positive if supplied; must identify an eligible comment on the same post |

Root comment:

```json
{"body":"ยังมีอาหารไหม"}
```

Reply; replace 456 with an actual comment ID:

```json
{"body":"ขอรับ 1 กล่องค่ะ","parentCommentId":456}
```

### UpdateCommentRequest

| Field | Type | Required | Validation |
|---|---|---|---|
| body | string | Yes | Nonblank, max 800; stored text is trimmed |

```json
{"body":"แก้ไขเป็นขอรับ 2 กล่องค่ะ"}
```

### Reservation Quantity

| Field | Type | Required | Validation |
|---|---|---|---|
| quantity | integer | Yes | 1–10000; stock and per-person limit also apply |

```json
{"quantity":2}
```

Creation also requires HTTP header `Idempotency-Key`: 16–64 characters matching `[a-zA-Z0-9-]{16,64}`, for example `550e8400-e29b-41d4-a716-446655440000`. This request key is a string; it does not change resource IDs into UUIDs. An active duplicate booking of the same post must be edited through its existing reservation.

### Pickup Code

| Field | Type | Required | Validation |
|---|---|---|---|
| code | string | Yes | Nonblank; service checks a six-digit code against the reservation |

```json
{"code":"123456"}
```

This is an example, not a working pickup code. Incorrect codes are tracked; repeated failures can return 429 and temporarily lock attempts.

### Stock Change

| Field | Type | Required | Validation |
|---|---|---|---|
| action | enum string | Yes | `ADD`, `REMOVE`, `OFFLINE`, `UNDO_OFFLINE` |
| amount | integer | Yes | 1–10000; stock rules apply |
| expectedVersion | integer | Yes | Nonnegative current StockSnapshot.version |

```json
{"action":"OFFLINE","amount":1,"expectedVersion":0}
```

Use the actual version from GET stock. ADD increases total quantity; REMOVE reduces unallocated total; OFFLINE records items given outside the web; UNDO_OFFLINE reverses that recorded amount. These operations preserve quantities already reserved or collected.

### Profile Name

| Field | Type | Required | Validation |
|---|---|---|---|
| name | string | Yes | Nonblank, max 80 |

### PreferenceInput

| Field | Type | Required | Validation / behavior |
|---|---|---|---|
| categories | string array / null | No | Recognized values: FOOD, DRINK, SNACK; unknown values are filtered and duplicates removed |
| keywords | string / null | No | Max 300; whitespace normalized; null becomes empty text |

```json
{"categories":["FOOD","SNACK"],"keywords":"ข้าว ขนม"}
```

### ReportInput

| Field | Type | Required | Validation |
|---|---|---|---|
| postId | integer | Yes | At least 1; referenced food post must exist |
| commentId | integer / null | No | Referenced comment must belong to that post |
| reason | string | Yes | Nonblank, max 1000 |

### Resolve

| Field | Type | Required | Validation / behavior |
|---|---|---|---|
| reason | string | Yes | Nonblank, max 1000 |
| closePost | boolean | No | True closes the post; omitted primitive boolean defaults to false |

### Active

| Field | Type | Required | Validation / behavior |
|---|---|---|---|
| reason | string | Yes | Nonblank, max 1000 |
| active | boolean | No | True enables, false disables; omitted primitive boolean defaults to false |

Always send `active` explicitly when changing account status.

### Image upload

Multipart field `file` contains a JPG/JPEG or PNG file. The storage validator checks image content, max 5 MiB and max 20 million pixels. Each food post has a maximum of five gallery images. The configured multipart request limit is 6 MB.

Invalid image content can return 400; rejection by the multipart upload-size limit returns 413. Successful upload returns the updated PostView, with its image list.

### QR frame upload

Multipart field `frame` contains the captured QR image. Success returns `{"value":"decoded-content"}`. An unreadable QR returns 422. QR scanning and reservation collection are separate operations.

## Query contracts

### Post search query

| Parameter | Default | Meaning / limit |
|---|---|---|
| q | Empty | Title/location search, max 100 characters |
| category | Empty | No filter, or FOOD / DRINK / SNACK |
| sort | expiry | expiry / latest / nearby |
| lat | Omitted | Latitude -90 through 90 |
| lng | Omitted | Longitude -180 through 180 |
| now | false | Restrict to posts whose pickup period has started |
| page | 0 | Zero-based, 0–10000 |
| size | 12 | 1–200 |
| ownership | Empty | Empty / mine / others |

Coordinates must be finite and supplied together; nearby sorting needs both. `ownership=mine` requires login. With an authenticated account, `others` excludes that account's posts; anonymous `others` has no account to exclude.

Example:

```text
GET /api/v1/food-posts?category=FOOD&sort=expiry&now=true&page=0&size=12
```

### Map search query

Accepts `q`, `category`, `now` and `ownership` with the above defaults. The controller fixes sort to expiry, page to 0 and size to 200. This is a maximum of 200 results, not an unbounded dataset.

### Page query

Accepts `page`, default 0. These routes have a fixed page size, not a client `size` or `sort` parameter:

| Routes | Fixed size | Ordering |
|---|---|---|
| Food-post comment list | 30 | createdAt, id ascending |
| /me/posts, /members/{ownerId}/posts | 12 | createdAt descending |
| /me/reservations | 12 | createdAt descending |
| /me/saved-posts | 12 | Saved-record createdAt descending |
| /me/notifications | 20 | createdAt descending |
| /admin/reports | 20 | createdAt descending |
| /admin/users | 20 | id ascending |

Except for `/me/saved-posts`, the listed service methods normalize negative pages to 0. `/me/saved-posts` and search `/food-posts` validate a 0–10000 page range and return 400 for an invalid page.

### Admin post search query

| Parameter | Default | Meaning / limit |
|---|---|---|
| page | 0 | Zero-based; fixed size 20; negatives normalized to 0 |
| q | Empty | Search title, owner display name or pickup location; max 120 |
| status | Empty | No filter, or AVAILABLE / LOW_STOCK / CLAIMED / EXPIRED / CANCELLED |

Ordering is createdAt descending then id descending. The status filter uses stored FoodPostStatus, rather than every derived display status in PostView.

## Responses

### PageView

```json
{
  "items": [],
  "page": 0,
  "totalPages": 0,
  "totalElements": 0
}
```

This example represents an empty page. Actual totals depend on data. There is no top-level `data`, `success` or `content` wrapper.

### PostView

| Field(s) | JSON type / meaning |
|---|---|
| id, ownerId | Numeric IDs |
| title, description, category, unit, pickupLocationName | Strings |
| quantity, reservedQuantity, collectedQuantity, availableQuantity, offlineQuantity | Integer stock quantities |
| latitude, longitude | Decimal coordinates |
| availableFrom, availableUntil, createdAt | ISO local date-time |
| status | Display status computed by the mapper |
| ownerName, allergens | Strings |
| imageUrl | Main image URL |
| images | Array of `{id,url,sortOrder}` |
| commentCount | Integer |
| mine, saved | Booleans relative to the reader |
| distanceKm | Number or null; depends on supplied coordinates |
| maxPerPerson | Integer or null |

Available stock is total quantity minus reserved, collected and offline quantities. Display status can differ from the stored enum because the mapper considers time and remaining stock.

### CommentView

| Field | Type / meaning |
|---|---|
| id, postId, authorId | Numeric IDs |
| authorName, body | Strings |
| createdAt | ISO local date-time |
| canDelete | Boolean permission for the current reader |
| parentCommentId | Numeric root-thread ID, or null |
| replyToCommentId | Numeric direct reply target, or null |
| replyToAuthorName | Target author's name, or null |
| parentDeleted | Whether the root comment was deleted |
| canEdit | True only for the comment author |

### ReservationView

| Field | Type / meaning |
|---|---|
| id | Numeric reservation ID |
| post | Nested PostView |
| quantity | Integer |
| status | RESERVED / COLLECTED / CANCELLED / EXPIRED |
| memberName, memberId | Member display name and numeric ID |
| pickupCode | String only for the reservation member while eligible; otherwise null |
| owner | Whether the reader owns the food post |
| createdAt | ISO local date-time |

### Other response types

| Type | Fields |
|---|---|
| MemberProfile | `id,name` |
| MapFoodPostResponse (legacy) | `id,title,description,category,quantity,unit,pickupLocationName,latitude,longitude,availableFrom,availableUntil,status,categoryIcon` |
| StockSnapshot | `postId,quantity,reservedQuantity,collectedQuantity,offlineQuantity,availableQuantity,version,unit` |
| ManagementSummary | `totalPosts,openPosts,waitingCount,collectedCount,offlineCount` |
| Stats | `shared,posts` |
| Count | `count` |
| Preferences | `categories,keywords` |
| ScanResult | `value` |
| NotificationView | `id,title,message,href,type,actorId,actorName,createdAt,read` |
| ReportView | `id,postId,commentId,title,reporter,reason,status,resolution,createdAt` |
| AdminPostView | `id,title,owner,status,availableQuantity,reservedQuantity,unit,pickupLocationName,availableUntil` |
| AdminUserView | `id,name,email,active,role` |

Image endpoints return image bytes, not JSON. If no image is available they redirect to `/images/default-profile.png` with 302.

## Errors

The API controller advice returns:

```json
{
  "status": 400,
  "message": "กรุณาตรวจสอบข้อมูลที่กรอก",
  "fields": {
    "body": "กรุณาเขียนความคิดเห็น"
  }
}
```

For a non-field error, `fields` is an empty object. Security-filter responses may omit `fields`; a CSRF rejection is not guaranteed to use this advice's JSON format.

| Status | Meaning |
|---|---|
| 400 | Invalid JSON, field validation, numeric/enum values or business input |
| 401 | API authentication required |
| 403 | Permission denied, inactive account or invalid/missing CSRF |
| 404 | Resource missing, deleted comment or a post hidden by access rules |
| 409 | Insufficient stock, state conflict, idempotency mismatch or concurrent/version conflict |
| 413 | Multipart upload exceeds configured size |
| 422 | QR cannot be decoded |
| 429 | Too many attempts / throttling, including pickup-code lockout |
| 500 | Unexpected API failure; details logged server-side |

Status codes in route tables describe successful requests, not every error that framework processing can produce. This is a source-based contract, not a claim that every invalid-input permutation has been exercised.

## Swagger / OpenAPI

- Local Swagger: `http://localhost:8080/swagger-ui.html` (or `/swagger-ui/index.html`; adjust the port).
- Local OpenAPI JSON: `http://localhost:8080/v3/api-docs`.
- Public Swagger: [https://kku-foodshare.onrender.com/swagger-ui.html](https://kku-foodshare.onrender.com/swagger-ui.html).
- Public OpenAPI JSON: [https://kku-foodshare.onrender.com/v3/api-docs](https://kku-foodshare.onrender.com/v3/api-docs).

The source permits access to Swagger and OpenAPI without login. Protected operations still require the application's authenticated session and CSRF token. Log in on the same origin first. There is no bearer JWT to paste into Authorize.

Configuration enables Swagger CSRF support, but successful protected writes through the deployed Swagger UI must be checked separately; opening the UI alone does not verify those requests.

## Verification

### Evidence currently available

| Check | Result / evidence | Scope and limit |
|---|---|---|
| Phase 12 Docker script on the project owner's Windows machine | Owner supplied terminal output ending `All Docker test commands passed` | Script checks java-tests, js-tests and postgres-tests sequentially; this report was not rerun in this review environment |
| PostgreSQL-backed test subset | Owner supplied `Tests run: 48, Failures: 0, Errors: 0, Skipped: 0` and BUILD SUCCESS | The 48 belong to CommentCrudJourneyTest, FoodJourneyTest, ReviewRegressionTest and WebPagesTest, not the total number of all Java tests |
| JavaScript unit suite rerun during this reference review | 132 tests, 132 passed, 0 failed, 0 skipped | Executed locally with Node; does not exercise Java endpoints or the deployed server |
| Comment HTTP contract test source | Eight tests in CommentCrudJourneyTest; assertions on JSON, status, Location, persistence, permissions, validation, CSRF and OpenAPI | Spring Boot + MockMvc with test users and CSRF helpers; this is not a browser login or a live Render request |
| Endpoint inventory in this reference | Compared against API controller mappings, DTOs, service rules and SecurityConfig | Static verification; does not identify the deployed commit |
| Every deployed endpoint's actual response | Not captured or independently exercised in this review | Still requires a runtime smoke check on the submitted deployment |

**Yes: API result assertions already exist and the supplied Docker run passed. No claim is made that every deployed endpoint has been runtime-tested.**

The available source includes an earlier local Maven-attempt report; that file is not a substitute for the successful Windows Docker reports. For submission, use the logs and Surefire reports produced by the successful run.

### Comment CRUD assertions already in the test suite

| Scenario | Expected result checked in tests |
|---|---|
| Create a comment | 201, Location header, canEdit=true |
| Read the created comment | 200, matching body |
| Edit own comment | 200, trimmed updated body and database persistence |
| List comments | 200, updated body in PageView |
| Delete a comment | 204, empty response |
| Read after deletion | 404 |
| Another member, post owner or admin edits someone else's comment | 403; original text retained |
| Blank, overlong or malformed update | 400; original text retained |
| Edit a reply | Original author, thread links and creation time preserved |
| Missing/deleted comment read or update | 404 |
| Edit reply after root deletion | Reply retained; parentDeleted=true |
| Protected request without login | 401 in tested cases |
| Signed-in update without CSRF | 403 |
| OpenAPI comment read/update paths | Present in /v3/api-docs |

### Re-run before handing in

From the repository root in Windows PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test-phase12.ps1
```

Keep the fresh output from:

- `test/reports/phase12/java-tests-docker.log`
- `test/reports/phase12/js-tests-docker.log`
- `test/reports/phase12/postgres-tests-docker.log`
- `code/target/surefire-reports/`

Use reports from the exact commit to be submitted. The PostgreSQL test step may overwrite reports for the classes it reruns, so preserve the separate service logs as well.

Then check the running application with real test accounts:

1. Public GET food-post search: confirm 200, JSON PageView, pagination and sorting.
2. Login using a test account; perform comment create → read → update → list → delete → read-after-delete. Record status, Location and returned JSON.
3. Use a second account to verify author-only editing and an admin account to verify the admin routes.
4. Check a missing resource, invalid body and write without CSRF.
5. Open the public deployment, Swagger and OpenAPI JSON. Confirm the deployed commit/configuration and repeat a small smoke check using dedicated test data.

Store response evidence in `test/reports/` after removing session cookies, CSRF tokens, passwords and pickup codes. If the hosting service has gone idle, allow it to finish starting before judging the response.

## Source locations

Paths are relative to the repository root:

| Purpose | Location |
|---|---|
| API routes, input records and success responses | `code/src/main/java/com/kku/foodshare/controller/api/` |
| Request DTOs | `code/src/main/java/com/kku/foodshare/dto/request/` |
| Response DTOs | `code/src/main/java/com/kku/foodshare/dto/response/` |
| Ownership, stock, reservation, moderation and comment rules | `code/src/main/java/com/kku/foodshare/service/impl/` |
| Session login and public/protected routes | `code/src/main/java/com/kku/foodshare/config/SecurityConfig.java` |
| Application time zone | `code/src/main/java/com/kku/foodshare/config/TimeConfig.java` |
| CSRF HTML meta values | `code/src/main/resources/templates/fragments.html` |
| REST error envelope | `code/src/main/java/com/kku/foodshare/exception/ApiExceptionHandler.java` |
| Comment CRUD response assertions | `code/src/test/java/com/kku/foodshare/CommentCrudJourneyTest.java` |
| Test runner / isolated test services | `scripts/test-phase12.ps1`, `compose.test.yaml` |

