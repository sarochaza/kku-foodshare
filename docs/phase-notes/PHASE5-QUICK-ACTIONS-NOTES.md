# KKU FoodShare — Phase 5 Quick Actions

ต่อจาก **Phase4.1 QuickPreview** ที่ใช้งานล่าสุด ทำ 3 ฟีเจอร์ตามที่ตกลง:

1. **แก้การจองตรงหน้าโพสต์** — ปุ่ม − / + หรือพิมพ์จำนวน แล้วบันทึกได้เลย รวมถึงโพสต์ที่จองเต็มแล้ว ยกเลิกและเปิด QR เดิมได้ในหน้าเดียวกัน การแก้ใช้ PUT ของการจองเดิม จึงไม่สร้างการจองซ้ำ เลขอ้างอิงและรหัสรับอาหารเดิมยังอยู่
2. **โพสต์ของฉันอยู่ด้านบน** — ทางลัดในส่วนหัวทุกหน้าที่เข้าสู่ระบบ ใช้ได้ทั้งคอมพิวเตอร์และมือถือ
3. **จัดการจำนวนจากการ์ดเจ้าของโพสต์** — เพิ่มอาหาร ลดจำนวนที่ยังว่าง บันทึกแจกนอกเว็บ และแก้ยอดแจกที่บันทึกเกิน พร้อมตัวอย่างยอดหลังบันทึกและการยืนยัน

## กติกาจำนวน

`ยังจองได้ = จำนวนทั้งหมด − รอมารับ − รับผ่านเว็บ − แจกนอกเว็บ`

- ผู้จองเดิมได้รับการกันของก่อนเสมอ การลดหรือแจกนอกเว็บแตะได้เฉพาะส่วนที่ยังว่าง
- “ลดจำนวนที่ยังว่าง” ใช้เมื่อของเสีย/จำนวนลงเกิน ไม่เพิ่มยอดบริจาค
- “บันทึกแจกนอกเว็บ” ใช้เมื่อส่งมอบให้ผู้รับจากช่องทางอื่น เพิ่มยอดแจกนอกเว็บและยอดส่งมอบรวม
- “แก้ยอดแจกนอกเว็บที่ลงเกิน” ใช้แก้การบันทึกผิด ของส่วนนั้นจะกลับมาจองได้
- เพิ่มอาหารได้จนจำนวนทั้งหมดไม่เกิน 10,000 ถ้าโพสต์รับครบแล้วแต่ยังไม่หมดเวลา การเพิ่มจะเปิดให้จองอีกครั้ง
- โพสต์ปิดหรือหมดเวลาแล้วปรับจำนวนไม่ได้ การนำของทั้งหมดออกโดยไม่ได้แจกให้ใช้ “ปิดโพสต์” เพื่อรักษากติกาจำนวนทั้งหมดต้องมากกว่า 0
- โพสต์ที่จองเต็มยังแสดงการจองของสมาชิกคนนั้นให้แก้ไขได้
- สรุป “ส่งมอบแล้ว” รวมรับผ่านเว็บและแจกนอกเว็บ โดยยังแสดงสองช่องทางแยกบนการ์ด
- ยอดแจกนอกเว็บเก็บเป็นยอดสะสมของโพสต์ ยังไม่ใช่ประวัติรายธุรกรรม/ระบบคะแนน
- โพสต์ที่รับครบยังเก็บใน “โพสต์ของฉัน” พร้อมข้อมูลการส่งมอบ ไม่ลบประวัติ

## รักษาข้อมูลเดิม

Compose, Authentication, Security configuration, QR Scanner, รูปแบบรหัส 6 หลัก, การค้นหาสถานที่, แผนที่ และเส้นทางเดิมไม่ได้เปลี่ยน

เพิ่ม Flyway `V3__offline_distribution.sql` เพียงหนึ่งคอลัมน์ `offline_quantity` ค่าเริ่มต้น 0 และขยาย constraint จำนวนรวม โพสต์ สมาชิก การจอง รูป และรหัส QR เดิมยังอยู่ ไม่แก้ V1/V2 ไม่สร้างฐานข้อมูลใหม่

การแก้จำนวนใช้ **post write lock เดียวกับการจอง** พร้อม `expectedVersion` ป้องกันยอดเก่าและคำขอซ้ำ ถ้ามีการจองหรือปรับยอดแทรก ระบบตอบ 409 และให้ตรวจยอดล่าสุดอีกครั้ง ข้อมูลที่ใช้ยืนยันถูกตรึงไว้กับตัวอย่างยอดที่เห็น แม้มีคำตอบโหลดใหม่เข้ามาระหว่างยืนยัน

หากคำตอบการสร้างการจองหาย ระบบตรวจการจองที่เซิร์ฟเวอร์ก่อนเสนอการจองใหม่ เมื่อพบการจองแล้วสามารถยกเลิกและจองใหม่ได้ด้วยรหัสคำขอใหม่ หากยังตรวจผลไม่ได้จะคงรหัสคำขอเดิมไว้เพื่อไม่สร้างซ้ำ

## รันบน Windows / PowerShell

แตก ZIP แล้วเข้า **โฟลเดอร์ย่อย `kku-foodshare` ที่มี `compose.yaml`**:

```powershell
cd "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\KKU-FoodShare-Phase5-QuickActions\kku-foodshare"
Copy-Item "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\KKU-FoodShare-Phase4-1-QuickPreview\kku-foodshare\.env" ".\.env"
Test-Path .\.env
docker compose -p kku-foodshare-phase1 up --build -d
docker compose -p kku-foodshare-phase1 ps
```

