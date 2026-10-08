# Phase 11 — รูปอาหารถาวร อีเมลลืมรหัสผ่าน และเตือนเวลารับอาหาร

ต่อจาก Phase 10.1 โดยเก็บแผนที่ ธีม แอดมิน QR Scanner การจอง และ Owner Management เดิมไว้

## สิ่งที่เพิ่ม

1. รูปอาหารเลือกเก็บบน Cloudinary ได้ เมื่อเปิดใช้ใน Render รูปใหม่จะอยู่ภายนอกเครื่องแอปและไม่ถูกล้างพร้อม filesystem ของ Render
2. คู่มือสมัคร Brevo และตั้งค่าอีเมลลืมรหัสผ่าน พร้อม regression test ที่ผ่าน Password Reset จริงและส่งผ่าน HTTP adapter ของ Brevo ไปยังบริการจำลองในเครื่อง
3. เตือนการจองที่ยังรอรับก่อนหมดเวลารับ 30 นาที ขึ้นในกระดิ่งและหมวดการจองเดิม พร้อมชื่ออาหาร จำนวน จุดรับ และเวลาสิ้นสุด
4. ปุ่ม “ดูการจอง” เปิดบัตรจองที่เกี่ยวข้องโดยตรง แม้รายการนั้นไม่ได้อยู่หน้าแรกของประวัติการจอง ยังแสดง QR และรหัส 6 หลักเดิม
5. กระดิ่งอัปเดตจำนวนแจ้งเตือนทุก 60 วินาทีเมื่อเปิดเว็บอยู่ และตรวจอีกครั้งเมื่อกลับมาเปิดแท็บ

ไม่มี migration ใหม่ ไม่เปลี่ยน schema ไม่แก้ยอดอาหารหรือสถานะการจอง ไม่เปลี่ยน SecurityConfig ไม่เปลี่ยนพอร์ต/volume/ชื่อ Compose project

**งานนี้เตรียมโค้ดและคู่มือ ยังไม่ได้ตั้งค่าบัญชี Cloudinary/Brevo หรือ Deploy ลง Render ให้จริง** ต้องตั้งค่าบัญชีตามด้านล่างก่อนใช้งานบริการเหล่านั้น

## รันในเครื่อง Windows ตามเดิม

แตก ZIP ใน `Principle` แล้วเข้าโฟลเดอร์ `kku-foodshare` ภายใน โครงสร้าง ZIP มี `kku-foodshare/` เพียงชั้นเดียว

```powershell
cd "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\Principle\KKU-FoodShare-Phase11-Storage-Email-Reminders\kku-foodshare"

if (!(Test-Path .\.env)) {
    Copy-Item "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\Principle\kku-foodshare\.env" ".\.env"
}

$env:APP_PORT = "8081"
$env:APP_BASE_URL = "http://127.0.0.1:8081"
docker compose -p kku-foodshare-phase1 up --build -d
docker compose -p kku-foodshare-phase1 ps
```

เปิด http://127.0.0.1:8081 หรือ http://localhost:8081 โดยใช้ชื่อโปรเจกต์และ `.env` เดิมเพื่อให้ต่อกับ volume ฐานข้อมูลเดิม อย่าลบ volume ฐานข้อมูล

ค่าเริ่มต้น `IMAGE_STORAGE_PROVIDER=local` จึงรันได้โดยยังไม่ต้องสมัครบริการเก็บรูป ในเครื่องยังเก็บรูปลง `food_images` volume เดิม

ถ้าแตกไฟล์ไว้ชื่ออื่น เช่นมี `(1)` ให้เปลี่ยนเฉพาะ path ใน `cd` และคงเครื่องหมายคำพูดไว้ ตรวจว่าอยู่ถูกที่ด้วย `Test-Path .\compose.yaml` ซึ่งต้องเป็น `True`

## ทดสอบลืมรหัสผ่านในเครื่องก่อนสมัคร Brevo

จากโฟลเดอร์เดียวกัน หลัง copy `.env` และตั้ง `APP_PORT` ตามด้านบน:

