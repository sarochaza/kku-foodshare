# KKU FoodShare — Welcome Polished

ต่อจาก Notifications Guest Home เวอร์ชันล่าสุดที่ใช้ทดสอบอีเมลได้ในเครื่อง

## สิ่งที่ปรับรอบนี้

- `/` เป็นหน้าเริ่มต้นแนะนำเว็บ ทั้ง localhost และ 127.0.0.1 ผู้ไม่ได้ล็อกอินยังดูอาหารและแผนที่ได้ตามเดิม
- ภาพประกอบใหม่ นักศึกษาแบ่งปันอาหารในมหาวิทยาลัย โทนฟ้า–เขียว อบอุ่น ไม่มีข้อความฝังในภาพ
- ข้อความและปุ่มเป็น HTML จริง รองรับมือถือ พร้อมการ์ดแนะนำผู้รับ/ผู้แบ่งปัน วิธีใช้ และ FAQ ที่กดเปิดอ่านได้
- CSS แยกใน welcome.css โดยตกแต่งเนื้อหาหลักภายใต้ .welcome-page มีขอบโค้ง เงาเบา พื้นหลังไล่สี และป้ายลอยขยับเล็กน้อย ปิด animation เมื่อผู้ใช้ตั้ง reduced motion
- ลบ `>` ที่หลุดจาก markup ตรง header และลูกศรตกแต่งที่ไม่จำเป็นในหน้าเริ่มต้น/ทางลัดผู้แบ่งปัน
- เพิ่มแท็บหน้าแรก แยกสถานะค้นหา/แผนที่/วิธีใช้งาน มี aria-current และอัปเดตเมื่อเปลี่ยน hash หรือสลับรายการ/แผนที่ในหน้าค้นหา
- เมนูบนแท็บเล็ต 601–900px แสดงเป็นแถวที่สอง มือถือใช้แถบล่างเดิม

ไม่มีการเปลี่ยน Authentication, Security, Docker Compose, .env, database หรือ logic จำนวนอาหาร/จอง/QR/เจ้าของโพสต์ในรอบนี้ ระบบ Mailpit และลืมรหัสผ่านจากรอบก่อนยังอยู่ตามเดิม การรันทดสอบ SMTP จริงยังต้องตั้งค่าผู้ส่งตามคู่มือรอบก่อน

## ไฟล์ที่แก้หรือเพิ่ม

| ไฟล์ | งาน |
| --- | --- |
| code/src/main/resources/templates/home.html | หน้าเริ่มต้น ภาพ CTA และ FAQ |
| code/src/main/resources/templates/fragments.html | แก้เครื่องหมายเกิน โหลด CSS และข้อมูลแท็บ |
| code/src/main/resources/static/css/welcome.css | CSS ใหม่ของหน้าเริ่มต้นและ header |
| code/src/main/resources/static/js/app.js | เรียก navigation และอัปเดตสถานะเมื่อสลับแผนที่/รายการ |
| code/src/main/resources/static/js/navigation.mjs | ตรวจ route/query/hash และสีแท็บ |
| code/src/main/resources/static/images/welcome-community.webp | ภาพใหม่ 1536×1024 ประมาณ 168 KiB |
| code/src/test/js/navigation.test.mjs | ทดสอบสถานะแท็บ desktop/mobile/history |
| code/src/test/java/com/kku/foodshare/WebPagesTest.java | ปรับ assertion ให้ตรงข้อความและภาพใหม่ |
| PHASE8-WELCOME-POLISHED-NOTES.md | คู่มือฉบับนี้ |

## ใช้โค้ดใหม่บน Windows ทีละขั้น

แตก ZIP ไว้ใต้ Principle ให้ได้โครงสร้าง:

```
KKU-FoodShare-Phase8-Welcome-Polished/
  kku-foodshare/
    compose.yaml
    code/
```

คัดลอกเฉพาะคำสั่ง ไม่คัดลอก PS C:\...> และใส่เครื่องหมายคำพูดรอบ path ทุกครั้ง

