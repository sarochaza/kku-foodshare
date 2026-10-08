# Phase 11.1 — คำแนะนำหน้าสุดและการดูครั้งแรกแยกตามบัญชี

ทำต่อจาก Phase 11 ที่ส่งมอบแล้ว คงคำแนะนำทั้ง 6 ขั้นตอนและข้อความเดิมทั้งหมด ไม่มีการแก้ Reservation, Stock, QR Scanner, Maps, SecurityConfig หรือ Deployment configuration ในรอบนี้ และไม่ได้เปลี่ยนเว็บ Render หรือฐานข้อมูล Neon ที่ใช้งานอยู่

## สิ่งที่เปลี่ยน

- กล่องคำแนะนำใช้ native modal dialog ซึ่งอยู่ใน browser top layer เพื่อให้เมนู แผนที่ และชั้นแสดงผลปกติทับไม่ได้ พร้อมจำกัดความสูงและเลื่อนภายในกล่องได้บนโทรศัพท์
- บัญชีใหม่ที่สมัครหลังอัปเดต เห็นคำแนะนำอัตโนมัติเมื่อเข้าหน้าแรกหลังล็อกอินครั้งแรก รองรับหน้า `/home`, `/` และ `/explore` โดยไม่เปลี่ยนปลายทางล็อกอินเดิม
- บันทึกสถานะตามบัญชีบนเซิร์ฟเวอร์ ไม่ใช้สถานะร่วมกันใน localStorage ของเบราว์เซอร์อีกต่อไป บัญชีใหม่คนละบัญชีจึงมีสถานะการดูแยกกัน และบัญชีที่ดูจบแล้วจะไม่แสดงซ้ำอัตโนมัติเมื่อเปลี่ยนเบราว์เซอร์
- เมื่อกดเสร็จสิ้น ข้าม หรือ Escape จะบันทึกว่าได้ดูคำแนะนำแล้ว การเปิดหน้าและกดถัดไป/ย้อนกลับยังไม่ถือว่าดูจบ ถ้าปิดหน้าไปก่อนจะยังแสดงอีกครั้ง
- ถ้าบันทึกสถานะไม่ได้ กล่องยังปิดได้ เว็บยังใช้งานต่อได้ และแจ้งว่าอาจแสดงคำแนะนำอีกในครั้งหน้า
- ปุ่มเปิดคำแนะนำซ้ำในโปรไฟล์และ `/?guide=1` ยังใช้งานได้ตามเดิม ผู้เยี่ยมชมที่ยังไม่ล็อกอินไม่เห็นอัตโนมัติ
- บัญชีเดิมก่อนอัปเดตไม่ถูกบังคับให้ดูใหม่ แต่เปิดซ้ำเองได้

## ข้อมูลและ API

เพิ่ม Flyway `V10__account_onboarding.sql` เพียงหนึ่งช่อง `users.onboarding_completed` โดยบัญชีเดิมเป็น TRUE และบัญชีใหม่เป็น FALSE ใช้ Java default FALSE ให้ตรงกัน ไม่มีการลบข้อมูล

เพิ่ม `POST /api/v1/me/onboarding` ใน controller โปรไฟล์เดิม ใช้ตัวตนผู้ล็อกอินและ CSRF เดิม ไม่มี user ID จากผู้ใช้ การบันทึกซ้ำไม่เพิ่ม version ซ้ำ และการเปลี่ยนสถานะครั้งแรกเพิ่ม version เพื่อป้องกันการเขียนค่าทับจาก entity เก่า

Flyway จะรัน migration เมื่อเปิดแอปเวอร์ชันใหม่ตามกระบวนการเดิม รอบนี้ยังไม่ได้รัน migration บน Neon

## ไฟล์โค้ดที่แก้

- `code/src/main/java/com/kku/foodshare/domain/entity/User.java`
- `code/src/main/java/com/kku/foodshare/repository/UserRepository.java`
- `code/src/main/java/com/kku/foodshare/service/ProfileSettingsService.java`
- `code/src/main/java/com/kku/foodshare/service/impl/ProfileSettingsServiceImpl.java`
- `code/src/main/java/com/kku/foodshare/controller/api/ProfileSettingsController.java`
- `code/src/main/java/com/kku/foodshare/controller/web/PageAdvice.java`
- `code/src/main/resources/db/migration/V10__account_onboarding.sql`
- `code/src/main/resources/templates/fragments.html`, `home.html`, `dashboard.html`, `explore.html`
- `code/src/main/resources/static/css/app.css`
- `code/src/main/resources/static/js/app.js`, `onboarding.mjs`

