# Phase 12 — Comment CRUD

รอบนี้ต่อยอด Comment เดิมให้เพิ่ม อ่าน แก้ไข และลบได้ครบทั้ง REST API และหน้าเว็บ
คงการตอบกลับ การรายงานความคิดเห็น และสิทธิ์ลบเดิม ใช้ schema และ migrations เดิมทั้งหมด

## รันแอปใน Docker ตามวิธีที่ใช้ได้เดิม

แตก ZIP ใต้ `C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\Principle`
จะได้ `KKU-FoodShare-Phase12-Comment-CRUD\kku-foodshare` แล้วรัน PowerShell:

```powershell
cd "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\Principle\KKU-FoodShare-Phase12-Comment-CRUD\kku-foodshare"
if (!(Test-Path .\.env)) { Copy-Item "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\Principle\kku-foodshare\.env" ".\.env" }
docker compose -p kku-foodshare-phase1 up --build -d
docker compose -p kku-foodshare-phase1 ps
docker compose -p kku-foodshare-phase1 logs --tail=100 app
```

ใช้ `.env` เดิมที่รันได้ ถ้าไฟล์ต้นทางข้างต้นไม่มี ให้คัดลอกจากโฟลเดอร์ Phase11-1 ที่คุณรันได้แทน
พอร์ตขึ้นกับ `APP_PORT` ใน `.env` เดิม เช่น `8081` เปิด http://localhost:8081
ดูค่าพอร์ตด้วย `Select-String -Path .\.env -Pattern '^APP_PORT='`
Compose project ชื่อเดิมรักษา volumes ของแอปไว้ ไม่ต้องลบฐานข้อมูลเพื่อเพิ่ม Comment Update

## ทดสอบก่อนอัป Git และ deploy