```powershell
cd "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\Principle\KKU-FoodShare-Phase8-Welcome-Polished\kku-foodshare"

if (-not (Test-Path -LiteralPath ".\compose.yaml")) {
    throw "ยังไม่อยู่ในโฟลเดอร์ kku-foodshare ที่มี compose.yaml"
}

if (-not (Test-Path -LiteralPath ".\.env")) {
    $previousEnv = "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\Principle\KKU-FoodShare-Phase8-Notifications-Guest-Home (1)\kku-foodshare\.env"
    if (-not (Test-Path -LiteralPath $previousEnv)) {
        throw "ไม่พบ .env เดิม ตรวจชื่อโฟลเดอร์ต้นทางก่อน"
    }
    Copy-Item -LiteralPath $previousEnv -Destination ".\.env"
}

Test-Path -LiteralPath ".\.env"
docker compose -p kku-foodshare-phase1 up --build -d
docker compose -p kku-foodshare-phase1 ps
```

ใช้ project name เดิม จะอัปเดต container ของเว็บเดิม และใช้ volume เดิม หากเครื่องใช้ APP_PORT=8081 ตามก่อนหน้านี้ เปิด http://localhost:8081 หรือ http://127.0.0.1:8081 หลัง app แสดง healthy

หากยังใช้กล่องอีเมลทดสอบ Mailpit ให้ใช้คำสั่งนี้แทนคำสั่ง up ด้านบน:

```powershell
docker compose -p kku-foodshare-phase1 -f compose.yaml -f compose.mail-local.yaml up --build -d
docker compose -p kku-foodshare-phase1 -f compose.yaml -f compose.mail-local.yaml ps
```

กล่องอีเมลทดสอบ: http://127.0.0.1:8025 ไม่ใช่ Gmail จริง ดู PHASE8-WELCOME-PASSWORD-RESET-NOTES.md สำหรับรายละเอียด

หลังอัปเดต กด Ctrl+F5 เพื่อโหลด CSS/JS ใหม่ ทดสอบหน้าเริ่มต้นก่อนล็อกอินด้วยหน้าต่าง InPrivate/Incognito เนื่องจากอีกแท็บในหน้าต่างเดิมใช้ session ร่วมกัน

## ถ้าใช้วิธีคัดลอกไฟล์แทนย้ายทั้งโปรเจกต์

สำรองไฟล์เดิมก่อน แล้วแทนที่/เพิ่มไฟล์ตามตารางด้านบนโดยรักษาตำแหน่งเดิม ไม่คัดลอก .env หรือ compose จากเครื่องอื่น และอย่าลืม welcome.css, navigation.mjs และภาพ welcome-community.webp จากนั้นรัน Docker build จากโฟลเดอร์เดิมที่ใช้งานได้ วิธีนี้ยังคงใช้ .env เดิมในที่เดิม

## ตรวจที่ทำแล้วและข้อจำกัด

- `node --check src/main/resources/static/js/app.js` ผ่าน (รันจาก code)
- `node --check src/main/resources/static/js/navigation.mjs` ผ่าน
- `node --test src/test/js/*.test.mjs` ผ่าน 65 tests ไม่มี fail ครอบคลุมชุดเก่าของ map/location/QR/quick-actions/travel และแท็บใหม่
- ตรวจ HTML: ID ไม่ซ้ำ และไม่มี text node `>` เกินใน header
- ภาพใหม่อยู่ใน static/images จริง และกำหนด width/height/aspect ratio ป้องกัน layout ขยับ
- `mvn -o -B test` และ `mvn -o -B -DskipTests package` รันไม่สำเร็จในสภาพแวดล้อมส่งมอบ เพราะ Spring Boot parent 4.1.1 ไม่มีใน offline cache จึงยังยืนยัน Java regression/Thymeleaf rendering จริงไม่ได้ ไม่ได้แก้ POM เพื่อหลบข้อผิดพลาด
- เบราว์เซอร์ทดสอบเข้า localhost ของสภาพแวดล้อมส่งมอบไม่ได้ จึงยังไม่ได้ยืนยัน screenshot หรือ overflow ที่ 320px จากเบราว์เซอร์จริง CSS ได้เตรียม responsive breakpoints ไว้ แต่ต้องตรวจบนเครื่องที่รันเว็บได้ตามรายการข้างล่าง
- ZIP มี kku-foodshare ชั้นแรก ไม่รวม .env, target, uploads, .git, JAR/class/build files