```powershell
docker compose -p kku-foodshare-phase1 -f compose.yaml -f compose.mail-test.yaml up --build -d
docker compose -p kku-foodshare-phase1 -f compose.yaml -f compose.mail-test.yaml ps
```

- เว็บ: http://127.0.0.1:8081
- กล่องรับเมลทดสอบ Mailpit: http://127.0.0.1:8025
- ใช้อีเมลของบัญชีที่สมัครในฐานข้อมูลเครื่องนี้ บัญชีที่สมัครบนเว็บ Render อยู่ใน Neon ซึ่งเป็นอีกฐานข้อมูลหนึ่ง
- ขอรีเซ็ตรหัส เปิดเมลใน Mailpit กดลิงก์ ตั้งรหัสใหม่ แล้วลองเข้าสู่ระบบ
- Mailpit รับเมลในเครื่อง ไม่ส่งเข้า Gmail/Outlook การใช้ overlay นี้ไม่ต้องใส่ Brevo API key และไม่แก้ `.env`

## ตั้งรูปอาหารถาวรบน Render ด้วย Cloudinary

1. สมัครที่ https://cloudinary.com/users/register/free และเลือก Free plan
2. เข้าหน้า Console จด **Cloud name** ของ product environment ที่จะใช้
3. เข้า **Settings → API Keys** เพื่อดู **API key** และ **API secret** เก็บใน Render เท่านั้น
4. เข้า Render → Web Service `kku-foodshare` → Environment เพิ่มค่าต่อไปนี้:

| Key | Value |
| --- | --- |
| `IMAGE_STORAGE_PROVIDER` | `cloudinary` |
| `CLOUDINARY_CLOUD_NAME` | Cloud name ของบัญชีคุณ |
| `CLOUDINARY_API_KEY` | API key ของบัญชีคุณ |
| `CLOUDINARY_API_SECRET` | API secret ของบัญชีคุณ |

ใส่ค่าจริงในช่อง Value ไม่ใส่คำอธิบายหรือวงเล็บ และไม่ส่ง key/secret มาในแชท

ไม่ต้องสร้าง unsigned upload preset แอปอัปโหลดผ่าน backend โดยลงลายเซ็น SHA-256 และเรียก Cloudinary ผ่าน HTTPS

รูปยังใช้ JPG/PNG ไม่เกิน 5 MB ตรวจเนื้อไฟล์และขนาดภาพตามกฎเดิม จากนั้นย่อด้านยาวไม่เกิน 1400 px และบันทึกเป็น JPG ก่อนอัปโหลด เพื่อใช้พื้นที่/แบนด์วิดท์น้อยลง

ชื่อไฟล์ Cloudinary มี `cld-` นำหน้าและเก็บในช่อง filename เดิม รูปเก่าที่เป็น local filename ยังอ่านจากที่เก็บเดิมได้ ส่วน `/media/cld-...jpg` จะ redirect ไปยัง CDN จึงไม่ต้องโหลดไฟล์ผ่านแอปซ้ำ

ถ้าตั้ง `cloudinary` แต่ไม่ใส่ credentials หรือบริการอัปโหลดไม่สำเร็จ ระบบจะแจ้งว่าบันทึกรูปไม่สำเร็จ แทนการบันทึกลงพื้นที่ชั่วคราวแล้วแสดงว่าสำเร็จ

ค่า `IMAGE_STORAGE_PROVIDER` ต้องเป็น `local` หรือ `cloudinary` เท่านั้น หากพิมพ์ผิดแอปจะไม่เริ่มทำงานและแสดงข้อความตั้งค่าใน log เพื่อป้องกันรูปถูกเก็บในพื้นที่ชั่วคราวโดยไม่รู้ตัว

**รูปเดิมที่หายไปแล้วบน Render ไม่สามารถกู้จากฐานข้อมูลได้ เพราะฐานข้อมูลเก็บชื่อไฟล์ ไม่ได้เก็บไฟล์รูปอาหาร** หลังตั้ง Cloudinary ให้เพิ่มรูปกลับผ่านหน้าแก้ไขโพสต์เดิม เพื่อรักษาประวัติและการจอง ไม่ต้องลบหรือสร้างโพสต์ใหม่ รูปโปรไฟล์เก็บอยู่ในฐานข้อมูลตามระบบเดิม