คัดลอก `.env` เดิมครั้งแรกเท่านั้น ถ้าปลายทางมีไฟล์เดิมแล้วไม่ต้องคัดลอกซ้ำ ห้ามสร้าง APP_SECRET ใหม่ เพราะใช้ถอดรหัสรับอาหารที่ยังค้างอยู่ ห้ามเปลี่ยนชื่อ Compose project หรือ database credentials เดิม

เปิด **http://127.0.0.1:8081** หรือ **http://localhost:8081** รอ `app` และ `db` เป็น `healthy` แล้วกด Ctrl+F5 เพื่อโหลด JavaScript ใหม่

คำสั่ง Build นี้ใช้ project และ volumes เดิม ไม่ต้องสั่ง down และ **อย่าใช้ `down -v`** หากต้องการรักษาข้อมูล ใน ZIP ไม่มี `.env` และไม่มีข้อมูลฐานข้อมูลจริง

ตรวจกรณีเปิดไม่ขึ้น:

```powershell
docker compose -p kku-foodshare-phase1 logs --tail=100 app
```

## ผลการตรวจ

- Maven/JUnit: **55 tests ผ่าน, 0 failures, 0 errors, 0 skipped** รวมชุดเดิมและ 6 กรณีใหม่สำหรับการจอง/stock/migration
- Maven package: **BUILD SUCCESS** หลังผ่านชุดทดสอบ
- JavaScript/Node: **28 tests ผ่าน** รวมชุดแผนที่ ค้นหาสถานที่ เส้นทาง QR เดิม และ 6 กรณีใหม่
- StockPolicy: compile โค้ดจริงด้วย Java 17 และผ่านการตรวจขอบเขต/ไล่กรณีจำนวนทั้งหมด 1–20 โดยไม่กินของที่จองไว้
- Browser journey ใหม่: ใช้หน้าเว็บจริงและ Backend จริงในฐานทดสอบ H2 ทดสอบการจอง แก้/ยกเลิก จองเต็ม QR แจกนอกเว็บ แก้ยอด เพิ่ม/ลด stock ข้อมูลเก่า 409 และเน็ตขาดหลังเซิร์ฟเวอร์จองสำเร็จ
- Browser journey เดิม: ผ่าน 17 กรณี (สร้าง/แก้โพสต์ รูป ค้นหา Onboarding แผนที่ ตำแหน่ง เส้นทาง จอง ส่งมอบ โปรไฟล์ แจ้งเตือน และ fallback) พร้อมตรวจ 20 กรณีขนาดจอ
- Mobile: owner stock และการจองตรงหน้าโพสต์ไม่ล้นที่ 320, 360, 390, 768, 1366 px รวม QR ที่ 320, 390, 1366 px ปุ่มกดหลักอย่างน้อย 44 px
- แก้ CSS หนึ่งจุดให้ checkbox ตัวกรองหน้าแรกอยู่ภายในแถบเลื่อน ป้องกันหน้าแรกล้นที่ 360 px โดยไม่เปลี่ยนตัวกรอง/Onboarding
- ตรวจ syntax JavaScript และตรวจทานโค้ดอิสระ แก้ทั้งกรณีข้อมูลเปลี่ยนระหว่างยืนยันและเน็ตขาดแล้วจองใหม่ พร้อม regression tests ที่ล้มก่อนแก้และผ่านหลังแก้

การทดสอบ Browser ใช้ fixture สำหรับบริการภายนอกของแผนที่/ค้นหาสถานที่/เส้นทาง ผลนี้ยืนยันการทำงานร่วมกันของ UI และ Backend ไม่ใช่ SLA ของบริการภายนอก Migration ทดสอบรักษาแถวเดิมและ constraint บน H2 PostgreSQL mode; สภาพแวดล้อมนี้ไม่มี Docker จึงไม่ได้รัน Compose/PostgreSQL ของเครื่องคุณโดยตรง

ไฟล์หลักที่เพิ่ม/แก้:

- `code/src/main/resources/static/js/quick-actions.js` (ใหม่), `app.js`, `static/css/app.css`
- `code/src/main/resources/templates/fragments.html`, `my-posts.html`
- `code/src/main/java/com/kku/foodshare/domain/StockPolicy.java` (ใหม่)
- `code/src/main/java/com/kku/foodshare/service/OwnerStockService.java` (ใหม่)
- `code/src/main/java/com/kku/foodshare/controller/api/OwnerStockController.java` (ใหม่)
- `domain/entity/FoodPost.java`, `dto/response/PostView.java`, `mapper/PostViewMapper.java`
- `repository/FoodPostRepository.java`, `repository/ReservationRepository.java`
- `service/ReservationService.java`, `service/impl/ReservationServiceImpl.java`, `service/impl/FoodCatalogServiceImpl.java`
- `controller/api/ReservationController.java`
- `code/src/main/resources/db/migration/V3__offline_distribution.sql` (ใหม่)
- Tests: `FoodJourneyTest.java`, `WebPagesTest.java`, `OfflineMigrationTest.java` (ใหม่), `StockPolicyChecks.java` (ใหม่), `quick-actions.test.mjs` (ใหม่)
- `test/quick-actions-journey.cjs`, `test/home-overflow-check.cjs` (ใหม่), `test/browser-journey.cjs` (ปรับ selector ให้ตรง UI การจองใหม่)
- `test/PHASE5-VERIFICATION.md`, `docs/superpowers/plans/2026-10-06-owner-quick-actions*.md`

ดูคำสั่งรันซ้ำและหลักฐาน Browser ใน `test/PHASE5-VERIFICATION.md` ภาพหน้าใหม่อยู่ใน `img/phase5/`
