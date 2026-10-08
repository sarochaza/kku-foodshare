# Architecture and design

## Layered architecture

Controller รับ HTTP/validation และเรียก service interface เท่านั้น ไม่เรียก Repository โดยตรง Service จัดการ authorization, transaction, stock และ state ส่วน Repository ติดต่อ SQL ผ่าน JPA DTO แยกจาก Entity; ใช้ constructor injection

```mermaid
flowchart TD
  UI[Thymeleaf และ JavaScript] --> API[Web / REST Controllers]
  API --> S[Service interfaces]
  S --> I[Service implementations]
  I --> R[JPA Repositories]
  R --> DB[(PostgreSQL)]
  I --> F[ImageStorage]
  F --> V[(Persistent volume)]
```

## Use cases

```mermaid
flowchart TD
  Guest[ผู้เยี่ยมชม] --> Browse[ค้นหาอาหารและดูจุดรับ]
  Guest --> SignUp[สมัครสมาชิก]
  Member[สมาชิก] --> Share[สร้างและจัดการโพสต์]
  Member --> Book[จอง / แก้จำนวน / ยกเลิก]
  Member --> Pickup[แสดงรหัสและยืนยันรับ]
  Member --> Report[รายงานปัญหา]
  Admin[ผู้ดูแล] --> Review[ตรวจรายงานและปิดโพสต์]
  Admin --> Suspend[ระงับหรือเปิดบัญชี]
```

## Domain / ER model

```mermaid
erDiagram
  USERS ||--o| USER_PROFILE_IMAGES : avatar
  USERS ||--o{ FOOD_POSTS : owns
  USERS ||--o{ RESERVATIONS : books
  USERS ||--o{ PASSWORD_RESET_TOKENS : resets
  USERS ||--o{ NOTIFICATIONS : receives
  USERS ||--o{ REPORTS : reports
  USERS ||--o{ AUDIT_EVENTS : acts
  FOOD_POSTS ||--o| FOOD_POST_IMAGES : picture
  FOOD_POSTS ||--o{ RESERVATIONS : contains
  FOOD_POSTS ||--o{ REPORTS : concerns
```

| Table | หน้าที่ / หลักการ |
|---|---|
| users | สมาชิก, email แบบ unique ไม่แยกตัวพิมพ์, BCrypt, active/role, optimistic version |
| user_profile_images | PK/FK user_id ความสัมพันธ์ one-to-one, bytea และ content type |
| password_reset_tokens | SHA-256 token hash, อายุ/เวลาที่ใช้แล้ว, version, FK user |
| food_posts | เจ้าของ รายละเอียด พิกัด ช่วงเวลา total/reserved/collected และ version |
| food_post_images | หนึ่งรูปต่อโพสต์, ชื่อ UUID ที่ระบบสร้าง |
| reservations | จำนวน/สถานะ/idempotency key/รหัสรับเข้ารหัสและ hash/lock/version |
| notifications | ข้อความภายในระบบและเวลาอ่าน |
| reports | ผู้รายงาน โพสต์ เหตุผล ผลตรวจ ผู้ดูแลและ version |
| audit_events | ผู้กระทำ action เป้าหมาย เหตุผล และเวลา |

Migration V1 มี FK, check constraints, index และ partial unique index บังคับหนึ่ง active reservation ต่อคนต่อโพสต์ V2 เพิ่ม version ของ users. `available = quantity - reserved - collected` ต้องไม่ติดลบ ช่วงรับต้องสิ้นสุดหลังเริ่ม และพิกัดต้องอยู่ในช่วงละติจูด/ลองจิจูดจริง

## Class / pattern relationships

