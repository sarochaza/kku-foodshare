# KKU FoodShare

เว็บไซต์แบ่งปันอาหารในชุมชนมหาวิทยาลัยขอนแก่น ผู้แบ่งปันโพสต์อาหาร รูป และจุดรับได้
ผู้รับค้นหาและจองอาหาร แสดง QR หรือรหัสรับ และติดตามการแจ้งเตือน
เจ้าของปรับ stock สำหรับการแจกนอกเว็บและยืนยันส่งมอบได้
รองรับความคิดเห็น รายการบันทึก โปรไฟล์ และการดูแลชุมชน

รุ่น Phase 12 เพิ่ม Comment CRUD ครบทั้ง API และหน้าเว็บ: เพิ่ม อ่าน แก้ไข และลบ
ผู้เขียนแก้ไขข้อความของตนเองได้จากเมนู … โดยคงการตอบกลับและสิทธิ์ลบเดิม
อ่าน [วิธีรันรุ่นนี้และทดสอบ Docker](doc/comment-crud.md) ก่อนอัป Git และ deploy

## สมาชิกกลุ่ม

ZIP ต้นฉบับไม่มีข้อมูลสมาชิกครบทั้งทีม ตารางนี้ต้องเติมจากข้อมูลจริงก่อนส่ง ไม่ใช้การแบ่งหัวข้อศึกษาแทนหลักฐานผู้เขียนโค้ด

| ลำดับ | ชื่อ-นามสกุล | รหัสนักศึกษา | Section | Branch | หน้าที่จริง |
|---|---|---|---|---|---|
| 1 | รอข้อมูลสมาชิก | รอข้อมูล | รอข้อมูล | ชื่อ_รหัส_section | รอยืนยัน |
| 2 | รอข้อมูลสมาชิก | รอข้อมูล | รอข้อมูล | ชื่อ_รหัส_section | รอยืนยัน |
| 3 | รอข้อมูลสมาชิก | รอข้อมูล | รอข้อมูล | ชื่อ_รหัส_section | รอยืนยัน |
| 4 | รอข้อมูลสมาชิก | รอข้อมูล | รอข้อมูล | ชื่อ_รหัส_section | รอยืนยัน |

## Tech Stack

- Java 17+, Spring Boot 4.1.1, Maven Wrapper
- PostgreSQL, Spring Data JPA/Hibernate, Flyway V1–V10
- Thymeleaf, JavaScript, Leaflet และระบบค้นหา/เส้นทางเดิม
- Spring Security, session/CSRF, BCrypt, Google OAuth แบบเปิดตาม configuration
- ImageStorage: local volume หรือ Cloudinary; EmailService: SMTP หรือ Brevo ตาม configuration
- JUnit 5, Mockito, Spring Boot Test, H2 สำหรับ tests, Node test runner และ Playwright
- Docker Compose; GitHub Actions สำหรับ tests และ Docker build

## System Architecture

Controller → Service interface → implementation → Repository → Entity/Database
API ใช้ DTO; Mapper แปลงข้อมูลโดยไม่อ่าน Repository การประกอบ PostView อยู่ใน service transaction

- [SOLID พร้อมไฟล์และบรรทัด](doc/solid-analysis.md)
- [Design Patterns](doc/design-patterns.md)
- [Diagram ทั้งหมด](doc/diagrams/README.md)
- [Use Case Description](doc/diagrams/use-case-descriptions.md)

## Database Design (ER Diagram)

มี 12 ตารางแอป: users, user_profile_images, password_reset_tokens, food_posts, food_post_images,
reservations, notifications, reports, audit_events, post_comments, notification_preferences, saved_posts
One-to-One: สมาชิก–รูปโปรไฟล์/การตั้งค่าการแจ้งเตือน
One-to-Many: สมาชิก–โพสต์/การจอง และโพสต์–รูป/การจอง/ความคิดเห็น

