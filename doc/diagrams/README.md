# Diagrams

Diagram sources describe the submitted code. Use Case uses PlantUML; the diagrams below render as Mermaid. Deployment shows the cloud configuration described in the existing phase notes, not proof of a live deployment. Local Docker uses PostgreSQL and local volumes unless cloud providers are configured.

![Use Case Diagram](use-case.svg)

[Use Case source](use-case.puml) · [Use Case descriptions](use-case-descriptions.md)

## Domain

```mermaid
classDiagram
  class User
  class FoodPost
  class FoodPostImage
  class Reservation
  class PostComment
  class Notification
  class NotificationPreference
  class UserProfileImage
  class SavedPost
  class Report
  class PasswordResetToken
  class AuditEvent
  User "1" --> "0..*" FoodPost : shares
  User "1" --> "0..*" Reservation : books
  FoodPost "1" --> "0..*" Reservation : contains
  FoodPost "1" --> "0..*" FoodPostImage : gallery
  FoodPost "1" --> "0..*" PostComment : discussions
  User "1" --> "0..*" PostComment : writes
  User "1" --> "0..*" Notification : receives
  User "1" --> "0..1" NotificationPreference : configures
  User "1" --> "0..1" UserProfileImage : avatar
  User "1" --> "0..*" SavedPost : saves
  FoodPost "1" --> "0..*" SavedPost : saved
  FoodPost "1" --> "0..*" Report : reported
  User "1" --> "0..*" Report : reports
  User "1" --> "0..*" PasswordResetToken : resets
  User "1" --> "0..*" AuditEvent : acts
```

## Class

```mermaid
classDiagram
  direction TB
  class FoodDiscoveryStrategy {
    <<Strategy>>
    +key() String
    +order(cb, post, lat, lng) Order
  }
  FoodDiscoveryStrategy <|.. ExpiryFoodStrategy
  FoodDiscoveryStrategy <|.. LatestFoodStrategy
  FoodDiscoveryStrategy <|.. NearbyFoodStrategy
  FoodCatalogServiceImpl --> FoodDiscoveryStrategy
  class ReservationState {
    <<State>>
    +requireMutable()
  }
  ReservationState <|.. ReservationStatus
  ReservationServiceImpl --> ReservationStatus
  ReservationServiceImpl --> ApplicationEventPublisher
  ApplicationEventPublisher --> ActivityNotice : publishes
  class NotificationServiceImpl {
    <<Observer>>
    +notify(ActivityNotice)
  }
  ActivityNotice --> NotificationServiceImpl : event listener
  NotificationServiceImpl --> PickupReminderService
  PickupReminderService <|.. PickupReminderServiceImpl
  ImageStorage <|.. LocalImageStorage
  ImageStorage <|.. CloudinaryImageStorage
  CloudinaryImageStorage --> ImageStorage : local qualifier
  PostViewService <|.. PostViewServiceImpl
  PostViewServiceImpl --> PostViewMapping
  PostViewMapping <|.. PostViewMapper
  PostViewServiceImpl --> PostViewContext
  PostViewMapper --> PostViewContext
```

## Sequence Create

```mermaid
sequenceDiagram
  actor Owner
  participant API as FoodCatalogController
  participant Service as FoodCatalogService
  participant Repo as JPA Repositories
  Owner->>API: POST /api/v1/food-posts
  API->>API: Validate request and session
  API->>Service: create(email, request)
  Service->>Repo: Require active member
  Service->>Service: Validate coordinates, quantity and time
  Service->>Repo: Save FoodPost in transaction
  Service->>Service: Build PostView through mapper
  Service-->>API: PostView
  API-->>Owner: 201 + Location + PostView
```

## Sequence Reserve

```mermaid
sequenceDiagram
  actor Receiver
  participant API as ReservationController
  participant Service as ReservationService
  participant Repo as JPA Repositories
  Receiver->>API: POST reservation + Idempotency-Key
  API->>Service: reserve(email, postId, quantity, key)
  Service->>Repo: Require member and lock post
  Service->>Repo: Check previous key and active booking
  Service->>Service: Check owner, stock, time and limit
  alt Allowed
    Service->>Repo: Save reservation and update reserved stock
    Service->>Service: Publish ActivityNotice
    Service-->>API: ReservationView with pickup code
    API-->>Receiver: 201
  else Conflict
    Service-->>API: Problem 409
    API-->>Receiver: Standard error JSON
  end
```

## Sequence Collect

