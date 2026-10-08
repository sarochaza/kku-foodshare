# KKU FoodShare Phase 10.1 — Theme, alignment, image and reset setup

ต่อจาก Phase 10 Admin/Theme/Reset โดยตรง รวมงานเดิมของ Phase 10 ไว้ครบ

## แก้ในรอบนี้

- กล่อง FAQ หน้าแรก: พื้นเทาเข้ม หัวข้อและคำตอบอ่านชัดเมื่อเลือกธีมดำ รวมสถานะเปิด/ปิดและ hover
- แถบตัวกรองหน้าแรก: “เรียงตาม”, dropdown, “รับได้ตอนนี้” และปุ่มแผนที่อยู่แนวเดียวกัน ปุ่มสูงเท่ากัน 54px บน PC และ 48px บนมือถือ
- แก้สาเหตุจาก CSS `label` ทั่วไปที่ใส่ margin ล่างและ flex แนวตั้ง โดยแก้เฉพาะตัวกรองของหน้าแรก
- ปุ่มแผนที่ใช้พื้นเทาเมื่อเลือกธีมดำ
- หน้าล็อกอิน สมัคร ลืมรหัส และตั้งรหัสใหม่: พื้นหลังฝั่งมาสคอตเป็นโทนเข้มในธีมดำ ตัวหนังสือ/คำอธิบาย/กล่องเล็กอ่านชัด เก็บสีภาพมาสคอตตามเดิม
- หน้าลืมรหัสและตั้งรหัสใช้ layout กระชับชุดเดียวกับหน้าล็อกอิน รองรับมือถือ
- ใช้ภาพ “มื้อแบ่งปันริมสระในรั้วมหาวิทยาลัย.png” ที่ผู้ใช้แนบ ในส่วน “มีอาหารเหลืออยู่ไหม? ส่งต่อความอร่อยกันเถอะ” เก็บไฟล์ภาพตามต้นฉบับและแสดงสัดส่วน 3:2 เพื่อไม่ตัดใบหน้า
- Compose ทั้งสองชื่อส่ง `MAIL_ENABLED`, `MAIL_PROVIDER`, `BREVO_API_KEY` เข้า container แล้ว เดิมใส่ใน `.env` อย่างเดียวค่ายังไม่ถึงแอป
- เพิ่ม Compose overlay สำหรับกล่องอีเมลทดสอบ Mailpit เป็นทางเลือกในเครื่อง โดยไม่เปลี่ยนฐานข้อมูล/volume/ชื่อ project

ไม่เปลี่ยนระบบจอง จำนวนสินค้า แผนที่/เส้นทาง QR สิทธิ์ล็อกอิน token รีเซ็ตรหัส หรือ schema ฐานข้อมูล งานแอดมินจาก Phase 10 ยังอยู่ครบ

## ทำไมลืมรหัสผ่านในภาพยังใช้ไม่ได้

ข้อความ “ระบบส่งอีเมลยังไม่เปิดใช้งาน” หมายถึงแอปกำลังใช้ `MAIL_ENABLED=false` ไม่มีบริการส่งอีเมลเปิดอยู่ การแก้หน้าเว็บอย่างเดียวจึงส่งอีเมลไม่ได้

เลือกใช้อย่างใดอย่างหนึ่ง:

1. ทดสอบ/เดโมในเครื่อง: ใช้ Mailpit ตามคำสั่งด้านล่าง อ่านอีเมลที่ `http://127.0.0.1:8025` ไม่ส่งเข้า Gmail จริง
2. เว็บ Render: สมัคร Brevo ยืนยันผู้ส่ง และใส่ API key ใน Render จึงจะส่งเข้ากล่องอีเมลจริงได้

ยังไม่มี API key/ผู้ส่งของผู้ใช้ จึงยังไม่ได้ทดสอบส่งอีเมลจริงผ่านบัญชี Brevo บน Render การทดสอบ SMTP ในเครื่องและคำขอ HTTP ของตัวส่ง Brevo ผ่านแล้ว

## เตรียมและรันใน PowerShell

แตก ZIP แล้วเข้าโฟลเดอร์ย่อย `kku-foodshare` ที่มี `Dockerfile` และ `compose.yaml`:

```powershell
cd "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\Principle\KKU-FoodShare-Phase10-1-Theme-Email-Polish\kku-foodshare"
if (!(Test-Path .\.env)) {
    Copy-Item "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\Principle\kku-foodshare\.env" ".\.env"
}
$env:APP_PORT = "8081"
```

หากแตก ZIP ไปที่อื่น ให้เปลี่ยนเฉพาะพาธ `cd` ให้ตรงโฟลเดอร์จริง เก็บ `.env` เดิมไว้ ZIP ไม่รวม `.env`

