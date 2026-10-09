# การทดสอบ KKU FoodShare

โฟลเดอร์นี้รวบรวมสคริปต์ทดสอบหน้าเว็บและรายงานผลการทดสอบของระบบ KKU FoodShare

## โครงสร้างไฟล์

| ตำแหน่ง | หน้าที่ |
|---|---|
| `browser-tests/` | สคริปต์ทดสอบหน้าเว็บ `.cjs` และเทสตัวเรียกรายงาน |
| `run-browser-tests.cjs` | ตัวเรียกชุดทดสอบ ส่งต่อไปยัง `browser-tests/` |
| `package.json` | คำสั่ง npm และรายการ dependencies |
| `package-lock.json` | ล็อกเวอร์ชัน dependencies |
| `reports/browser/` | รายงาน Browser แยกตามรอบการรัน |
| `reports/backend-test-results/` | รายงานการทดสอบผ่าน Docker เดิมชื่อ `phase12/` |
| `node_modules/` | Dependencies ที่ติดตั้งในเครื่อง ไม่อัปโหลดขึ้น Git |

Java/JUnit และ JavaScript unit tests อยู่ใน `code/src/test/` ตามโครงสร้างโปรเจกต์เดิม

## สิ่งที่ต้องเตรียม

- Node.js และ npm
- Docker Desktop สำหรับรันแอปและฐานข้อมูล
- Chromium ของ Playwright
- เว็บ local ที่เปิดใช้งานได้ เช่น `http://localhost:8081`

เทสประเภท live สร้างบัญชี โพสต์ และรายการจองสำหรับทดสอบ ควรรันกับฐานข้อมูลทดสอบแยกจากระบบ production

## ติดตั้งครั้งแรก

รันจากโฟลเดอร์หลัก `kku-foodshare`:

```powershell
cd .\test
npm ci
npx playwright install chromium
cd ..
```

หากยังไม่มี `package-lock.json` ให้ใช้ `npm install` แทน `npm ci`

## เปิดเว็บผ่าน Docker

รันจากโฟลเดอร์หลักที่มีไฟล์ Compose และ `.env`:

```powershell
docker compose -p kku-foodshare-phase1 up --build -d
docker compose -p kku-foodshare-phase1 ps
```

รอให้เว็บพร้อมใช้งานก่อนเริ่มทดสอบ โดย URL ต้องตรงกับพอร์ตที่ตั้งใน `.env`

## รัน Browser tests ทั้งหมด

รันจากโฟลเดอร์หลัก `kku-foodshare`:

```powershell
node .\test\run-browser-tests.cjs --base-url http://localhost:8081 --include-live
```

หากมี `scripts/test-browser.ps1` สามารถใช้:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test-browser.ps1 -BaseUrl "http://localhost:8081" -IncludeLive
```

### รันเฉพาะบางชุด

```powershell
node .\test\run-browser-tests.cjs --base-url http://localhost:8081 --include-live --scripts quick-actions-journey,home-overflow-check
```

### ตรวจตัวเรียกและการบันทึกรายงาน

```powershell
node --test .\test\browser-tests\report-runner.test.cjs
```

## ชุดทดสอบ Browser

| สคริปต์ | สิ่งที่ตรวจ | รูปแบบ |
|---|---|---|
| `about-image-check.cjs` | รูปภาพหน้าเกี่ยวกับเรา ข้อความทดแทน และการอ้างอิงใน template/CSS | Source check |
| `admin-posts-check.cjs` | การแสดงผลหน้าจัดการโพสต์สำหรับผู้ดูแล | Fixture |
| `dark-surfaces-check.cjs` | การแสดงผลพื้นผิวและข้อความในธีมมืด | Fixture |
| `onboarding-check.cjs` | การแสดงผลและการโต้ตอบกับคำแนะนำเริ่มต้น | Fixture |
| `phase10-ui-check.cjs` | การแสดงผล UI ที่เกี่ยวข้องกับการปรับปรุง Phase 10 | Fixture |
| `pickup-reminder-check.cjs` | การแสดงผล UI ของการเตือนรับอาหาร | Fixture |
| `browser-journey.cjs` | สมัครสมาชิก โพสต์อาหาร พิกัด แผนที่ จอง รับอาหาร โปรไฟล์ การแจ้งเตือน และการรับมือข้อผิดพลาด | Live พร้อม fixtures สำหรับบริการบางส่วน |
| `quick-actions-journey.cjs` | การจอง QR การปรับสต็อก การแจกแบบออฟไลน์ และการแสดงผลบนมือถือ | Live |
| `home-overflow-check.cjs` | หน้าแรก คำแนะนำ ตัวกรอง และการล้นหน้าจอหลายขนาด | Live |

`Source check` ตรวจจากไฟล์โค้ด ส่วน `Fixture` ใช้ข้อมูลหรือการตอบกลับจำลอง จึงไม่ยืนยันการทำงานของบริการภายนอกจริง

## การทดสอบ Backend ผ่าน Docker

รันจากโฟลเดอร์หลัก:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test-phase12.ps1
```