```mermaid
classDiagram
  FoodCatalogService <|.. FoodCatalogServiceImpl
  FoodCatalogServiceImpl --> FoodDiscoveryStrategy
  FoodDiscoveryStrategy <|.. ExpiryFoodStrategy
  FoodDiscoveryStrategy <|.. LatestFoodStrategy
  FoodDiscoveryStrategy <|.. NearbyFoodStrategy
  FoodCatalogServiceImpl --> ImageStorage
  ImageStorage <|.. LocalImageStorage
  ReservationService <|.. ReservationServiceImpl
  ReservationServiceImpl --> ReservationState
  ReservationState <|.. ReservationStatus
  ReservationServiceImpl --> ActivityNotice
  ActivityNotice --> NotificationServiceImpl
```

| หลักการ | จุดใช้จริง |
|---|---|
| SRP | LocalImageStorage ตรวจ/แปลง/เก็บรูป; PickupCodeService สร้างและป้องกันรหัส; NotificationServiceImpl เก็บข้อความ |
| OCP | FoodDiscoveryStrategy เพิ่มลำดับค้นหาใหม่โดยเพิ่ม implementation และเลือกผ่าน strategy registry |
| LSP | Expiry/Latest/Nearby มี contract สร้าง Order ให้ Criteria query เดียวกัน |
| ISP | FoodCatalogService, ReservationService, ModerationService และ ImageStorage แยกตามหน้าที่ของผู้เรียก |
| DIP | Controller พึ่ง service interface; catalog พึ่ง ImageStorage; implementation รับ dependency ผ่าน constructor |
| Strategy (behavioral) | FoodDiscoveryStrategy เลือกลำดับ expiry/latest/nearby |
| State (behavioral) | ReservationStatus implements ReservationState; requireMutable อนุญาตเฉพาะ RESERVED |
| Observer (behavioral) | ActivityNotice และ PostClosed ใช้ Spring application events; listeners สร้าง notification และยกเลิกการจอง |

ไม่มีการนับ annotation/framework repository เป็น GoF pattern เพิ่มเติม ทั้ง 3 patterns เป็น behavioral ตามข้อกำหนด “อย่างน้อยหนึ่งกลุ่ม” ของใบงาน

## Sequence 1: สร้างโพสต์

```mermaid
sequenceDiagram
  actor Owner as ผู้แบ่งปัน
  participant API as CatalogController
  participant S as CatalogService
  participant DB as Database
  Owner->>API: POST พร้อม CSRF และพิกัด
  API->>S: create(email, DTO)
  S->>DB: ตรวจสมาชิก + INSERT post
  DB-->>S: id และข้อมูล
  S-->>Owner: 201 PostView
  Owner->>API: POST multipart image
  API->>S: image(owner, id, file)
  S->>S: ตรวจ pixels และ re-encode
  S->>DB: เก็บชื่อไฟล์รูป
  S-->>Owner: PostView พร้อม imageUrl
```

## Sequence 2: จองพร้อมกัน

```mermaid
sequenceDiagram
  actor Receiver as ผู้รับ
  participant API as ReservationController
  participant S as ReservationService
  participant DB as Database
  Receiver->>API: POST quantity + idempotency key + CSRF
  API->>S: reserve(email, post, quantity, key)
  S->>DB: SELECT post FOR UPDATE
  S->>DB: ตรวจ retry / active reservation
  S->>S: ตรวจเจ้าของ เวลา และจำนวน
  alt อาหารพอ
    S->>DB: เพิ่ม reserved + INSERT reservation
    S->>DB: INSERT notifications ใน transaction เดียว
    S-->>Receiver: 201 + รหัสรับเฉพาะผู้รับ
  else ไม่พอหรือสถานะเปลี่ยน
    S-->>Receiver: 409 + ข้อความ
  end
```

## Sequence 3: ส่งมอบอาหาร