รันเว็บตามปกติ:

```powershell
docker compose -p kku-foodshare-phase1 up --build -d
docker compose -p kku-foodshare-phase1 ps
```

เปิด `http://127.0.0.1:8081` หรือ `http://localhost:8081`

### ทดสอบลืมรหัสผ่านในเครื่องทันที

ใช้คำสั่งนี้แทนการรันปกติเมื่อจะทดสอบอีเมล:

```powershell
docker compose -p kku-foodshare-phase1 -f compose.yaml -f compose.mail-test.yaml up --build -d
docker compose -p kku-foodshare-phase1 -f compose.yaml -f compose.mail-test.yaml ps
```

1. เปิด `http://127.0.0.1:8081/forgot-password`
2. กรอกอีเมลของบัญชีที่สมัครอยู่ในฐานข้อมูลของเว็บในเครื่อง
3. เปิดกล่องอีเมลทดสอบ `http://127.0.0.1:8025`
4. เปิดอีเมล KKU FoodShare แล้วกดลิงก์ตั้งรหัสใหม่
5. ตั้งรหัสใหม่และกลับไปล็อกอิน รหัสเดิมจะใช้ไม่ได้

overlay ใช้ port ที่เผยแพร่ของแอปสร้างลิงก์ ไม่ส่งผู้ใช้ไปพอร์ตภายใน container และไม่เปลี่ยน `.env` เดิม ขอลิงก์ล่าสุดเพียงครั้งเดียวแล้วตรวจกล่องจดหมาย: ระบบเดิมจำกัดการขอซ้ำ 5 นาที และลิงก์ใช้ได้ 15 นาที/ครั้งเดียว

บัญชีใน Neon กับฐานข้อมูล Docker ในเครื่องเป็นคนละฐานข้อมูล ต้องใช้บัญชีที่มีอยู่ในฐานข้อมูลที่กำลังทดสอบ

กลับไปรันปกติด้วยคำสั่งปกติด้านบนได้ ไม่ใช้ `down -v` เพราะจะลบ volume ของข้อมูลเดิม

## เปิดส่งอีเมลจริงบน Render

Render Free ปิดการส่งออกผ่านพอร์ต SMTP ทั่วไป จึงใช้ Brevo HTTPS API ที่เพิ่มไว้ใน Phase 10

1. เข้า https://www.brevo.com/ สมัครบัญชีและเลือกแผน Free ให้บัญชีพร้อมใช้ transactional email ตามขั้นตอนที่ Brevo แสดง
2. ใน Brevo เข้า Settings → Senders, Domains & Dedicated IPs → Senders → Add a sender ใส่ชื่อ `KKU FoodShare` กับอีเมลผู้ส่งที่คุณเข้าถึงได้ แล้วทำการยืนยันที่ Brevo ขอ ถ้าใช้โดเมนของตัวเองให้ทำการยืนยันโดเมนด้วย
3. เข้า Settings → SMTP & API → API Keys & MCP → Generate a new API key สร้าง **API key** สำหรับเว็บนี้ เก็บคีย์ส่วนตัว อย่าใช้ SMTP key แทน
4. ใน Render เปิดบริการ `kku-foodshare` → Environment ตั้งค่าด้านล่าง โดยแทนคำอธิบายด้วยค่าจริง ไม่ใส่เครื่องหมาย `< >` ไม่ใส่เครื่องหมาย quote รอบค่าในช่อง Render:

| Variable | Value |
| --- | --- |
| `MAIL_ENABLED` | `true` |
| `MAIL_PROVIDER` | `brevo` |
| `BREVO_API_KEY` | API key ที่สร้างจาก Brevo |
| `MAIL_FROM` | อีเมลผู้ส่งที่ยืนยันกับ Brevo แล้ว |
| `APP_BASE_URL` | `https://kku-foodshare.onrender.com` |

ไม่ต้องใส่ SMTP host/port/password สำหรับ Brevo ไม่ต้องเปิด Spring profile `mail` เพื่อใช้ Brevo คงค่าฐานข้อมูล Neon, APP_SECRET และค่าตั้งค่าอื่นที่ใช้งานได้ไว้ตามเดิม

