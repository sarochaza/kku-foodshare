
## สรุปผลการตรวจสอบ

| การตรวจ | ผล | หลักฐาน/ข้อจำกัด |
|---|---|---|
| JavaScript ทั้งชุด | PASS — 130 tests, 0 failures | ผลจาก `node-tests.txt` ของรอบที่รายงานจำนวนนี้ |
| Comment UI tests | PASS — 20 tests, 0 failures | ผลจาก `comment-js-tests.txt` |
| Java 17 syntax parser | PASS — 6 ไฟล์ Java ที่แก้/เพิ่ม | เป็นการตรวจ syntax เบื้องต้น ส่วนการ compile และ execute มีผล Maven แยกต่างหาก |
| YAML test Compose | PASS — parse ได้ และมีการรันทดสอบผ่าน Docker Compose แล้ว | Log การรัน Docker test services |
| ตรวจโค้ด Comment CRUD | แก้ findings แล้ว | ป้องกัน More/PUT race และใช้ `getId()` ของ lazy proxies ใน test |
| Maven / JUnit ร่วมกับ PostgreSQL | PASS — 48 tests, 0 failures, 0 errors, 0 skipped | `postgres-tests-docker.txt` จบด้วย `BUILD SUCCESS` |
| ฐานข้อมูล PostgreSQL และ Flyway | PASS | เชื่อมต่อ PostgreSQL ใน Docker ได้ และ Flyway ตรวจสอบและใช้ migrations ครบ 10 รายการ |
| Browser automation | PASS — 9 scripts, 0 failures, 0 not run | `summary.json` และ Log ทั้ง 9 scripts ของรอบที่ผ่านครบ |
| Git push / public deployment | ต้องตรวจหลักฐานแยกจากผลทดสอบ | ผลทดสอบในเครื่องไม่ได้ยืนยันว่าโค้ดเดียวกันถูก deploy บน URL สาธารณะแล้ว |

## การทดสอบ Comment CRUD

`CommentCrudJourneyTest` มี 8 integration test methods ครอบคลุม:

- การสร้าง อ่าน แก้ไข และลบคอมเมนต์ รวม HTTP status และ Location header
- การบันทึกข้อมูลและอ่านกลับหลัง reload
- การอนุญาตให้เจ้าของคอมเมนต์แก้ไขได้
- Validation ความยาวข้อความ 1–800 ตัวอักษร
- ข้อมูลการตอบกลับและ `createdAt`
- การตอบกลับ 404 เมื่อไม่พบรายการหรือรายการถูกลบ
- การตอบกลับหลังคอมเมนต์หลักถูกลบ
- Authentication, CSRF และ Swagger endpoints

ผลเฉพาะคลาสดูได้จากรายงาน
`com.kku.foodshare.CommentCrudJourneyTest.txt`
และ `TEST-com.kku.foodshare.CommentCrudJourneyTest.xml`

## การทดสอบ Comment UI

Comment JavaScript tests ครอบคลุม:

- ส่ง PUT เมื่อแก้ไขคอมเมนต์ แทนการสร้างใหม่ด้วย POST
- แทนที่ข้อความเดิมโดยไม่เพิ่มจำนวนคอมเมนต์
- ยกเลิกการแก้ไข
- รักษาข้อความร่างเมื่อบันทึกไม่สำเร็จ
- สลับระหว่างการแก้ไขและการตอบกลับ
- ป้องกันการโหลดหน้าถัดไประหว่างบันทึก
- ป้องกันผล PUT เก่ากระทบโพสต์ที่เปิดใหม่
- ตรวจข้อความว่างและข้อความยาวเกินกำหนด

Tests เดิมของ Comment ยังคงครอบคลุมการตอบกลับ ลบ รายงาน และ escape ข้อความ

ระหว่างพัฒนาใช้การทดสอบแบบ red/green: tests ของฟีเจอร์แก้ไขและ pagination race ล้มเหลวก่อนเพิ่ม implementation หรือ guard และผ่านหลังแก้ไข

## การทดสอบร่วมกับฐานข้อมูลจริง

ชุด `postgres-tests` รัน Java/Spring Boot ร่วมกับ PostgreSQL จริงใน Docker โดยใช้ฐานข้อมูลทดสอบแยก

ผลที่บันทึกใน Log:

- ฐานข้อมูลพร้อมใช้งาน
- Spring Boot เชื่อมต่อ PostgreSQL ได้
- Flyway ตรวจสอบและใช้ migrations ครบ 10 รายการ
- Tests run: 48
- Failures: 0
- Errors: 0
- Skipped: 0
- BUILD SUCCESS

จำนวน 48 หมายถึงกรณีทดสอบในรอบ PostgreSQL ที่อ้างอิง ไม่ใช่จำนวนคลาสหรือจำนวน Browser scripts

## การทดสอบ Browser automation

ผลรอบที่ผ่านครบ: **ผ่าน 9 scripts | ไม่ผ่าน 0 | ไม่ได้รัน 0**

| ไฟล์ | ขอบเขตการตรวจ |
|---|---|
| about-image-check | ตรวจการอ้างอิงภาพจาก source |
| admin-posts-check | ตรวจหน้าโพสต์สำหรับผู้ดูแลด้วยข้อมูลจำลอง |
| dark-surfaces-check | ตรวจ UI ในธีมมืดด้วยข้อมูลจำลอง |
| onboarding-check | ตรวจคำแนะนำเริ่มต้นใช้งานด้วยข้อมูลจำลอง |
| phase10-ui-check | ตรวจ UI ที่ชุด Phase 10 ครอบคลุมด้วยข้อมูลจำลอง |
| pickup-reminder-check | ตรวจ UI แจ้งเตือนรับอาหารด้วยข้อมูลจำลอง |
| browser-journey | ตรวจเส้นทางใช้งานหลักบนเว็บในเครื่อง |
| quick-actions-journey | ตรวจการจอง QR และการปรับสต็อกบนเว็บในเครื่อง |


การตรวจ source และ fixture ไม่ได้ยืนยัน API จริง ส่วน live tests บางขั้นใช้ข้อมูลจำลองของบริการภายนอก เช่น การค้นหาสถานที่และเส้นทาง

ผล Browser ชุดนี้ไม่ได้ใช้แทนหลักฐาน Comment CRUD เฉพาะคลาสหรือ Comment UI tests

## วิธีรันทดสอบซ้ำ

รันจากโฟลเดอร์ `kku-foodshare` โดยเปิด Docker Desktop:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test-phase12.ps1