```mermaid
sequenceDiagram
  actor Owner
  participant API as ReservationController
  participant Service as ReservationService
  participant Repo as JPA Repositories
  Owner->>API: POST /reservations/id/collection
  API->>Service: collect(email, id, code)
  Service->>Repo: Lock post then load reservation
  Service->>Service: Check owner, state, pickup window and code
  alt Correct code
    Service->>Repo: Reduce reserved, increase collected, mark COLLECTED
    Service->>Service: Publish ActivityNotice
    Service-->>API: ReservationView
    API-->>Owner: 200
  else Invalid code
    Service->>Repo: Retain failed attempts and lock deadline
    Service-->>API: InvalidPickupCode 400 or 429
    API-->>Owner: Standard error JSON
  end
```

## Activity

```mermaid
flowchart TD
  Choose["เลือกอาหาร"] --> Login{"เข้าสู่ระบบแล้ว?"}
  Login -->|ยัง| SignIn["เข้าสู่ระบบ"]
  SignIn --> Quantity["ระบุจำนวน"]
  Login -->|แล้ว| Quantity
  Quantity --> Validate{"จำนวน เวลา และสิทธิ์ผ่าน?"}
  Validate -->|ไม่ผ่าน| Error["แสดงสาเหตุ"]
  Validate -->|ผ่าน| Reserve["บันทึกจองและกัน stock"]
  Reserve --> Pending{"การจองยังรอรับ"}
  Pending -->|ยกเลิก| Cancel["คืน stock และ CANCELLED"]
  Pending -->|หมดเวลา| Expire["คืน stock และ EXPIRED"]
  Pending -->|มารับ| Code{"เจ้าของตรวจรหัส"}
  Code -->|ถูก| Collect["ส่งมอบและ COLLECTED"]
  Code -->|ผิด| Retry["แจ้งรหัสผิดหรือจำกัดการลอง"]
  Retry --> Pending
```

## State

```mermaid
stateDiagram-v2
  [*] --> RESERVED: reserve
  RESERVED --> COLLECTED: correct pickup code
  RESERVED --> CANCELLED: cancel or close post
  RESERVED --> EXPIRED: pickup deadline passes
  COLLECTED --> [*]
  CANCELLED --> [*]
  EXPIRED --> [*]
```

## Component

```mermaid
flowchart TD
  UI["Thymeleaf and JavaScript"] --> HTTP["Web and REST Controllers"]
  HTTP --> Contracts["Service Interfaces"]
  Contracts --> Services["Service Implementations"]
  Services --> Repositories["JPA Repositories"]
  Repositories --> Database["PostgreSQL"]
  Services --> Mapping["DTO and Mapper"]
  Mapping --> HTTP
  Services --> Events["Application Events"]
  Events --> Notice["Notification Listener"]
  Services --> Storage["ImageStorage Interface"]
  Storage --> Providers["Local or Cloudinary"]
```

## Deployment

```mermaid
flowchart TD
  Browser["Browser on desktop or phone"] -->|HTTPS| App["Spring Boot application"]
  App -->|JDBC TLS| DB["PostgreSQL or Neon"]
  App -->|signed HTTPS| Cloud["Cloudinary image storage"]
  App -->|HTTPS API| Mail["Brevo email provider"]
  App -->|OAuth| Google["Google identity provider"]
  Browser -->|tiles and routes| Maps["Map services"]
```

## Er

```mermaid
erDiagram
  USERS ||--o| USER_PROFILE_IMAGES : avatar
  USERS ||--o| NOTIFICATION_PREFERENCES : preferences
  USERS ||--o{ FOOD_POSTS : shares
  USERS ||--o{ RESERVATIONS : books
  FOOD_POSTS ||--o{ RESERVATIONS : contains
  FOOD_POSTS ||--o{ FOOD_POST_IMAGES : gallery
  USERS ||--o{ POST_COMMENTS : authors
  FOOD_POSTS ||--o{ POST_COMMENTS : discusses
  POST_COMMENTS o|--o{ POST_COMMENTS : replies
  USERS ||--o{ NOTIFICATIONS : receives
  USERS o|--o{ NOTIFICATIONS : visible_actor
  USERS ||--o{ SAVED_POSTS : saves
  FOOD_POSTS ||--o{ SAVED_POSTS : saved_by
  USERS ||--o{ PASSWORD_RESET_TOKENS : resets
  FOOD_POSTS ||--o{ REPORTS : reported
  USERS ||--o{ REPORTS : reporter
  USERS o|--o{ REPORTS : reviewer
  POST_COMMENTS o|--o{ REPORTS : comment_report
  USERS ||--o{ AUDIT_EVENTS : actor
```