Cloudinary Free ณ วันที่ตรวจเอกสาร 8 ต.ค. 2026 มี 25 credits/เดือน ใช้ร่วมกันสำหรับ storage, bandwidth และ transformations ไม่ต้องเพิ่มบัตรเพื่อสมัคร Free ตรวจยอด Usage ใน Console และไม่เลือกอัปเกรดถ้าต้องการใช้งานฟรี

## สมัคร Brevo สำหรับลืมรหัสผ่านบนเว็บจริง

1. สมัครที่ https://www.brevo.com/ เลือก Free plan แล้วยืนยันอีเมลสมัครบัญชี
2. หากมีแบบฟอร์มธุรกิจ/การใช้งาน ให้ระบุว่าใช้ส่ง transactional password-reset emails ของเว็บไซต์ KKU FoodShare ไม่ต้องซื้อแพ็กเกจหรือเครดิต SMS
3. เข้า **Settings → Senders, Domains & Dedicated IPs → Senders → Add a sender**
4. ใส่ชื่อผู้ส่ง `KKU FoodShare` และอีเมลที่คุณเปิดกล่องรับได้ ยืนยันตามลิงก์/รหัสที่ Brevo ส่ง หากใช้โดเมนของตนเอง ให้ทำ domain authentication ตามที่ Brevo แสดง
5. เข้า **Settings → SMTP & API → API Keys & MCP** แล้วสร้าง API key ใหม่ ใช้ API key สำหรับ HTTPS API ไม่ใช่ SMTP key
6. เข้า Render → Web Service → Environment:

| Key | Value |
| --- | --- |
| `MAIL_ENABLED` | `true` |
| `MAIL_PROVIDER` | `brevo` |
| `BREVO_API_KEY` | API key จริง เก็บใน Render เท่านั้น |
| `MAIL_FROM` | อีเมลผู้ส่งที่ยืนยันใน Brevo แล้ว |
| `APP_BASE_URL` | `https://kku-foodshare.onrender.com` |

ไม่ต้องเพิ่มโปรไฟล์ `mail` เพื่อใช้ Brevo และไม่ต้องตั้งค่า SMTP สำหรับวิธีนี้ คงค่าฐานข้อมูล Neon, `APP_SECRET` และ Google login ที่มีอยู่

Brevo Free ณ วันที่ตรวจเอกสารมี 300 emails/วัน การอนุมัติบัญชีผู้ส่งและการเข้ากล่อง inbox ขึ้นกับ Brevo/ผู้รับ ทดสอบการส่งจริงหลังยืนยัน sender ไม่ถือว่าการบันทึก API key เป็นหลักฐานว่าเมลส่งถึงแล้ว

โค้ดใช้ HTTPS API เพราะ Render Free จำกัด outbound SMTP ports 25/465/587

## นำโค้ดใหม่ขึ้น Render

คัดลอกไฟล์โปรเจกต์จาก ZIP ไปยัง checkout Git ที่ Render ใช้อยู่ โดยเก็บ `.git` และ `.env` เดิมไว้ อย่าเอาโฟลเดอร์ `kku-foodshare` ไปซ้อนอีกชั้นหนึ่ง

```powershell
cd "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\Principle\kku-foodshare"
git status -sb
git check-ignore -v .env
```

ตรวจ diff และทดสอบก่อน commit/push บน branch `sarocha_6733802967_01` ที่ใช้อยู่ แล้วให้ Render Deploy commit ใหม่ บันทึก Environment ของ Cloudinary/Brevo ให้ครบก่อนลองอัปโหลดและส่งอีเมลจริง ไม่เปลี่ยนค่าฐานข้อมูลเพื่อใช้ฟีเจอร์นี้

### ตรวจบนเว็บจริง