สคริปต์เรียก services ใน `compose.test.yaml`:

- `java-tests` — ทดสอบ Java/JUnit
- `js-tests` — ทดสอบ JavaScript
- `postgres-tests` — ทดสอบส่วนที่ต้องใช้ PostgreSQL

รายงาน JUnit อยู่ที่ `code/target/surefire-reports/`

ตำแหน่ง Docker logs ขึ้นอยู่กับ path ที่กำหนดในสคริปต์ หากยังไม่ได้ปรับสคริปต์หลังเปลี่ยนชื่อโฟลเดอร์ จะยังบันทึกที่ `test/reports/phase12/`

## อ่านรายงาน Browser

แต่ละรอบสร้างโฟลเดอร์ใหม่ใต้ `reports/browser/` โดยชื่อโฟลเดอร์ใช้เวลา UTC และรหัสแยกแต่ละรอบ

| ไฟล์หรือโฟลเดอร์ | เนื้อหา |
|---|---|
| `summary.md` | สรุปผลสำหรับอ่าน |
| `summary.json` | สรุปผลแบบ JSON |
| `summary.csv` | สรุปผลสำหรับเปิดเป็นตาราง |
| `logs/` | ข้อความและข้อผิดพลาดจากแต่ละชุด |
| `screenshots/` | ภาพหน้าจอที่ชุดทดสอบบันทึก |
| `reports/` | รายละเอียดเพิ่มเติมของแต่ละชุด |

บางชุดไม่ได้สร้างภาพหรือรายงานเพิ่มเติม จึงอาจมีโฟลเดอร์ย่อยว่าง

### ความหมายของผล

| สถานะ | ความหมาย |
|---|---|
| `PASS` | สคริปต์จบสำเร็จ |
| `FAIL` | สคริปต์พบข้อผิดพลาดหรือหมดเวลารัน |
| `NOT_RUN` | ชุดนั้นไม่ได้ถูกเลือกให้รัน |

ตัวอย่างผลที่ผ่านครบ:

```text
PASS 9 / FAIL 0 / NOT_RUN 0
```

จำนวน 9 หมายถึงชุดสคริปต์ ไม่ใช่จำนวน assertions หรือ test cases ทั้งหมด

## ผลที่ยืนยันล่าสุด

| การทดสอบ | ผล |
|---|---|
| Java/JUnit ผ่าน Docker | 48 tests, 0 failures, 0 errors, 0 skipped — BUILD SUCCESS |
| Docker test script | All Docker test commands passed |
| Browser suite บนเว็บ local | PASS 9 / FAIL 0 / NOT_RUN 0 |

ผลดังกล่าวอ้างอิงจากการรันบนเครื่องผู้พัฒนา ก่อนการจัดย้ายไฟล์ทดสอบ ควรรันอีกครั้งหลังเปลี่ยน path เพื่อยืนยันโครงสร้างใหม่ และเก็บรายงานรอบนั้นไว้เป็นหลักฐาน

## ขอบเขตและข้อจำกัด

- Fixtures ไม่ยืนยันการเชื่อมต่อ Google Login, Cloudinary, Brevo หรือบริการแผนที่จริง
- หลัง deploy ต้องตรวจ URL สาธารณะ Swagger UI และเส้นทางใช้งานหลักอีกครั้ง
- เก็บรายงานรอบที่ผ่านครบสำหรับส่งงาน โดยไม่แก้สถานะของรายงานรอบที่ล้มเหลว

## ไฟล์ที่อัปโหลดขึ้น Git

เก็บสคริปต์ คู่มือ `package.json`, `package-lock.json` และรายงานที่ใช้เป็นหลักฐาน

ไม่อัปโหลด `node_modules/`, ไฟล์สำรอง `.bak`, `.env` หรือไฟล์ที่มีรหัสผ่านและ API keys
