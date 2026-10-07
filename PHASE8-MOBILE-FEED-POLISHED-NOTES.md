# Mobile Feed Polished

ต่อจาก Welcome Polished ที่ผู้ใช้ยืนยันว่าเป็นรูปแบบที่ต้องการ คงหน้าเริ่มต้น ภาพประกอบ และธีมเดิม

## งานรอบนี้

1. แถบไอคอนมือถืออยู่ด้านล่าง viewport ตั้งแต่ความกว้างไม่เกิน 900px รองรับ safe-area และเว้นพื้นที่เนื้อหาไม่ให้โดนแถบทับ
2. แก้ backdrop-filter ของ header ที่ทำให้ fixed navigation ถูกยึดกับ header แทน viewport ปิดเอฟเฟกต์เบลอเฉพาะหน้าจอขนาดเล็ก และแยก stacking context ของแผนที่เพื่อไม่ให้ปุ่มบนแผนที่ทับเมนูล่าง
3. การ์ดในหน้าแรก ค้นหา ประวัติผู้แบ่งปัน และโพสต์ที่บันทึก แสดงกว้างไม่เกิน 340px พื้นที่ grid ไม่เกิน 1080px พร้อมลดช่องว่างและขนาดตัวอักษรบางส่วน ปุ่มหลักยังอย่างน้อย 44px
4. คงสัดส่วนภาพสี่เหลี่ยมและการจัด collage เดิม ไม่เปลี่ยนตำแหน่งรูปบนการ์ด ภาพในตัวดูรูปใช้ contain เพื่อมองเห็นรูปเต็มไม่ครอป
5. แตะรูปเปิด dialog ในหน้าเดิม เลื่อนดูได้ครบ รวมรูปที่ซ่อนใต้ +N รองรับปุ่มก่อนหน้า/ถัดไป การปัด และปุ่มลูกศรบนคีย์บอร์ด
6. ปิดรูปด้วยปุ่ม × ปิด ที่เห็นชัด แตะพื้นที่นอก dialog หรือ Escape คืน focus มาที่รูปเดิมโดยไม่ scroll กระโดด ไม่ต้องเปิด/ปิดแท็บใหม่
7. เปิด Ctrl-click/คำสั่งเปิดแท็บใหม่เองได้ตามพฤติกรรมลิงก์ปกติ และยังมีลิงก์สำรองหาก dialog ใช้ไม่ได้
8. เพิ่ม dropdown “แสดงต่อหน้า” 6 / 12 / 24 โพสต์เหนือรายการหน้าแรก ค้นหา และหน้าหลังล็อกอิน โดยใช้ size ของ API เดิม หน้าแรกเริ่ม 6 หน้าค้นหา/หลังล็อกอินเริ่ม 12
9. เปลี่ยนจำนวนแล้วกลับหน้าแรกของผลค้นหา เก็บตัวกรอง/เรียงลำดับเดิม และจำจำนวนใน URL เพิ่ม pagination ของฟีดหน้าแรก แผนที่ยังขอ 200 รายการหน้า 0 ตามเดิม ไม่ลดหมุดตาม dropdown

ไม่มีการเปลี่ยน backend, auth, security config, database, .env หรือ Docker Compose ในรอบนี้ ระบบ QR รหัส 6 หลัก การจอง stock และ owner management ใช้โค้ดเดิม

## ไฟล์ที่แก้/เพิ่ม

| ไฟล์ | งาน |
| --- | --- |
| code/src/main/resources/templates/fragments.html | โหลด CSS และ dialog ดูรูป |
| code/src/main/resources/templates/home.html | dropdown จำนวนและ pagination |
| code/src/main/resources/templates/explore.html | dropdown จำนวน |
| code/src/main/resources/templates/dashboard.html | dropdown จำนวนหลังล็อกอิน |
| code/src/main/resources/static/css/feed-polish.css | CSS การ์ด แถบล่าง และตัวดูรูป |
| code/src/main/resources/static/js/app.js | เชื่อมตัวดูรูป จำนวนโพสต์ และ pagination |
| code/src/main/resources/static/js/ui.js | gallery พร้อมข้อมูลรูปทั้งหมดและ fallback link |
| code/src/main/resources/static/js/image-viewer.mjs | เปิด/ปิดภาพ เปลี่ยนรูป keyboard และ swipe |
| code/src/main/resources/static/js/feed-options.mjs | จำกัดตัวเลือกจำนวนที่รองรับ |
| code/src/test/js/image-viewer.test.mjs | ทดสอบดูรูป legacy/+N/ปิด/swipe/URL |
| code/src/test/js/feed-options.test.mjs | ทดสอบคำขอจำนวนและตัวกรอง/แผนที่ |
| code/src/test/java/com/kku/foodshare/WebPagesTest.java | ตรวจ markup ใหม่ |
| PHASE8-MOBILE-FEED-POLISHED-NOTES.md | คู่มือนี้ |

## วิธีใช้บนเครื่อง Windows

แตก ZIP ใต้ Principle ให้เป็น KKU-FoodShare-Phase8-Mobile-Feed-Polished แล้วเข้าโฟลเดอร์ kku-foodshare ที่มี compose.yaml

คัดลอกเฉพาะคำสั่ง ไม่คัดลอก PS C:\...>:

