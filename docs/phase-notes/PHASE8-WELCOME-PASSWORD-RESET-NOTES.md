# หน้าเริ่มต้นและระบบลืมรหัสผ่าน

## สิ่งที่ปรับในรอบนี้

- ใช้ ZIP `KKU-FoodShare-Phase8-Notifications-Guest-Home.zip` ที่ส่งมาล่าสุดเป็นฐาน
- ผู้ที่ยังไม่ล็อกอินเปิด `/` ได้ทั้ง localhost และ 127.0.0.1: แนะนำว่าเว็บทำอะไร ปุ่มสร้างบัญชี/เข้าสู่ระบบ และขั้นตอนฝั่งผู้รับกับผู้แบ่งปันอยู่ก่อนแผนที่
- คนที่ยังไม่ล็อกอินดูอาหารและแผนที่ได้ ไม่บังคับล็อกอินตั้งแต่เข้าเว็บ
- ไม่ขอตำแหน่งหรือเปิด tour อัตโนมัติกับ guest; ผู้ใช้ยังกดตำแหน่งของฉันได้ตามเดิม และเปิด tour ได้ผ่าน `/?guide`
- แก้ guest ถูกส่งไป login จากการโหลดพรีวิวคอมเมนต์อัตโนมัติ: ไม่เรียก optional API ที่ต้องล็อกอินบนหน้าสาธารณะ และแสดงปุ่มเข้าสู่ระบบในส่วนคอมเมนต์ของ detail โดยไม่แตะ security configuration เดิม
- คงหน้า `/home` สำหรับผู้ล็อกอิน ฟีด แผนที่ QR การจอง จำนวนอาหาร และการแจ้งเตือนเดิมไว้
- รีเซ็ตรหัสผ่านด้วยลิงก์ทางอีเมลที่หมดอายุ 15 นาทีและใช้ได้ครั้งเดียว เก็บ hash ของ token ไม่แสดงลิงก์ในหน้าเว็บหรือ log
- ป้องกันบัญชีที่ถูกระงับใช้ reset token, จัดการคำขอมากเกินไปเป็นข้อความบนฟอร์ม และแสดงข้อความสำเร็จบนหน้า login
- หาก SMTP ส่งไม่ได้ ลบ token ที่ส่งไม่สำเร็จเพื่อไม่ให้ติด cooldown 5 นาทีโดยไม่ได้รับอีเมล ข้อความตอบยังเหมือนกันทั้งอีเมลที่มี/ไม่มีบัญชีเพื่อไม่เปิดเผยข้อมูลสมาชิก
- เพิ่ม `compose.mail-local.yaml` เป็นตัวเลือกสำหรับทดสอบเท่านั้น ไม่แก้ `compose.yaml`, `.env`, security config หรือ migrations เดิม

## 1. รันโปรเจกต์

เปิด PowerShell ในโฟลเดอร์ `kku-foodshare` ที่มี `compose.yaml` และเช็ก:

```powershell
Get-Location
Test-Path .\compose.yaml
```

ครั้งแรกถ้ายังไม่มี `.env` ให้คัดลอกจากโปรเจกต์ที่ใช้งานอยู่เดิม (ไม่ต้องคัดลอกซ้ำทุกครั้ง):

```powershell
if (-not (Test-Path .\.env)) {
    Copy-Item "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\Principle\KKU-FoodShare-Phase8-Maps-Fix-Updated (1)\kku-foodshare\.env" ".\.env"
}
```

อย่าสร้าง DATABASE_PASSWORD ใหม่ เพราะโปรเจกต์ `kku-foodshare-phase1` ใช้ฐานข้อมูลเดิม

```powershell
docker compose -p kku-foodshare-phase1 up --build -d
docker compose -p kku-foodshare-phase1 ps
```

เปิด http://localhost:8081 หรือ http://127.0.0.1:8081 ตาม `APP_PORT` ที่ตั้งไว้ใน `.env`
รอ app เป็น healthy และกด Ctrl+F5 หากยังแสดง CSS เก่า ถ้าเคยเข้าสู่ระบบให้ logout หรือเปิดหน้าต่าง incognito เพื่อดูหน้าสำหรับ guest

## 2. ทดสอบลืมรหัสผ่านบนเครื่องโดยยังไม่ตั้งค่า SMTP จริง

ตัวเลือกนี้ใช้ Mailpit รับอีเมลทดสอบไว้ในเครื่อง **ไม่ส่งเข้ากล่อง Gmail/อีเมลจริง** และไม่เหมาะสำหรับ deploy ให้คนอื่นใช้งาน

1. เปิด `.env` บนเครื่อง (อย่าส่งเนื้อหาให้ผู้อื่น) ตรวจ `APP_PORT=8081` หรือพอร์ตที่ใช้งานอยู่ ไฟล์ override จะสร้างลิงก์ localhost ตามพอร์ตนี้ให้อัตโนมัติ ไม่ต้องเปลี่ยน `.env` ถ้าพอร์ตเดิมถูกต้องแล้ว
2. รันโดยเพิ่มไฟล์ override:

```powershell
docker compose -p kku-foodshare-phase1 -f compose.yaml -f compose.mail-local.yaml up --build -d
docker compose -p kku-foodshare-phase1 -f compose.yaml -f compose.mail-local.yaml ps
```

