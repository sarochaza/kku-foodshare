# Phase 12 — Comment CRUD verification

ตรวจจาก source รุ่น Phase 12 ที่ส่งใน ZIP นี้ วันที่ 9 ตุลาคม 2026 (Asia/Bangkok)

| การตรวจ | ผล | หลักฐาน/ข้อจำกัด |
|---|---|---|
| JavaScript ทั้งชุด | PASS — 130 tests, 0 failures | `node-tests.txt` |
| Comment UI tests | PASS — 20 tests, 0 failures | `phase12/comment-js-tests.txt` |
| Java 17 syntax parser | PASS — 6 ไฟล์ Java ที่แก้/เพิ่ม | ตรวจ parse เท่านั้น ไม่ใช่การ compile/type-check application |
| YAML test Compose | PASS — parse ได้ | services/command/volumes ถูกอ่านได้; ยังไม่ได้ตรวจด้วย Docker Compose |
| ตรวจโค้ด Comment CRUD | แก้ findings แล้ว | ป้องกัน More/PUT race และใช้ getId() ของ lazy proxies ใน test |
| Maven `verify` / JUnit | BLOCKED ก่อน compile | ไม่สามารถ resolve DNS ของ repo.maven.apache.org เพื่อโหลด Spring Boot parent; `phase12/maven-attempt.txt` |
| Docker build + PostgreSQL tests | ยังไม่ได้รัน | ไม่มี Docker runtime ในสภาพแวดล้อมตรวจ |
| Browser journey | ยังไม่ได้รัน | Chromium executable ไม่มีในสภาพแวดล้อมตรวจ; ตรวจหน้าเว็บตาม doc/comment-crud.md |
| Git push / public deployment verification | ต้องทำหลัง Docker ผ่านบนเครื่องคุณ | ไม่มีการอ้างว่า ZIP รุ่นนี้ deploy แล้ว |

## Tests ที่เพิ่ม

`CommentCrudJourneyTest` มี 8 integration test methods สำหรับครบ CRUD/status/Location,
persistence หลัง reload, author-only edit, validation 1–800, reply links/createdAt,
404 ของรายการหาย/ถูกลบ, reply หลัง root ถูกลบ, authentication/CSRF และ Swagger endpoints
Java tests เหล่านี้ยังไม่ได้ execute เพราะติด dependency resolution

Comment JavaScript tests ครอบคลุม PUT แทน POST, แทนที่ข้อความโดยไม่เพิ่มจำนวน,
ยกเลิกการแก้ไข, บันทึกไม่สำเร็จแล้วรักษาร่าง, สลับ edit/reply,
กั้น pagination ระหว่างบันทึก, ผล PUT เก่าหลังสลับโพสต์ และข้อมูลว่าง/ยาวเกิน
Tests เดิม 13 รายการของ Comment ยังคงผ่าน รวมตอบกลับ ลบ รายงาน และ escape ข้อความ

ทดสอบ UI เพิ่มแบบ red/green: 4 tests ของฟีเจอร์แก้ไขล้มเหลวก่อนเพิ่ม implementation
และ test pagination race ล้มเหลวก่อนเพิ่ม guard จากนั้นผ่านพร้อมชุดเดิมทั้งหมด

## สิ่งที่ยังต้องรันบน Windows

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test-phase12.ps1
```

รันจากโฟลเดอร์ `kku-foodshare` ด้วย Docker Desktop
script ใช้ฐานข้อมูลทดสอบแยก และเก็บ log รายขั้นใน test/reports/phase12
หลัง automated tests ผ่าน ตรวจ Comment ด้วยสองบัญชีตามคู่มือก่อนอัป Git และ deploy

รายงานเก่า/ไฟล์ผลใน ZIP ต้นฉบับเป็นหลักฐานของรุ่นก่อนหน้า ไม่ใช้ยืนยัน Java/PostgreSQL ของ Phase 12