คำสั่งตรวจบนเครื่องคุณเมื่อมี Maven/Node และ dependencies พร้อม:

```powershell
cd .\code
mvn -o -B test
node --check src/main/resources/static/js/app.js
node --check src/main/resources/static/js/navigation.mjs
node --test src/test/js/*.test.mjs
mvn -o -B -DskipTests package
cd ..
```

ถ้า Maven offline ขาด dependencies ให้รัน `mvn -B test` ขณะออนไลน์เพื่อดาวน์โหลดก่อน ไม่ต้องเปลี่ยนเวอร์ชัน Spring Boot

## ตรวจใช้งานจริงก่อนนำไปต่อ

1. InPrivate เปิด `/` ต้องเห็นหน้าแนะนำเว็บ ไม่ถูกพาไป login เอง
2. คลิกค้นหาอาหาร/แผนที่/วิธีใช้งาน สีฟ้าต้องอยู่แท็บนั้น ตรวจสลับปุ่มแผนที่กับรายการในหน้าค้นหาด้วย
3. FAQ กดเปิด/ปิดด้วยเมาส์และคีย์บอร์ดได้
4. ที่ 320, 390, 768, 1024, 1366px ตรวจไม่ล้นขอบ ภาพไม่บิด ปุ่มแตะสะดวก
5. ล็อกอิน ตรวจ popover โปรไฟล์ แจ้งเตือน ตัวกรอง หมุด ตำแหน่ง และ Google Maps
6. ใช้สองบัญชีใน browser profile แยกกัน ตรวจจอง/จำนวนคงเหลือ/เจ้าของโพสต์/สแกน QR และรหัส 6 หลัก

## ฟีเจอร์ที่ควรเพิ่มต่อ (ยังไม่เพิ่มรอบนี้)

- แจ้งเตือนก่อนหมดเวลานัดรับ โดยใช้ระบบ in-app notifications เดิมและไม่แจ้งซ้ำ
- หน้าสรุปการรับอาหาร: อาหาร จุดนัดรับ เวลารับ QR และสถานะรวมในพื้นที่เดียว โดยใช้ข้อมูล reservation เดิม
- ข้อมูลความปลอดภัยอาหารที่อ่านง่าย เช่น ส่วนผสมที่อาจแพ้และคำแนะนำการเก็บรักษา จากข้อมูลจริงที่ผู้โพสต์กรอก
- ระบบบันทึกโพสต์มีอยู่แล้วในโปรเจกต์ ไม่จำเป็นต้องสร้างซ้ำ

## ภาพประกอบ

สร้างด้วยเครื่องมือสร้างภาพในตัว แล้วบีบอัดเป็น WebP สำหรับเว็บ

Prompt ที่ใช้: "Use case: stylized-concept. Asset type: polished KKU FoodShare landing page hero illustration, consumed as an image in an existing Thai blue-green food sharing site. Create an original warm plush soft 3D illustration: two friendly Thai university students, a young woman and young man, exchanging a modest cardboard box of fresh vegetables, oranges and a neatly packed meal, in a leafy sunny university campus. Small friendly cat sits near them. Sky blue and mint green with cream accents, rounded tactile materials, gentle airy sunlight, uplifting and welcoming. Landscape composition 3:2, characters centered in right two thirds, open pale sky toward upper left, background softly defocused. No smartphone, no interface, no words, no letters, no logos, no watermarks. Detailed high quality render with restrained visual clutter. This is artwork only; all Thai headings and functional buttons are added in HTML separately."