Tests เพิ่ม `OnboardingJourneyTest.java`, `onboarding.test.mjs`, `test/onboarding-check.cjs` และปรับ assertion ที่อ้างโค้ด localStorage เก่าใน `guest-home.test.mjs`

ย้ายเฉพาะเอกสาร `PHASE*-NOTES.md` และ `QUICK-PREVIEW-NOTES.md` ไปรวมใน `docs/phase-notes/` พร้อมสำรอง README เดิมและแก้ลิงก์ README ไม่ได้ย้ายไฟล์แอป `.env` หรือ Compose

## แก้ Build error ของ test (8 ต.ค. 2026)

แก้เฉพาะ `OnboardingJourneyTest.java` ซึ่งเรียก `User.getVersion()` ที่ไม่มีใน entity เดิมสองจุด เปลี่ยนเป็น `JdbcTemplate.queryForObject` อ่าน `users.version` จากฐานข้อมูลทดสอบโดยตรง เพื่อคงการตรวจว่าการบันทึกซ้ำไม่เพิ่ม version ไม่มีการแก้ production code, Dockerfile, Compose หรือ migration ใน hotfix นี้

ยืนยันว่าโค้ดอ่านค่าเดิม compile ไม่ผ่านจากเมธอดที่ไม่มี และ expression ใหม่ compile ผ่านกับ User/JdbcTemplate จริงแล้ว แต่ยังไม่ได้รัน Maven testCompile หรือ integration test ทั้งไฟล์ เพราะเครื่องทดสอบยังไม่มี test dependencies ดังที่ระบุด้านล่าง ให้ Build ใน Docker ตามคำสั่งเพื่อยืนยันทั้งโปรเจกต์

## แก้หน้าเว็บเบลอขณะดูคำแนะนำ (8 ต.ค. 2026)

ภาพที่ผู้ใช้ส่งมามีกล่องคำแนะนำชัดเจน แต่ช่องค้นหา ตัวกรอง และพื้นที่ที่กำลังแนะนำด้านหลังเบลอ ต้นเหตุคือ `.home-guide::backdrop` เปลี่ยนเฉพาะ background แต่ยังรับ `backdrop-filter: blur(4px)` จาก `dialog::backdrop` กลาง

แก้เฉพาะ CSS ของคำแนะนำ: ปิด `backdrop-filter` และ `-webkit-backdrop-filter` ของ backdrop นี้ และลดความทึบของ `.guide-shade` เป็น 16% ให้หน้าเว็บและกรอบไฮไลต์มองเห็นชัด ยังคงกล่องคำแนะนำใน browser top layer ข้อความเดิมและสถานะบัญชีเดิม ไม่มีการแก้ระบบบัญชี ฐานข้อมูล หรือ modal อื่น

เพิ่ม browser regression assertions ว่า backdrop ไม่มี blur และ shade ไม่ทึบเกิน 25% ที่ 360/390/768/1440px ตรวจ syntax script และ diff whitespace ผ่านแล้ว แต่ยังไม่ได้รัน browser journey จริงเพราะไม่มี Chromium executable ในเครื่องนี้ ไม่ได้รันทดสอบ Java ทั้งชุดซ้ำสำหรับการแก้ CSS นี้

หลัง Build ใหม่ให้กด Ctrl+F5 ที่หน้าเว็บเพื่อโหลด CSS ใหม่ หากบัญชีดูจบแล้วให้เปิดคำแนะนำซ้ำจากโปรไฟล์ หรือเปิด `/?guide=1` การทดสอบนี้ไม่ต้องสมัครบัญชีใหม่อีก

## ผลตรวจรอบนี้