จากโฟลเดอร์ `kku-foodshare` รัน:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test-phase12.ps1
```

script จะรัน 3 ขั้นตามลำดับและหยุดถ้ามีขั้นใดล้มเหลว:

| ขั้น | ตรวจอะไร | ผลลัพธ์ |
|---|---|---|
| java-tests | Java ทั้งชุด: JUnit 5 / Mockito / Spring Boot + H2 | `code/target/surefire-reports/` และ `test/reports/phase12/java-tests-docker.log` |
| js-tests | JavaScript ทั้งชุด รวม Comment UI | `test/reports/phase12/js-tests-docker.log` |
| postgres-tests | Comment CRUD และ journey ของโพสต์/การจอง/หน้าเว็บ พร้อม Flyway/Hibernate validation บน PostgreSQL 17 | `code/target/surefire-reports/` และ `test/reports/phase12/postgres-tests-docker.log` |

การทดสอบใช้ Compose project `kku-foodshare-tests` และฐานข้อมูล `foodshare_test` ของตนเอง
ไม่มีพอร์ตฐานข้อมูลเปิดออกมา และไม่เชื่อมฐานข้อมูลของแอปหรือ Neon
ต้องเชื่อมอินเทอร์เน็ตเพื่อดาวน์โหลด Docker images และ Maven dependencies ครั้งแรก
Surefire XML/TXT ของ class ที่รันซ้ำในขั้น PostgreSQL จะแสดงผลจากขั้น PostgreSQL ล่าสุด; logs ของแต่ละขั้นแยกไว้ครบ

หากต้องการรันแยก:

```powershell
docker compose -p kku-foodshare-tests -f compose.test.yaml run --rm java-tests
docker compose -p kku-foodshare-tests -f compose.test.yaml run --rm js-tests
docker compose -p kku-foodshare-tests -f compose.test.yaml run --rm postgres-tests
docker compose -p kku-foodshare-tests -f compose.test.yaml down
```

## ตรวจหน้าเว็บด้วยสองบัญชี

1. บัญชี A เปิดโพสต์ → ความคิดเห็น → เขียนข้อความ → ส่ง
2. กดเมนู … ของข้อความตนเอง → **แก้ไขความคิดเห็น** → เปลี่ยนข้อความ → **บันทึก**
3. รีเฟรชแล้วเปิดความคิดเห็นอีกครั้ง: เห็นข้อความใหม่ จำนวนความคิดเห็นไม่เพิ่ม
4. ทดลองแก้ไขแล้วกด × ในแถบ “แก้ไขความคิดเห็นของคุณ”: กลับไปเขียนความคิดเห็นใหม่
5. ทดลองตอบกลับ แล้วแก้ไขคำตอบ: คำตอบยังอยู่ใต้ความคิดเห็นต้นทาง
6. บัญชี B อ่านได้ แต่ไม่มีเมนูแก้ไขข้อความของ A แม้เป็นเจ้าของโพสต์หรือแอดมิน
7. ลบข้อความของ A: ข้อความหาย จำนวนลดลง และการตอบกลับที่มีอยู่ยังคงอ่านได้
8. ลองข้อความว่างหรือยาวเกิน 800 ตัวอักษร: ไม่บันทึก ถ้าบันทึกไม่สำเร็จ ร่างแก้ไขยังอยู่ให้ลองใหม่

## API สำหรับตรวจใน Swagger

เปิด http://localhost:8081/swagger-ui/index.html (เปลี่ยนพอร์ตตาม `.env`)
หัวข้อ `comment-controller` มี 5 operations ดังนี้:

| CRUD | Method | Endpoint | สำเร็จ |
|---|---|---|---|
| Create | POST | `/api/v1/food-posts/{postId}/comments` | 201 + `Location` |
| Read list | GET | `/api/v1/food-posts/{postId}/comments?page=0` | 200 |
| Read one | GET | `/api/v1/comments/{id}` | 200 |
| Update | PUT | `/api/v1/comments/{id}` | 200 |
| Delete | DELETE | `/api/v1/comments/{id}` | 204 |

Create: `{"body":"ยังมีอาหารไหม"}`
Reply: `{"body":"ขอรับหนึ่งกล่อง","parentCommentId":123}`
Update: `{"body":"ขอรับสองกล่อง"}`

ใช้ session ที่ล็อกอินแล้ว ทุกคำขอที่เปลี่ยนข้อมูลต้องส่ง CSRF token ตาม meta `_csrf`/`_csrf_header` ของหน้าเว็บ
Swagger แสดง endpoint แต่ไม่ได้ข้ามข้อกำหนด session/CSRF; ทดลองผ่านหน้าเว็บได้โดยส่ง token ให้อัตโนมัติ
400 = ข้อมูลไม่ถูกต้อง; 401 = ยังไม่ล็อกอิน; 403 = ไม่มีสิทธิ์หรือไม่มี CSRF; 404 = ไม่พบ/ถูกลบแล้ว
Error ของ controller ใช้รูปแบบเดิม `{status,message,fields}`

## ขอบเขตสิทธิ์และข้อมูล

| ผู้ใช้ | อ่าน | แก้ไข | ลบ |
|---|---|---|---|
| ผู้เขียน | ได้หลังล็อกอิน | ข้อความของตนเอง | ข้อความของตนเอง |
| เจ้าของโพสต์ | ได้หลังล็อกอิน | เฉพาะข้อความที่ตนเขียน | ความคิดเห็นในโพสต์ของตน |
| แอดมิน | ได้หลังล็อกอิน | เฉพาะข้อความที่ตนเขียน | ได้ตามสิทธิ์เดิม |
| สมาชิกอื่น | ได้หลังล็อกอิน | ข้อความของตนเอง | ข้อความของตนเอง |

API response เพิ่ม `canEdit` สำหรับแสดงเมนูแก้ไข; server ตรวจผู้เขียนซ้ำเสมอ
Update เปลี่ยนเฉพาะ `body`; ผู้เขียน โพสต์ เวลาสร้าง และ parent/reply-to ไม่เปลี่ยน
ใช้ transaction และ pessimistic lock เดิมของ Comment repository เพื่อจัดการ Update/Delete
Delete ยังคง soft delete จึงไม่ทำให้การตอบกลับเดิมสูญหาย
ไม่มีการแจ้งเตือน “ความคิดเห็นใหม่” จากการแก้ไขข้อความ

## โค้ดสำหรับอธิบายงาน

| ส่วน | ไฟล์ | หน้าที่ |
|---|---|---|
| Presentation | `code/src/main/java/com/kku/foodshare/controller/api/CommentController.java` | รับ HTTP/DTO/validation แล้วเรียก service interface |
| Request DTO | `code/src/main/java/com/kku/foodshare/dto/request/CreateCommentRequest.java`, `UpdateCommentRequest.java` | แยกข้อมูล Create/Update จาก Entity |
| Service interface | `code/src/main/java/com/kku/foodshare/service/CommentService.java` | สัญญา CRUD และ response view เดิม |
| Business logic | `code/src/main/java/com/kku/foodshare/service/impl/CommentServiceImpl.java` | สิทธิ์ผู้เขียน ตรวจข้อความ อ่าน/แก้ใน transaction และแปลงเป็น response view |
| Repository/Entity | `PostCommentRepository`, `PostComment` | ใช้การอ่าน active/lock และ schema เดิม |
| Frontend | `code/src/main/resources/static/js/comments.mjs` | เมนูแก้ไข โหมดบันทึก ยกเลิก และจัดการผลลัพธ์ล่าช้า |
| Integration test | `code/src/test/java/com/kku/foodshare/CommentCrudJourneyTest.java` | CRUD ผ่าน HTTP + persistence + permissions + validation + Swagger |
| UI tests | `code/src/test/js/comments.test.mjs` | การเรียก PUT, จำนวนข้อความ, โหมดแก้ไข และการแข่งขันของคำขอ |

Controller ไม่เรียก Repository โดยตรง; service ใช้ constructor injection และ interfaces ตามโครงสร้างเดิม
ผลที่รันจริงและรายการที่ต้องทดสอบในเครื่องคุณอยู่ที่ [รายงาน Phase 12](../test/reports/verification.md)

## หลังทดสอบผ่าน

คัดลอกไฟล์รุ่นนี้เข้าสู่ checkout Git ที่คุณใช้งาน โดยรักษา `.git` และ `.env` เดิม
ดู `git status` และ diff จากนั้น commit/push ผ่านบัญชีและ branch ของตนตามใบงาน
ให้ทีม review ผ่าน Pull Request ก่อน merge และ deploy ตาม configuration เดิม
หลัง deploy เปิดหน้าเว็บ, Comment CRUD และ Swagger ของ URL จริงอีกครั้งเพื่อยืนยันว่าขึ้นรุ่นใหม่แล้ว