```mermaid
sequenceDiagram
  actor Owner as ผู้แบ่งปัน
  participant API as ReservationController
  participant S as ReservationService
  participant DB as Database
  Owner->>API: POST code + CSRF
  API->>S: collect(owner, reservation, code)
  S->>DB: หา post id แล้ว lock post ก่อนอ่าน reservation
  S->>S: ตรวจเจ้าของ สถานะ เวลา และ hash
  alt รหัสถูก
    S->>DB: reserved ลด / collected เพิ่ม / COLLECTED
    S->>DB: บันทึกแจ้งเตือนผู้รับ
    S-->>Owner: ยืนยันรับสำเร็จ
  else รหัสผิด
    S->>DB: เก็บ failedAttempts และเวลาล็อก
    S-->>Owner: 400 หรือ 429
  end
```

## Activity และ state

```mermaid
flowchart TD
  A[เลือกอาหาร] --> B{เข้าสู่ระบบแล้ว?}
  B -- ยัง --> L[เข้าสู่ระบบ]
  L --> C[ระบุจำนวนและยืนยัน]
  B -- แล้ว --> C
  C --> D{จำนวนและเวลายังใช้ได้?}
  D -- ไม่ --> E[แสดงสาเหตุและโหลดข้อมูลใหม่]
  D -- ใช่ --> F[บันทึกจองและแสดงรหัส]
  F --> G{ผู้รับมารับทันเวลา?}
  G -- ใช่ --> H[เจ้าของตรวจรหัสและส่งมอบ]
  G -- ไม่ --> I[หมดอายุและคืนจำนวน]
```

```mermaid
stateDiagram-v2
  [*] --> RESERVED: จองสำเร็จ
  RESERVED --> COLLECTED: เจ้าของตรวจรหัสถูก
  RESERVED --> CANCELLED: ผู้มีสิทธิ์ยกเลิก / ปิดโพสต์
  RESERVED --> EXPIRED: หมดเวลารับ
  COLLECTED --> [*]
  CANCELLED --> [*]
  EXPIRED --> [*]
```

## Component / deployment

```mermaid
flowchart TD
  Browser[โทรศัพท์ / โน้ตบุ๊ก] -->|HTTPS| Caddy[Caddy reverse proxy]
  Caddy --> App[Spring Boot 1 instance]
  App --> PG[(PostgreSQL 17)]
  App --> Images[(Persistent image volume)]
  App -. optional .-> SMTP[SMTP provider]
  Browser -. optional login .-> Google[Google OAuth]
  Browser --> Tiles[Map tile provider]
```

Security ใช้ session + CSRF, active-account filter, ownership ใน service, BCrypt, AES-GCM สำหรับรหัสรับที่จำเป็นต้องแสดงซ้ำ, pessimistic post locks และ optimistic versions การเรียก mutating service อยู่ใน transaction; notifications ภายในฐานข้อมูล rollback พร้อมธุรกรรมหลัก

## Source references

เลขบรรทัดอ้างอิง source ในแพ็กเกจนี้

| เรื่อง | ไฟล์ | บรรทัด |
|---|---|---:|
| SRP / รูป | `code/src/main/java/com/kku/foodshare/service/storage/LocalImageStorage.java` | 17 |
| DIP / constructor | `code/src/main/java/com/kku/foodshare/service/impl/FoodCatalogServiceImpl.java` | 35 |
| OCP / Strategy interface | `code/src/main/java/com/kku/foodshare/service/discovery/FoodDiscoveryStrategy.java` | 6 |
| LSP / Nearby strategy | `code/src/main/java/com/kku/foodshare/service/discovery/NearbyFoodStrategy.java` | 9 |
| ISP / Reservation contract | `code/src/main/java/com/kku/foodshare/service/ReservationService.java` | 6 |
| State | `code/src/main/java/com/kku/foodshare/domain/entity/ReservationStatus.java` | 6 |
| Observer listener | `code/src/main/java/com/kku/foodshare/service/impl/NotificationServiceImpl.java` | 28 |
| Atomic stock lock | `code/src/main/java/com/kku/foodshare/service/impl/ReservationServiceImpl.java` | 93 |
| User concurrency | `code/src/main/java/com/kku/foodshare/domain/entity/User.java` | 41 |