5. นำโค้ดรุ่นนี้เข้า Git repository เดิม/branch ที่ Render เชื่อมต่อ เก็บ `.git` และ `.env` เดิม Commit และ Push จากนั้น Save/Rebuild/Deploy ให้ Render ใช้ทั้งโค้ดใหม่และค่าตั้งค่าใหม่ การ Build Docker บนเครื่องไม่ได้อัปเดตเว็บ Render
6. บนเว็บจริงเปิด `/forgot-password` ใช้อีเมลที่สมัครอยู่ในเว็บจริง ตรวจ inbox/spam และตรวจ transactional logs ใน Brevo หากอีเมลไม่เข้า (เช่น บัญชียังไม่อนุมัติ ผู้ส่งไม่ยืนยัน หรือคีย์ไม่ถูกต้อง)
7. ลิงก์ในอีเมลต้องขึ้นต้นด้วย `https://kku-foodshare.onrender.com/reset-password` ต้องไม่เป็น `localhost` / `127.0.0.1`

ไม่ต้องส่ง API key, `.env` หรือรหัสฐานข้อมูลในแชต และไม่เพิ่มลง Git/ZIP

## ไฟล์ที่เปลี่ยนจาก Phase 10

- `code/src/main/resources/static/css/app.css`
- `code/src/main/resources/static/css/theme.css`
- `code/src/main/resources/static/css/welcome.css`
- `code/src/main/resources/templates/home.html`
- `code/src/main/resources/templates/forgot-password.html`
- `code/src/main/resources/templates/reset-password.html`
- `code/src/main/resources/static/images/foodshare-share-banner.png` (ใหม่)
- `compose.yaml`, `docker-compose.yml` (เพิ่มเฉพาะการส่งค่าตั้งค่าอีเมล 3 ค่า)
- `compose.mail-test.yaml` (ใหม่/ใช้เฉพาะเมื่อเลือกทดสอบในเครื่อง)
- `code/src/test/java/com/kku/foodshare/MailDeploymentConfigTest.java` (ใหม่)
- `code/src/test/java/com/kku/foodshare/PasswordResetSmtpDeliveryTest.java` (ใหม่)
- `test/phase10-ui-check.cjs` (ใหม่)
- `PHASE10-1-POLISH-NOTES.md` (ไฟล์นี้)

ไฟล์และระบบแอดมินที่เพิ่มใน Phase 10 อยู่ใน ZIP นี้ด้วย รายการเดิมอยู่ใน `PHASE10-ADMIN-THEME-RESET-NOTES.md`

## การตรวจ

ผลล่าสุด: Java 86 รายการผ่าน, JavaScript 117 รายการผ่าน, UI จาก Thymeleaf ที่เรนเดอร์จริงผ่านทั้ง 4 ขนาด, regression หน้าแอดมินผ่าน และ Maven package สำเร็จ

- Java: `cd code` แล้ว `./mvnw test` (Windows ใช้ `.\mvnw.cmd test`)
- JavaScript: จากโฟลเดอร์ `kku-foodshare` ใช้ `node --test code/src/test/js/*.test.mjs`
- UI: `node test/phase10-ui-check.cjs` ใช้ Playwright/Chromium ตรวจ CSS และ template จริงที่ 360/390/768/1440px รวมธีมปกติ ดำ และขาวดำ
- หลัง Java tests สามารถใช้ view ที่ Thymeleaf เรนเดอร์แล้วด้วย `RENDERED_TEMPLATES_DIR=code/target/ui-rendered node test/phase10-ui-check.cjs` (Bash) หรือกำหนด `$env:RENDERED_TEMPLATES_DIR="code/target/ui-rendered"` ก่อนคำสั่ง Node ใน PowerShell
- SMTP test ส่งผ่าน JavaMailSender จริงไปยัง SMTP capture ในเครื่อง → อ่านลิงก์จากอีเมล → ตั้งรหัสใหม่ → ตรวจว่ารหัสเดิมล็อกอินไม่ได้/รหัสใหม่ล็อกอินได้ ฐานข้อมูลทดสอบเป็น H2 ไม่แตะ Neon หรือ Docker volume
- Regression ของแอดมินและพื้นหลังเทาเดิม: `node test/admin-posts-check.cjs` และ `node test/dark-surfaces-check.cjs`
- สภาพแวดล้อมทำงานนี้ไม่มี Docker daemon จึงไม่ได้รัน `docker compose up` ที่นี่ มีการตรวจ YAML/ค่าตั้งค่าและทดสอบแอปผ่าน Maven แล้ว ให้รัน Docker ตามคำสั่งข้างบนในเครื่องผู้ใช้

หากยังไม่มี Playwright สำหรับการตรวจ UI ให้ติดตั้งจากโฟลเดอร์ `test` ก่อน:

```powershell
cd test
npm install
npx playwright install chromium
cd ..
```

เอกสารผู้ให้บริการ:

- https://render.com/docs/free
- https://help.brevo.com/hc/en-us/articles/208836149-Create-a-new-sender-From-name-and-From-email
- https://help.brevo.com/hc/en-us/articles/209467485-Create-and-manage-your-API-keys