3. สมัครบัญชีทดสอบด้วยอีเมลใหม่ แล้ว logout
4. หน้า login กดลืมรหัสผ่าน ใส่อีเมลบัญชีที่เพิ่งสมัคร
5. เปิด http://127.0.0.1:8025 กล่องจดหมายทดสอบ แล้วเปิดอีเมลที่ส่งถึงบัญชีนั้น
6. กดลิงก์ตั้งรหัสผ่าน ใส่รหัสใหม่เหมือนกันสองช่อง แล้วบันทึก
7. ต้องกลับมาหน้า login พร้อมข้อความสำเร็จ รหัสใหม่ล็อกอินได้ รหัสเก่าล็อกอินไม่ได้
8. เปิดลิงก์เดิมซ้ำ ต้องแจ้งหมดอายุ/ใช้แล้ว

กล่องทดสอบเปิดได้โดยไม่ต้องล็อกอินและเห็นจดหมายทั้งหมด จึง bind เฉพาะ localhost เท่านั้น ใช้บัญชีทดสอบและอย่าเปิดพอร์ต 8025 ให้เครือข่ายภายนอก
เมื่อจะกลับไปรันปกติ/SMTP จริง ไม่ใช้ override และหยุดกล่องทดสอบ:

```powershell
docker compose -p kku-foodshare-phase1 -f compose.yaml -f compose.mail-local.yaml stop mailpit
docker compose -p kku-foodshare-phase1 up -d --force-recreate app
```

## 3. ส่งลิงก์เข้าอีเมลจริง

ระบบเดิมรองรับ SMTP ผ่าน `mail` profile อยู่แล้ว ผู้ดูแลต้องมีบัญชีผู้ส่งที่ใช้กับ provider ได้
แก้ค่าต่อไปนี้ใน `.env` ของเครื่องเท่านั้น (ตัวอย่าง Gmail):

```dotenv
SPRING_PROFILES_ACTIVE=mail
APP_BASE_URL=http://localhost:8081
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=YOUR_SENDER@gmail.com
MAIL_PASSWORD=YOUR_APP_PASSWORD
MAIL_FROM=YOUR_SENDER@gmail.com
```

- ถ้ามี Google login เดิม ใช้ `SPRING_PROFILES_ACTIVE=google,mail` แทน `mail` อย่าลบค่าตั้ง Google เดิม
- `MAIL_PASSWORD` เป็น App Password ของบัญชีผู้ส่ง ไม่ใช่รหัส login เว็บ/รหัส Gmail ปกติ; App Password ต้องเปิด 2-Step Verification และบางบัญชีองค์กรอาจไม่อนุญาต
- ค่า `YOUR_...` เป็น placeholder ต้องแทนด้วยค่าจริงบนเครื่อง ห้าม commit `.env` หรือส่งรหัสมาในแชท
- `APP_BASE_URL` ต้องเป็น URL ที่ผู้กดลิงก์เปิดถึง: localhost ใช้ได้เฉพาะคนที่ทดสอบบนเครื่องนี้ ถ้า deploy ต้องใช้โดเมน HTTPS ของเว็บจริง
- รันด้วย `compose.yaml` อย่างเดียวเพื่อไม่ให้ Mailpit override SMTP จริง:

```powershell
docker compose -p kku-foodshare-phase1 up --build -d
docker compose -p kku-foodshare-phase1 ps
```

จากนั้นทดสอบเหมือนขั้นตอนที่ 2 แต่เปิดอีเมลจริงของผู้รับแทน Mailpit ตรวจ Spam ด้วย หากไม่เข้า อย่ารายงานว่าใช้ได้จนกว่าจะได้รับอีเมลและเปลี่ยนรหัสสำเร็จ

อ้างอิง: https://support.google.com/accounts/answer/185833 และ https://mailpit.axllent.org/docs/install/docker/

## รายการไฟล์ที่เปลี่ยน

- `code/src/main/resources/templates/home.html`
- `code/src/main/resources/static/css/app.css`
- `code/src/main/resources/static/js/app.js`
- `code/src/main/resources/templates/login.html`
- `code/src/main/resources/templates/forgot-password.html`
- `code/src/main/resources/templates/reset-password.html`
- `code/src/main/java/com/kku/foodshare/service/impl/PasswordResetServiceImpl.java`
- `code/src/main/java/com/kku/foodshare/controller/web/PasswordResetController.java`
- `code/src/test/java/com/kku/foodshare/service/PasswordResetProtectionTest.java`
- `code/src/test/java/com/kku/foodshare/WebPagesTest.java`
- `code/src/test/java/com/kku/foodshare/PasswordResetJourneyTest.java`
- `code/src/test/java/com/kku/foodshare/controller/web/DashboardControllerTest.java` (ปรับ test ให้ใช้ authentication ที่ล็อกอินแล้วจริง)
- `code/src/test/js/guest-home.test.mjs`
- `compose.mail-local.yaml`
- notes ฉบับนี้

## ตรวจสอบก่อนส่งมอบ

จากโฟลเดอร์ `code`:

```powershell
.\mvnw.cmd -o -B test
node --check src/main/resources/static/js/app.js
node --test src/test/js/*.test.mjs
.\mvnw.cmd -o -B -DskipTests package
```

ถ้า Maven ไม่มี dependency ใน cache ให้รันโดยเอา `-o` ออกครั้งแรก เครื่องต้องต่ออินเทอร์เน็ต
JavaScript syntax และ Node tests ผ่านครบ 10 ไฟล์ในเครื่องแพ็ก Maven test/package ยังตรวจไม่สำเร็จเพราะ offline cache ไม่มี Spring Boot parent และการดาวน์โหลดติด DNS; ไม่ได้ยืนยันการส่ง SMTP จริงเพราะไม่ได้ใช้ credentials ของผู้ส่ง
