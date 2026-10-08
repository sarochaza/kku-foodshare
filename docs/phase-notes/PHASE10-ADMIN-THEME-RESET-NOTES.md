# Phase 10: ธีมสี แอดมินดูโพสต์ และลืมรหัสผ่าน

ต่อจาก GitHub branch sarocha_6733802967_01 commit a70c130 โดยแก้เฉพาะงานรอบนี้

## สิ่งที่เปลี่ยน
- ธีมดำ: ช่องค้นหา ตัวกรอง กระดิ่ง เมนูเลือกมุมมอง พื้นที่ไม่มีรายการ และช่องรหัสผ่านใช้พื้นผิวสีเทา ข้อความอ่านชัด ธีมที่เลือกยังใช้การจำค่าเดิมในเบราว์เซอร์
- แอดมิน: แท็บโพสต์ทั้งหมด ค้นหาชื่อโพสต์/ผู้โพสต์/จุดรับ กรองสถานะ แสดงยอดคงเหลือ ยอดจอง หน่วย และวันหมดเวลารับ พร้อมเปิดรายละเอียด รวมถึงโพสต์ที่ปิดแล้วสำหรับแอดมิน
- จำนวนรายงาน OPEN แสดงบนแท็บรายงาน โดยรักษาการตรวจรายงานและจัดการสมาชิกเดิม
- หน้าแอดมินรองรับมือถือและ PC และป้องกันผลการโหลดแท็บเก่าทับแท็บใหม่
- ลืมรหัสผ่าน: เพิ่มการส่งผ่าน Brevo HTTPS API สำหรับ Render Free โดยใช้ EmailService เดิม ระบบ token/validation/CSRF/cooldown เดิมคงอยู่
- หน้าตั้งรหัสใหม่เพิ่มปุ่มแสดง/ซ่อนทั้งสองช่อง ยืนยันรหัสและข้อความลิงก์หมดอายุใช้ระบบเดิม
- ไม่มีการเปลี่ยน schema, ข้อมูลฐานข้อมูล, จำนวนสต็อก, reservation logic, QR, แผนที่ หรือ SecurityConfig
- ยังไม่ได้เปลี่ยนที่เก็บรูป: รูปอัปโหลดบน Render Free ยังหายเมื่อ restart/redeploy/spindown จนกว่าจะทำงานเก็บรูปถาวรในรอบถัดไป

## เปิดระบบอีเมลจริงบน Render (ต้องตั้งค่าก่อนใช้งาน)
1. สมัคร Brevo Free: https://www.brevo.com/
2. เปิดใช้งาน transactional email และยืนยันอีเมลผู้ส่งตามขั้นตอนของ Brevo (หากบัญชีต้องผ่านการตรวจ ให้ดำเนินการให้ครบ)
3. สร้าง API Key ใน SMTP & API > API Keys ใช้ API Key ไม่ใช่ SMTP key
4. ใน Render > บริการ kku-foodshare > Environment ตั้ง:

```text
MAIL_ENABLED=true
MAIL_PROVIDER=brevo
BREVO_API_KEY=ใส่ API Key จริงเฉพาะใน Render
MAIL_FROM=อีเมลผู้ส่งที่ยืนยันกับ Brevo
APP_BASE_URL=https://kku-foodshare.onrender.com
```

ไม่ต้องเพิ่ม profile `mail` สำหรับ Brevo เพราะ profile ดังกล่าวเป็น SMTP เดิม
ไม่วาง API Key ใน Git, โค้ด, ZIP หรือส่งมาในแชท
Render Free บล็อกพอร์ต SMTP 25/465/587 ดังนั้นให้ใช้ HTTPS API ข้างต้น
Brevo Free มีโควตา 300 อีเมลต่อวันตามข้อมูลที่ตรวจเมื่อ 8 ตุลาคม 2026 บัญชีผู้ส่งต้องพร้อมและอยู่ในข้อกำหนดบริการ

5. Deploy โค้ดรอบนี้และบันทึก Environment จากนั้นทดสอบขอลิงก์ด้วยอีเมลบัญชีจริง ตรวจ inbox/spam เปิดลิงก์ภายใน 15 นาที ตั้งรหัสใหม่แล้วลองล็อกอิน
6. ลิงก์ใช้ซ้ำไม่ได้; คำขอถัดไปต้องเว้น 5 นาที; หน้าเว็บใช้ข้อความสำเร็จแบบเดียวกันสำหรับอีเมลที่มี/ไม่มีบัญชีเพื่อไม่เปิดเผยข้อมูลบัญชี
7. หากอีเมลไม่ถึง ให้ตรวจ Render logs และ Brevo transactional logs โดยไม่เปิดเผย API Key ระบบไม่แสดงรายละเอียดจากผู้ให้บริการต่อผู้ใช้

