# Tests

## Phase 12 — Comment CRUD

จาก `kku-foodshare` รัน `powershell -ExecutionPolicy Bypass -File .\scripts\test-phase12.ps1`
เพื่อทดสอบ Java/H2, JavaScript และ Comment/ระบบเดิมบน PostgreSQL ทดสอบแยกจากฐานข้อมูลของแอป
ไม่ต้องติดตั้ง Java/Maven/Node บน Windows เมื่อใช้ Docker Desktop
รายละเอียดคำสั่งและการตรวจหน้าเว็บอยู่ที่ [Comment CRUD](../doc/comment-crud.md)
`CommentCrudJourneyTest` ใช้ API, service และฐานข้อมูลจริงใน test context เพื่อตรวจ CRUD, validation,
สิทธิ์ผู้เขียน, CSRF, soft delete, ความสัมพันธ์การตอบกลับ และ OpenAPI

## Java — JUnit 5, Mockito และ Spring Boot Test

จาก `kku-foodshare/code`:

```powershell
.\mvnw.cmd clean verify
```

Linux/macOS ใช้ `./mvnw clean verify`; reports อยู่ใน `code/target/surefire-reports`
H2 ใช้สำหรับ tests; runtime ใช้ PostgreSQL + Flyway

SubmissionContractTest ตรวจ Layer, dependencies, API profile contract และ storage/รูป local เดิม
SubmissionContractChecks เป็น core checks เดียวกันที่รันแยกได้โดยไม่ต้องมี JUnit runner หรือ database
Tests เดิมครอบคลุม booking/stock/concurrency, QR, comments, notifications, reminder, onboarding และ password reset
Provider tests ใช้ fixture ในเครื่อง ไม่ส่งอีเมลออกไปจริง

## JavaScript

จากโฟลเดอร์โปรเจกต์หลัก:

```bash
node --test code/src/test/js/*.test.mjs
```

## PostgreSQL

.github/workflows/verify.yml มี PostgreSQL 17 และคำสั่งรัน migration/schema validation กับ integration tests
ใช้ฐานข้อมูลทดสอบว่างเท่านั้น CI configuration ไม่ใช่หลักฐานว่ารุ่นนี้รัน CI ผ่านแล้ว

## Browser

ติดตั้ง Node.js/Playwright แล้วเปิดแอปกับฐานข้อมูลทดสอบ:

```powershell
cd test
npm install
npx playwright install chromium
$env:TEST_BASE_URL="http://127.0.0.1:8081"
node browser-journey.cjs
node quick-actions-journey.cjs
$env:NODE_PATH=(Resolve-Path .\node_modules).Path
node onboarding-check.cjs
node pickup-reminder-check.cjs
```

onboarding-check ใช้ frontend fixture/mock HTTP boundary; Java integration ตรวจ API/DB แยก
บาง scripts สร้างสมาชิกและโพสต์ทดสอบ ห้ามรันกับ production database
ดู scripts อื่นในโฟลเดอร์ test ตามฟีเจอร์

## Results

[ผลตรวจชุด source นี้](reports/verification.md) แยกผลที่รันจริงจาก Maven/JUnit/browser/PostgreSQL ที่ยังต้องรัน
ไม่มีการนำรายงาน Java ของ Phase เก่ามาอ้างเป็นผลรุ่นนี้