1. อัปโหลดรูปในโพสต์ที่คุณเป็นเจ้าของ เปิดรูปได้ และตรวจพบ asset ใน Cloudinary จากนั้น Redeploy และตรวจรูปโพสต์เดิมอีกครั้ง
2. ใช้บัญชีที่สมัครบนเว็บจริงขอลืมรหัสผ่าน ตรวจ Inbox/Spam และ Brevo Transactional Logs ลิงก์ต้องเริ่มด้วย `https://kku-foodshare.onrender.com/reset-password`
3. ตั้งรหัสใหม่ ตรวจว่ารหัสเดิมเข้าไม่ได้ รหัสใหม่เข้าได้ และลิงก์ใช้ซ้ำไม่ได้
4. สร้างโพสต์ทดสอบที่รับได้อีก 15–30 นาที จองด้วยอีกบัญชี เปิดเว็บ/กระดิ่ง จะมีการเตือนในหมวดการจอง กดดูการจองแล้วเห็นบัตรและ QR ที่ถูกต้อง
5. เปิดหลายแท็บแล้วตรวจว่าแต่ละการจองมีการเตือนใกล้หมดเวลาเพียงครั้งเดียว

## ขอบเขตการเตือน

การเตือนนี้เป็น **การแจ้งเตือนภายในเว็บ** ใช้กระดิ่งเดิม ไม่ใช่ push notification ของระบบโทรศัพท์

- ตรวจเฉพาะ `RESERVED` และโพสต์ที่ยังไม่ปิด/หมดเวลา บัญชีผู้รับและผู้แบ่งปันยัง active
- แม้จำนวนที่คนอื่นจองได้จะเป็นศูนย์ ผู้ที่จองไว้แล้วก็ยังได้รับการเตือน
- ยกเลิก รับอาหารสำเร็จ หมดเวลา หรือปิดโพสต์แล้ว จะไม่สร้างการเตือนใหม่
- ขณะเว็บทำงาน job ตรวจทุก 60 วินาที ครั้งละไม่เกิน 100 รายการ และการอ่านกระดิ่ง/กล่องแจ้งเตือนตรวจของผู้ใช้คนนั้นอีกครั้ง
- Render Free พักแอปเมื่อไม่มีผู้ใช้งาน ถ้าแอปพัก job ไม่ได้ทำงาน เมื่อผู้ใช้กลับมาระหว่าง 30 นาทีสุดท้าย ระบบตรวจให้ใหม่ ถ้ากลับมาหลังหมดเวลาแล้วจะไม่สร้างการเตือนย้อนหลัง
- ใช้เวลาจุดรับตาม Clock `Asia/Bangkok` เดิม
- ใช้ dedupe key ต่อการจองและล็อก reservation row เฉพาะช่วงตรวจ/บันทึกการเตือน ป้องกันการสร้างซ้ำจาก scheduler กับ inbox ไม่แก้ stock หรือ state transition เดิม

## ไฟล์ที่แก้ใน Phase 11

- `code/src/main/java/com/kku/foodshare/service/storage/ImageStorage.java`
- `code/src/main/java/com/kku/foodshare/config/ImageStorageConfiguration.java` (ใหม่)
- `code/src/main/java/com/kku/foodshare/service/storage/CloudinaryImageStorage.java` (ใหม่)
- `code/src/main/java/com/kku/foodshare/controller/web/MediaController.java`
- `code/src/main/java/com/kku/foodshare/repository/ReservationRepository.java`
- `code/src/main/java/com/kku/foodshare/service/impl/NotificationServiceImpl.java`
- `code/src/main/java/com/kku/foodshare/service/impl/PickupReminderService.java` (ใหม่)
- `code/src/main/java/com/kku/foodshare/service/impl/PickupReminderJob.java` (ใหม่)
- `code/src/main/resources/application.properties`
- `code/src/main/resources/static/js/app.js`
- `code/src/main/resources/static/js/notification-badge.mjs` (ใหม่)
- `code/src/main/resources/static/css/app.css`
- `compose.yaml`, `docker-compose.yml`, `.env.example`
- `code/src/test/java/com/kku/foodshare/service/storage/CloudinaryImageStorageTest.java` (ใหม่)
- `code/src/test/java/com/kku/foodshare/service/storage/ImageStorageConfigurationTest.java` (ใหม่)
- `code/src/test/java/com/kku/foodshare/service/PickupReminderIntegrationTest.java` (ใหม่)
- `code/src/test/java/com/kku/foodshare/PasswordResetBrevoJourneyTest.java` (ใหม่)
- `code/src/test/java/com/kku/foodshare/FoodJourneyTest.java` (เทสต์ขยายเวลาเปรียบเทียบค่าเวลาแทนข้อความ ป้องกันล้มจากเศษวินาทีรูปแบบ `.120`/`.12` โดยไม่เปลี่ยนโค้ดการขยายเวลา)
- `test/pickup-reminder-check.cjs` (ใหม่)
- เอกสารแผนและบันทึก Phase 11