- [ER Diagram](doc/diagrams/README.md#er)
- [Data Dictionary, FK, Index, Fetch และ Cascade](doc/data-dictionary.md)
- Migration: `code/src/main/resources/db/migration/` ห้ามแก้ migration ที่ apply บนฐานข้อมูลจริงแล้ว

## Installation & Setup

ติดตั้ง Docker Desktop/Engine และ Compose หรือใช้ JDK 17+ กับ PostgreSQL 17
ZIP ไม่มี `.env` หรือ credentials จริง ถ้าอัปเกรดระบบเดิม ให้ใช้ `.env`, APP_SECRET และ Compose project เดิม
สร้าง environment สำหรับระบบใหม่:

```powershell
# Windows PowerShell — รันในโฟลเดอร์ kku-foodshare
powershell -ExecutionPolicy Bypass -File .\scripts\setup-env.ps1
```

```bash
# Linux/macOS
bash scripts/setup-env.sh
```

## How to Run

```powershell
docker compose -p kku-foodshare-phase1 -f docker-compose.yml up --build -d
docker compose -p kku-foodshare-phase1 -f docker-compose.yml ps
docker compose -p kku-foodshare-phase1 -f docker-compose.yml logs -f app
```

เปิดพอร์ตตาม APP_PORT ใน `.env`: ค่าใหม่ปกติ http://localhost:8080; ถ้า `.env` เดิมเป็น 8081 ให้ใช้ http://localhost:8081
Flyway ปรับ schema และ Hibernate validate ให้ ไม่ต้องลบ database หรือ volumes ตอนอัปเกรด
หยุดด้วย `docker compose -p kku-foodshare-phase1 -f docker-compose.yml down`; อย่าเติม `-v` เมื่อต้องการเก็บข้อมูล

รันโดยไม่ใช้ Docker: ตั้ง DATABASE_URL แบบ JDBC, DATABASE_USER, DATABASE_PASSWORD, APP_SECRET อย่างน้อย 32 ตัวอักษร และ UPLOAD_DIR

```powershell
cd code
.\mvnw.cmd spring-boot:run
```

Linux/macOS ใช้ `./mvnw spring-boot:run`
Camera/GPS ต้องได้รับสิทธิ์ผู้ใช้และใช้ HTTPS หรือ localhost

## API Documentation

- Swagger UI: `/swagger-ui/index.html` หรือ `/swagger-ui.html`
- OpenAPI: `/v3/api-docs`
- [REST API contract และ endpoint inventory](doc/api.md)
- Health: `/actuator/health`

CRUD หลัก: food-posts, reservations และ comments; Delete คือปิดโพสต์/ยกเลิกการจอง/ซ่อนความคิดเห็นเพื่อรักษาประวัติ
คำขอที่เปลี่ยนข้อมูลต้องมี session และ CSRF token ตาม meta ใน HTML

## How to Run Tests

```powershell
cd code
.\mvnw.cmd clean verify
cd ..
node --test code/src/test/js/*.test.mjs
```

Linux/macOS ใช้ `./mvnw clean verify`
JUnit reports อยู่ใน `code/target/surefire-reports` เก็บผลของ commit ที่ส่งจริง

- [คู่มือ test และ browser checks](test/README.md)
- [ผลตรวจ Phase 12](test/reports/verification.md)

ผล Phase 12 และข้อจำกัดแสดงในรายงานข้างต้น รายงานของรุ่นเก่าไม่ใช่ผลทดสอบของรุ่นนี้
รัน tests ทั้งชุดใน Docker ก่อนอัป Git:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test-phase12.ps1
```

## Deployment URL

เอกสารต้นฉบับระบุ https://kku-foodshare.onrender.com แต่ยังยืนยันไม่ได้ว่าเว็บ public ใช้ commit ของ ZIP นี้
ทีมต้องตรวจ URL จริงและ Swagger ของรุ่นส่งก่อนนำเสนอ แล้วอัปเดตข้อความนี้

- URL ที่ต้องตรวจ: https://kku-foodshare.onrender.com
- Swagger ที่ต้องตรวจ: https://kku-foodshare.onrender.com/swagger-ui/index.html
- [Configuration และวิธี Deploy](doc/deployment.md)

## อัปเดต checkout เดิมจาก ZIP นี้

ใช้ไฟล์ชุดนี้แทนโฟลเดอร์ source เดิม และเก็บ `.git`/`.env` ของ checkout เดิมไว้ อย่าคัดลอกทับอย่างเดียวโดยไม่ลบไฟล์ที่ยกเลิก เพราะไฟล์ reminder เดิมอาจค้างและทำให้ dependency ไม่ตรงชุดที่ทดสอบ

ไฟล์ Java ที่ต้องไม่มีหลังอัปเดต: `code/src/main/java/com/kku/foodshare/service/impl/PickupReminderService.java`
ใช้ interface ที่ `service/PickupReminderService.java` และ implementation `service/impl/PickupReminderServiceImpl.java` แทน
ตรวจ diff และใช้รายการ removed ใน `test/reports/source-changes.csv` ประกอบการลบเอกสารเก่าเฉพาะชุดนี้

## Git Workflow

ใช้ main สำหรับรุ่นส่ง, develop สำหรับรวมงาน, branch รายคนตาม `ชื่อ_รหัสนักศึกษา_section`
แต่ละคน Commit/Push ด้วยบัญชีตนเองตามเกณฑ์ใบงาน และรวมผ่าน Pull Request ที่มี reviewer
ZIP ไม่มี Git history จึงไม่รับรองจำนวน commit/contributors/PR รายคน
GitHub Actions ที่มีอยู่รัน tests/build และเก็บ report ยังไม่มีขั้น Deploy อัตโนมัติใน workflow

## Project Structure

| ตำแหน่ง | หน้าที่ |
|---|---|
| `code/` | Java, Thymeleaf, static assets, config, migrations และ Maven/JUnit tests |
| `test/` | browser journeys, วิธีทดสอบ และผลตรวจ; คง src/test ตาม Maven convention |
| `doc/` | SOLID, Patterns, API, Data Dictionary, Diagrams และ Deployment |
| `doc/slide/` | ที่ใส่สไลด์ของทีมก่อนส่ง; ชุด source นี้ยังไม่มีสไลด์จริง |
| `img/` | ภาพประกอบเดิม; ไม่อ้างว่าเป็นหลักฐาน UI ของ commit ล่าสุด |
| `scripts/` | setup environment และ backup/restore |
| Root Docker/Compose | วิธีรัน/deploy ที่คงชื่อและ path เดิม |

เอาบันทึก Phase, แผนงานเก่า, HELP และรายงานเก่าที่อาจสับสนกับรุ่นส่งออกแล้ว
คง source, assets, migration, tests และเอกสารที่เกี่ยวกับใบงาน/การรันไว้
ดู [Third-party notices](doc/third-party.md) และ license notices ของ font/QR assets