```powershell
cd "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\Principle\KKU-FoodShare-Phase8-Mobile-Feed-Polished\kku-foodshare"

if (-not (Test-Path ".\compose.yaml")) {
    throw "ยังไม่อยู่ในโฟลเดอร์ที่มี compose.yaml"
}

if (-not (Test-Path ".\.env")) {
    $previousEnv = "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\Principle\KKU-FoodShare-Phase8-Welcome-Polished\kku-foodshare\.env"
    if (-not (Test-Path -LiteralPath $previousEnv)) {
        $previousEnv = "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\Principle\KKU-FoodShare-Phase8-Notifications-Guest-Home (1)\kku-foodshare\.env"
    }
    if (-not (Test-Path -LiteralPath $previousEnv)) {
        throw "ไม่พบ .env เดิม ตรวจชื่อโฟลเดอร์ที่ใช้งานล่าสุดก่อน"
    }
    Copy-Item -LiteralPath $previousEnv -Destination ".\.env"
}

docker compose -p kku-foodshare-phase1 up --build -d
docker compose -p kku-foodshare-phase1 ps
```

ถ้าใช้กล่องอีเมลทดสอบ ให้ใช้คำสั่งนี้แทน up ด้านบน:

```powershell
docker compose -p kku-foodshare-phase1 -f compose.yaml -f compose.mail-local.yaml up --build -d
docker compose -p kku-foodshare-phase1 -f compose.yaml -f compose.mail-local.yaml ps
```

เปิดเว็บเดิม http://localhost:8081 หรือ http://127.0.0.1:8081 หลัง app เป็น healthy และกด Ctrl+F5 เพราะเพิ่ม CSS/JS ใหม่ ถ้าทดสอบบนโทรศัพท์จริง localhost ในโทรศัพท์จะหมายถึงโทรศัพท์ ไม่ใช่พีซี ต้องตั้งการเข้าถึงผ่านเครือข่าย/โดเมนแยกต่างหาก รอบนี้ไม่ได้เปลี่ยนการ bind port ของ Docker

หากไม่ต้องการย้ายโฟลเดอร์ สามารถสำรองไฟล์แล้วคัดลอกเฉพาะไฟล์ในตารางตามตำแหน่งเดิมไปยังโปรเจกต์ที่กำลังใช้ได้ อย่าลืม CSS และ JS ใหม่ทั้งสามไฟล์ แล้ว build จากโฟลเดอร์เดิม ใช้ .env ในที่เดิมได้เลย

## ผลตรวจ

- node --check app.js, ui.js, image-viewer.mjs ผ่าน
- node --test src/test/js/*.test.mjs ผ่าน 75 tests, 0 failures
- ทดสอบทั้ง gallery รูปเดียวเดิม รูป 5 รูปพร้อม +N, ปิดด้วยปุ่ม/backdrop/Escape, focus คืน, ปัด, คีย์บอร์ด, URL ที่ไม่ปลอดภัย, และ Ctrl-click
- ทดสอบคำขอจริงจากฟังก์ชัน home/explore: เปลี่ยนจำนวนแล้ว page=0, เก็บ category/q/ownership/now, map ยังคง size=200&page=0
- ชุดเดิมของ QR, reservation, GPS, routes, Google Maps และ profile menu ผ่าน
- ตรวจ HTML/ID ไม่ซ้ำใน home/explore/dashboard/fragments
- Maven offline test และ package ยังไม่ผ่านขั้นโหลด POM เนื่องจากเครื่องส่งมอบไม่มี Spring Boot parent 4.1.1 ใน cache จึงยังยืนยัน Java tests/การ render Thymeleaf บน Spring Boot จริงไม่ได้
- ยังไม่ได้ตรวจหน้าจอจริงด้วยเบราว์เซอร์ในสภาพแวดล้อมส่งมอบ เพราะพรีวิว localhost เชื่อมจากเบราว์เซอร์นั้นไม่ได้ การทดสอบตัวดูรูปเป็น JS tests ด้วย DOM จำลอง ต้องตรวจภาพและการใช้นิ้วบนเครื่องที่รันเว็บจริงอีกครั้ง
- ZIP มี kku-foodshare ชั้นแรก และไม่มี .env, uploads, target, node_modules, build, .git, JAR หรือ class

## ตรวจบนเครื่องที่ใช้งานจริง

1. ลอง 320 / 390 / 768 / 900px เลื่อนหน้าลง แถบไอคอนต้องอยู่ล่างตลอด และไม่ทับปุ่มบนหน้า
2. ที่ desktop zoom 100% การ์ดไม่เกิน 340px รูปยังจัดตามแบบเดิม
3. แตะรูปแรกและ +N ต้องเห็นครบ เปิด/ปิดหลายครั้งไม่กลับขึ้นต้นหน้า ลอง Esc/คีย์บอร์ด/ปัด และปิดจากภายนอก panel
4. ลอง dropdown 6 / 12 / 24 ทั้งหน้าแรก ค้นหา และหลังล็อกอิน แล้วเปลี่ยนหน้า/ตัวกรอง ตรวจหมุดไม่ลดลงตามจำนวนต่อหน้า
5. ยืนยัน QR จอง stock แผนที่ Google Maps เมนูโปรไฟล์และอีเมลทดสอบเดิม

คำสั่งทดสอบเมื่อมี Maven/Node และ dependencies พร้อม:

```powershell
cd .\code
mvn -o -B test
node --check src/main/resources/static/js/app.js
node --check src/main/resources/static/js/ui.js
node --check src/main/resources/static/js/image-viewer.mjs
node --test src/test/js/*.test.mjs
mvn -o -B -DskipTests package
cd ..
```

หาก Maven offline ขาด dependency ให้ดาวน์โหลดก่อนด้วย `mvn -B test` ขณะออนไลน์ โดยไม่เปลี่ยน POM