การแก้จาก Phase 10/10.1 รวมอยู่ใน ZIP นี้ด้วย อ่านรายชื่อของรอบก่อนใน `PHASE10-ADMIN-THEME-RESET-NOTES.md` และ `PHASE10-1-POLISH-NOTES.md`

## ทดสอบโค้ด

```powershell
cd .\code
.\mvnw.cmd test
cd ..
node --test code/src/test/js/*.test.mjs
```

ทดสอบ browser checks หลังติดตั้ง Playwright:

```powershell
cd .\test
npm install
npx playwright install chromium
cd ..
$env:NODE_PATH = (Resolve-Path .\test\node_modules).Path
node test/pickup-reminder-check.cjs
node test/admin-posts-check.cjs
node test/dark-surfaces-check.cjs
$env:RENDERED_TEMPLATES_DIR = "code/target/ui-rendered"
node test/phase10-ui-check.cjs
```

การทดสอบ provider ใช้ HTTP/SMTP fixture ในเครื่อง ไม่ใช้ข้อมูลจริงของผู้ใช้หรือส่งเมลออกไปภายนอก ยังไม่มี Docker daemon ในสภาพแวดล้อมที่แก้โค้ด จึงตรวจ Java package/browser/tests ที่นี่และให้คำสั่ง Docker สำหรับเครื่องผู้ใช้ด้านบน

## ผลตรวจของไฟล์ส่งมอบ

- Java: 107 tests ผ่านทั้งหมด ไม่มี failure/error/skipped
- JavaScript: 117 tests ผ่านทั้งหมด
- Browser: การเตือน/เปิดบัตรพร้อม QR/กระดิ่ง/ข้อความที่ escape ผ่านบนความกว้าง 360, 390, 768 และ 1440 px
- Browser regression: หน้าแอดมินและ dark surfaces ผ่านบน 390/1440 px; template จริง ธีม light/dark/monochrome ช่องกรอง รูป และหน้า auth ผ่านบน 360/390/768/1440 px
- `node --check` สำหรับ JavaScript ที่แก้ และ `git diff --check` ผ่าน
- Maven `package` ผ่าน
- ตรวจโค้ด Phase 11 และแก้ข้อเสนอแนะเรื่องชื่อ image provider แล้ว ไม่มีประเด็นค้างในการตรวจขอบเขตนี้

ยังต้องตรวจบน Render หลังใส่ credentials จริง: Cloudinary รับรูป/ส่งภาพผ่าน CDN, Brevo ส่งถึงกล่องผู้รับ และ reminder ทำงานร่วมกับ PostgreSQL จริง การทดสอบ reminder อัตโนมัติรอบนี้ใช้ H2 ไม่ได้เชื่อมต่อ Neon

## เอกสารอ้างอิงทางการ

- Cloudinary Free/credits: https://cloudinary.com/pricing
- Cloudinary credentials: https://cloudinary.com/documentation/developer_onboarding_faq_find_credentials
- Cloudinary signatures: https://cloudinary.com/documentation/authentication_signatures
- Cloudinary upload/delete: https://cloudinary.com/documentation/image_upload_api_reference
- Brevo Free: https://help.brevo.com/hc/en-us/articles/208589409-About-Brevo-s-pricing-plans
- Brevo sender: https://help.brevo.com/hc/en-us/articles/208836149-Create-a-new-sender-From-name-and-From-email
- Brevo API keys: https://help.brevo.com/hc/en-us/articles/209467485-Create-and-manage-your-API-keys
- Render Free filesystem/SMTP/sleep: https://render.com/docs/free
