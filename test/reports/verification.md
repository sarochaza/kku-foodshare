# ผลการทดสอบ KKU FoodShare

ทดสอบบนเครื่องผู้พัฒนาด้วย Docker และเว็บที่ http://localhost:8081

| การตรวจ | ผล | หลักฐาน/ข้อจำกัด |
|---|---|---|
| JavaScript ทั้งชุด | PASS — 130 tests, 0 failures | `node-tests.txt` เป็นผลการตรวจรอบเดิม |
| Comment UI tests | PASS — 20 tests, 0 failures | `phase12/comment-js-tests.txt` หรือ `backend-test-results/comment-js-tests.txt` หากเปลี่ยนชื่อโฟลเดอร์แล้ว |
| Java 17 syntax parser | PASS — 6 ไฟล์ Java ที่แก้/เพิ่ม | ตรวจ syntax เท่านั้น ไม่ใช่การ compile/type-check application |
| YAML test Compose | PASS — parse ได้ และใช้รันทดสอบผ่าน Docker แล้ว | `compose.test.yaml` และ Docker test logs |
| ตรวจโค้ด Comment CRUD | แก้ findings แล้ว | ป้องกัน More/PUT race และใช้ `getId()` ของ lazy proxies ใน test |
| Maven / JUnit ผ่าน Docker | PASS — 48 tests, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS | `code/target/surefire-reports/` และ `java-tests-docker.log`; ยืนยันเฉพาะคำสั่ง Maven ที่สคริปต์เรียก |
| Docker build และ PostgreSQL tests | PASS — All Docker test commands passed | Logs ของ `java-tests`, `js-tests` และ `postgres-tests` ในโฟลเดอร์รายงาน Backend |
| Browser test suite | PASS — 9 ชุด, 0 failed, 0 not run | `test/reports/browser/<รอบล่าสุด>/summary.md`, `summary.json`, `summary.csv` พร้อม logs และภาพหน้าจอ |
| การแสดงผลบนจอ 320px | PASS | ผ่าน `quick-actions-journey` หลังแก้ส่วนหัวที่ล้นหน้าจอ |
| Git push และ public deployment verification | deployแล้ว | ตรวจโค้ดที่ push และ deploy แล้ว รวมถึง URL สาธารณะและ Swagger UI หลัง deploy |

## ขอบเขตของผลการทดสอบ

- Browser suite มีทั้งการตรวจ source, การใช้ fixtures และการทดสอบกับเว็บ local
- ผลที่ใช้ fixtures ไม่ได้ยืนยันการเชื่อมต่อบริการภายนอกจริง เช่น Google Login, Cloudinary และ Brevo
- รายงาน Backend อยู่ใน `test/reports/phase12/` หรือ `test/reports/backend-test-results/` หากเปลี่ยนชื่อแล้ว
- รายงาน Browser ใช้โฟลเดอร์รอบล่าสุดที่ได้ `PASS 9 / FAIL 0 / NOT_RUN 0`
- ผลการทดสอบ local ไม่ได้ยืนยันว่าโค้ดรุ่นล่าสุด deploy บนเว็บสาธารณะแล้ว
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