**สถานะการทดสอบ:** ตรวจการส่ง HTTP/payload/การจัดการ provider error ด้วยเซิร์ฟเวอร์ทดสอบแล้ว แต่ยังไม่ได้ส่งอีเมลจริง เนื่องจากต้องใช้บัญชี Brevo และ API Key ของเจ้าของเว็บ

## Build และรันในเครื่อง Windows
คัดลอกโปรเจกต์ลงโฟลเดอร์ใหม่ก่อน; ไม่ทับ .env ที่ใช้งานอยู่

```powershell
cd "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\Principle\KKU-FoodShare-Phase10-Admin-Theme-Reset\kku-foodshare"
if (!(Test-Path .\.env)) { Copy-Item "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\Principle\kku-foodshare\.env" ".\.env" }
docker compose -p kku-foodshare-phase1 up --build -d
docker compose -p kku-foodshare-phase1 ps
```

เปิด http://127.0.0.1:8081
ใช้ Compose project เดิมเพื่อใช้ volumes เดิม อย่ารัน down -v หรือลบ volumes
ในการเปิดใช้ Brevo บน cloud ให้ตั้ง Environment ใน Render; การรัน Compose ในเครื่องยังใช้การตั้งค่าอีเมลเดิมของ Compose ไม่ได้เปลี่ยน deployment configuration

## วิธีอัปเดต repo เดิม
นำไฟล์ใน kku-foodshare ของ ZIP ทับโค้ดใน repo เดิม (ไม่แตะ .git และ .env) แล้วรันเทสก่อน commit/push ตามกระบวนการเดิม Render ต้อง Deploy commit ใหม่จึงเห็นการเปลี่ยนแปลง

## การทดสอบ
```powershell
cd code
.\mvnw.cmd test
.\mvnw.cmd -DskipTests package
cd ..
node --test code/src/test/js/*.test.mjs
```

การตรวจ browser เพิ่มเติมต้องมี Playwright และ Chromium ในเครื่องทดสอบ:
```text
node test/dark-surfaces-check.cjs
node test/admin-posts-check.cjs
```
รองรับ CHROMIUM_PATH เมื่อใช้ Chromium ที่ติดตั้งไว้เอง

- Java tests: 83 tests ผ่าน (ฐานข้อมูล H2 ของเทส; ไม่เชื่อมต่อ Neon จริง)
- JavaScript regression: 117 tests ผ่าน
- Browser checks: ธีมดำและหน้าแอดมินที่ 390px/1440px, ค้นหา/กรอง, รายละเอียด, badge, การ escape ข้อความ และการสลับกลับรายงาน
- JavaScript syntax ผ่าน; Maven package ผ่าน
- ปรับเทสเดิม 2 จุดที่ล้มเหลวใน baseline ก่อนแก้รอบนี้: WebPagesTest คาดว่า / redirect ทั้งที่โค้ดเดิมคืน home; CommentRepliesTest ตั้ง mock ซ้ำด้วย when ทำให้ answer เก่ารับ null ปรับเป็น doAnswer เท่านั้น ไม่เปลี่ยน production behavior ของสองระบบนี้
- ยังต้องตรวจบน Render หลัง Deploy: ส่งอีเมลจริง, ธีมบนอุปกรณ์จริง, บัญชี ADMIN ของฐานข้อมูล production

## ไฟล์ที่เปลี่ยน/เพิ่ม
- .env.example (ตัวอย่างเท่านั้น ไม่มีค่าลับ)
- code/src/main/java/com/kku/foodshare/controller/api/ModerationController.java
- code/src/main/java/com/kku/foodshare/repository/ReportRepository.java
- code/src/main/java/com/kku/foodshare/service/ModerationService.java
- code/src/main/java/com/kku/foodshare/service/impl/ModerationServiceImpl.java
- code/src/main/java/com/kku/foodshare/service/impl/FoodCatalogServiceImpl.java (อนุญาตแอดมินอ่านรายละเอียดโพสต์ปิดแล้วเท่านั้น)
- code/src/main/java/com/kku/foodshare/service/impl/BrevoEmailService.java
- code/src/main/java/com/kku/foodshare/service/impl/SmtpEmailService.java
- code/src/main/resources/application.properties
- code/src/main/resources/static/css/app.css
- code/src/main/resources/static/css/theme.css
- code/src/main/resources/static/js/app.js
- code/src/main/resources/templates/admin.html
- code/src/main/resources/templates/reset-password.html
- code/src/test/java/com/kku/foodshare/service/AdminPostListingTest.java
- code/src/test/java/com/kku/foodshare/service/BrevoEmailServiceTest.java
- code/src/test/java/com/kku/foodshare/service/CommentRepliesTest.java
- code/src/test/java/com/kku/foodshare/WebPagesTest.java
- test/admin-posts-check.cjs
- test/dark-surfaces-check.cjs
- PHASE10-ADMIN-THEME-RESET-NOTES.md

ข้อมูลบริการ: https://render.com/docs/free และ https://developers.brevo.com/docs/send-a-transactional-email