- JavaScript regression suite: 123 tests ผ่านทั้งหมด รวม 6 tests ใหม่ของคำแนะนำ
- Java production files ที่แก้ 6 ไฟล์ compile สำเร็จด้วย dependency ของ JAR Phase 11 เดิม
- Hibernate ตรวจ User mapping และ HQL ของคำสั่งบันทึกสถานะจริงได้ โดยไม่เชื่อมฐานข้อมูลหรือ execute คำสั่ง
- ตรวจ syntax JavaScript และ diff whitespace สำเร็จ
- ตรวจโค้ดเฉพาะการเปลี่ยนรอบนี้แล้ว ไม่พบข้อแก้ไขจาก review
- Java integration tests ใหม่ยังไม่ได้รัน: Maven cache ของ Spring Boot parent และ test dependencies ไม่มีในสภาพแวดล้อมรอบนี้ และดาวน์โหลดไม่ได้
- Browser journey ใหม่ยังไม่ได้รัน: ไม่มี Chromium executable ในสภาพแวดล้อมรอบนี้ จึงยังไม่ยืนยันการแสดงผลจริงและการแตะที่ 360/390/768/1440px
- ผล Java 107 tests และ Build ที่ผ่านก่อนหน้านี้เป็นของ Phase 11 ก่อนแก้รอบนี้ ไม่ถือเป็นผลทดสอบของ Phase 11.1

Tests ที่เพิ่มครอบคลุม registration/login, แยกบัญชี, migration ของบัญชีเดิม/ใหม่, CSRF, การดูซ้ำ, ข้าม/จบ/Escape และบันทึกไม่สำเร็จ ส่วน browser journey ตรวจ native modal เหนือชั้น z-index สูง พร้อมตำแหน่งกล่องและการแตะหลายขนาดหน้าจอ

## รันบน Windows PowerShell

คัดลอกทีละบรรทัด ไม่ต้องคัดลอกข้อความ `PS ...>` จากหน้าจอ:

```powershell
cd "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\Principle\KKU-FoodShare-Phase11-1-Onboarding-Account-Fix\kku-foodshare"
if (!(Test-Path .\.env)) { Copy-Item "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\Principle\kku-foodshare\.env" ".\.env" }
docker compose -p kku-foodshare-phase1 up --build -d
docker compose -p kku-foodshare-phase1 ps
```

เปิด http://127.0.0.1:8081 โดยใช้ `.env` เดิมที่ตั้งพอร์ต 8081 และ Compose project เดิมเพื่อใช้ฐานข้อมูลเดิม ห้าม `down -v` และห้ามลบ volumes ZIP ไม่รวม `.env`

ตรวจโดยสมัครบัญชีใหม่ แล้วล็อกอิน กดคำแนะนำจบหรือข้าม ออกจากระบบและเข้าอีกครั้งต้องไม่แสดงอัตโนมัติ จากนั้นสมัครอีกบัญชีในเบราว์เซอร์เดียวกันต้องแสดงครั้งแรก เปิดดูซ้ำได้จากโปรไฟล์

## รันทดสอบเพิ่มเติม

จากโฟลเดอร์ `kku-foodshare`:

```powershell
cd .\code
.\mvnw.cmd test
cd ..
node --test code/src/test/js/*.test.mjs
```

Browser journey ใช้ Playwright จาก `test/package.json` และไม่เชื่อมฐานข้อมูลหรือเว็บ Deploy:

```powershell
cd .\test
npm install
npx playwright install chromium
cd ..
$env:NODE_PATH = (Resolve-Path .\test\node_modules).Path
node .\test\onboarding-check.cjs
```

Browser fixture ใช้ frontend จริงกับ mock HTTP boundary เพื่อทดสอบกล่องและสถานะการแสดงผล ส่วน Java integration tests ตรวจ API/ฐานข้อมูลจริงของ test context แยกกัน ไม่ใช่ฐานข้อมูลใช้งานจริง

## Deploy และ Cloudinary/Brevo

ดู [คู่มือ Phase 11](PHASE11-STORAGE-EMAIL-REMINDER-NOTES.md) สำหรับ environment variables, Cloudinary, Brevo และการ Deploy ที่เตรียมไว้เดิม รุ่นนี้ไม่ได้เพิ่ม provider, API key หรือค่าใช้จ่ายจากบริการใหม่

หลังทดสอบในเครื่องแล้วจึงนำโค้ดรุ่นนี้ไป Git checkout เดิม ตรวจ `.env` ยังถูก ignore, commit และ push branch ที่ Render ใช้อยู่ตาม workflow เดิม Render จะ Build และ Flyway รัน V10 ตอนเปิดแอปใหม่ เก็บค่าการเชื่อม Neon, Cloudinary และ Brevo เดิม ไม่ต้องสร้างฐานข้อมูลใหม่
