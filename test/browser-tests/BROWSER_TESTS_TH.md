# การรันและเก็บผลทดสอบ Browser — ภาษาไทย

## วิธีนำไฟล์ไปใช้
แตก ZIP ที่โฟลเดอร์หลัก `kku-foodshare` แล้วผสานโฟลเดอร์ `test/` และ `scripts/` เข้ากับของเดิม ยอมรับการแทนที่ไฟล์ที่ชื่อซ้ำ ชุดนี้มีเฉพาะไฟล์ที่เปลี่ยนและไฟล์ตัวรันใหม่ ไม่ต้องย้าย source code หรือ README หลัก

ไฟล์ที่แก้เดิมมี `test/browser-journey.cjs` และ `test/quick-actions-journey.cjs` ตัวเลือกปุ่มสมัครสมาชิกและเข้าสู่ระบบเปลี่ยนเป็น `form.auth-form button[type=submit]` เพราะฟอร์มส่งความคิดเห็นที่อยู่ในหน้าร่วมมีปุ่ม submit ด้วย ทำให้ selector เดิมเลือกได้สองปุ่มและ Playwright หยุดด้วย strict mode violation

ติดตั้ง npm และ Chromium สำเร็จแล้วตาม Log ที่ส่งมา สามารถข้ามขั้นตอนติดตั้งได้ หากนำไปเครื่องใหม่ให้รันจากโฟลเดอร์หลัก:

```powershell
Push-Location .\test
npm install
npx playwright install chromium
Pop-Location
```

## รันซ้ำเฉพาะ Browser Journey ที่หยุด
จากตำแหน่งปัจจุบันที่ลงท้ายด้วย `kku-foodshare\test>` ให้กลับหนึ่งระดับก่อน:

```powershell
cd ..
powershell -ExecutionPolicy Bypass -File .\scripts\test-browser.ps1 -BaseUrl "http://localhost:8081" -IncludeLive -Scripts browser-journey
$LASTEXITCODE
```

เว็บต้องเปิดที่ URL นี้ และใช้ฐานข้อมูลสำหรับทดสอบที่แยกจากข้อมูลใช้งานจริง เพราะ journey สร้างบัญชี โพสต์ และการจองทดสอบ เทสที่ล้มกลางทางอาจเหลือข้อมูลทดสอบ รอบใหม่ใช้ชื่อบัญชีพร้อมรหัสเวลา

ตัวรันบันทึกผลให้เอง ไม่ต้องใช้ Tee-Object เพิ่ม ค่า 0 หมายถึงไฟล์ที่เลือกจบสำเร็จ ค่า 1 หมายถึงมีไฟล์ที่ไม่ผ่าน โปรดดู summary ประกอบเพื่อทราบว่าเลือกทดสอบอะไรบ้าง

## คำสั่งอื่น
รันเฉพาะการตรวจ source และหน้า fixture ทั้ง 6 ไฟล์:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test-browser.ps1
```

รันเฉพาะ onboarding บนหน้า fixture:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test-browser.ps1 -Scripts onboarding-check
```

รันทั้งหมด 9 ไฟล์ รวม journey ที่ต่อเว็บจริง:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test-browser.ps1 -BaseUrl "http://localhost:8081" -IncludeLive
```

ตัวรันหาตำแหน่งโฟลเดอร์หลักเอง และตั้ง working directory ให้เทสที่อ่านไฟล์ด้วย relative path หากเรียกจาก `test/` โดยไม่กลับหนึ่งระดับ ใช้เส้นทาง `..\scripts\test-browser.ps1` แทน

## ผลลัพธ์อยู่ด้วยกัน
ผลแต่ละรอบอยู่ใต้ `test/reports/browser/<วันเวลา-รหัสรอบ>/` ชื่อโฟลเดอร์ใช้เวลา UTC และมีรหัสแยกรอบเพื่อไม่ทับผลเดิม

| ตำแหน่งภายในรอบ | สิ่งที่เก็บ |
|---|---|
| `summary.md` | สรุปภาษาไทยพร้อมลิงก์ Log |
| `summary.json` | ผลแต่ละไฟล์ ขอบเขต เวลา และ exit code |
| `summary.csv` | ตารางสำหรับเปิดหรือรวมรายงาน |
| `logs/<ชื่อเทส>.log` | stdout และ stderr ของเทสนั้น บันทึก UTF-8 |
| `screenshots/<ชื่อเทส>/` | ภาพที่เทสนั้นสร้าง |
| `reports/<ชื่อเทส>/` | รายงานเสริม เช่น browser-result.json และ failure-body.txt |

บางเทสสร้างเฉพาะ Log โฟลเดอร์ภาพและรายงานเสริมของเทสนั้นจึงอาจว่าง ไฟล์ภาพเก่าที่เคยอยู่ใน `img/` ไม่ถูกย้ายหรือทับโดยชุดนี้

เปิดผลล่าสุดจากโฟลเดอร์หลักได้ด้วย:

```powershell
$latestBrowserRun = Get-ChildItem .\test\reports\browser -Directory |
    Where-Object { Test-Path (Join-Path $_.FullName "summary.md") } |
    Sort-Object Name -Descending |
    Select-Object -First 1
if ($latestBrowserRun) { Invoke-Item $latestBrowserRun.FullName }
```

## อ่านผลอย่างไร
- **PASS**: process ของเทสจบด้วย exit code 0
- **FAIL**: เทสส่ง exit code ที่ไม่ใช่ 0, เปิด process ไม่สำเร็จ หรือหมดเวลา (5 นาทีต่อไฟล์)
- **NOT_RUN**: ไม่ได้เลือกให้รันในรอบนั้น ไม่ถือว่าทดสอบผ่าน

เมื่อเทสหนึ่งไม่ผ่าน ตัวรันยังรันไฟล์ที่เหลือต่อ และเก็บผลล้มไว้ใน summary หากเลือกเฉพาะ browser-journey เทสอีก 8 ไฟล์จะเป็น NOT_RUN

| ขอบเขต | ไฟล์ | สิ่งที่ยืนยัน |
|---|---|---|
| source | about-image-check | ตรวจ source และไฟล์ภาพ |
| fixture | admin-posts-check, dark-surfaces-check, onboarding-check, phase10-ui-check, pickup-reminder-check | ตรวจ UI กับข้อมูลหรือหน้าจำลองของเทส |
| live | browser-journey, quick-actions-journey, home-overflow-check | ต่อเว็บที่ BaseUrl; สอง journey แรกเขียนข้อมูลทดสอบ |

PASS ของ fixture ไม่ได้ยืนยันว่า API หรือฐานข้อมูลจริงทำงานผ่าน ส่วน journey มีการจำลองบริการภายนอกบางรายการ เช่น tile/geocoder/routing ผลผ่านจึงไม่ใช่หลักฐานว่าผู้ให้บริการภายนอกพร้อมใช้งาน

## ตรวจตัวรันรายงาน
จากโฟลเดอร์หลัก:

```powershell
node --test test/report-runner.test.cjs
```

มี 3 กรณีตรวจ: บันทึก stdout/stderr และ exit code จริงพร้อมรันต่อหลังล้ม, สร้างรอบใหม่ไม่ทับผลเก่าและติดป้าย NOT_RUN, และบันทึกรายงานเมื่อหมดเวลา การตรวจตัวรันนี้แยกจากการตรวจเว็บด้วย Chromium

