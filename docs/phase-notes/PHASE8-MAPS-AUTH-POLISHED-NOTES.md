# Phase 8 — Sharing Map Consistency + Nearby Fix + Auth Visual Polish

ต่อจาก KKU-FoodShare-Phase8-Comments-Replies รอบนี้แก้เฉพาะมุมมองแผนที่ ตำแหน่ง หัวหน้าต่างคอมเมนต์ และหน้าตา login/register

## สิ่งที่แก้และสาเหตุ

### แผนที่แบบเดียวกัน

เดิม `/home` หลังล็อกอินใช้ `dashboard.html` ซึ่งยังมีแผนที่แบบเก่า ไม่มีรายการด้านข้าง แต่ `/explore?view=map` มี rich map อยู่แล้ว จึงทำให้กดไอคอนแล้วได้หน้าตาต่างจากแท็บ “แผนที่แบ่งปัน”

ตอนนี้ทั้ง `dashboard.html` และ `explore.html` ใช้ Thymeleaf fragment `sharingMap` เดียวกันจาก `fragments.html` มีรายการอาหารด้านข้าง ขยาย/คืนขนาด และ preview เมื่อเลือกหมุดเหมือนกัน กดไอคอนแผนที่แล้วคืนมุมมองปกติพร้อมด้านข้าง แม้ก่อนหน้านั้นจะเคยขยายไว้

หน้าแรก `/` มีไอคอนเปิดแผนที่แบ่งปันที่ไป `/explore?view=map` โดยคงหมวด/รับได้ตอนนี้/การเรียงที่เลือกไว้ แผนที่ในหน้าแรกเดิมยังอยู่

### ใกล้ฉัน

เดิม map API เรียงตามเวลาหมดอายุและไม่รับพิกัด ขณะที่ปุ่มใกล้ฉันปรับเฉพาะรายการ อีกทั้ง viewport รวมหมุดที่อยู่ไกลทั้งหมดจนไม่เห็นความเปลี่ยนแปลงรอบตัวเรา

- ใช้ search API เดิมที่รองรับ `sort=nearby`, `lat`, `lng`, `size=200` กับข้อมูลหมุดเมื่อเลือกใกล้ฉัน ไม่มี backend/API ใหม่
- หมวด คำค้น รับได้ตอนนี้ และโพสต์ของฉัน/คนอื่นยังส่งไปครบ
- แผนที่โฟกัสตำแหน่งผู้ใช้กับหมุดรอบ 3 กม. ถ้าไม่มีหมุดในบริเวณนั้นยังแสดงจุดผู้ใช้ รายการด้านข้างยังเลือกอาหารที่อยู่ไกลได้
- จัดอันดับใกล้ฉันด้วยระยะเส้นตรงของระบบเดิม โดยแสดงข้อความระบุให้ชัด ส่วนระยะถนนและเวลาเดินทางจริงยังใช้ระบบ routing เดิม
- กดปุ่มใกล้ฉันจะขอพิกัดใหม่ พร้อมข้อความกำลังค้นหา/ความคลาดเคลื่อน และแยกกรณีไม่ได้อนุญาต GPS, ระบุตำแหน่งไม่ได้, timeout
- เมื่อ GPS ใช้ไม่ได้ มีปุ่ม “เลือกจุดของฉันบนแผนที่” ให้แตะพื้นที่แผนที่หนึ่งครั้งแล้วใช้จุดนั้นค้นหาใกล้ฉันได้ กดใกล้ฉันอีกครั้งเพื่อกลับไปขอ GPS
- ไม่ยอมรับพิกัด null จาก session cache เป็นตำแหน่ง 0,0 และป้องกันผลตอบกลับเก่าทับตำแหน่ง/มุมมองใหม่

การได้ตำแหน่งจริงยังขึ้นกับบริการตำแหน่งของอุปกรณ์และสิทธิ์ที่ให้กับเว็บไซต์ โหมดเลือกจุดเองเป็นทางเลือกเมื่ออุปกรณ์ไม่ให้พิกัด ไม่ใช่ตำแหน่ง GPS จำลอง

### คอมเมนต์

เอาคำ “ความคิดเห็น” ออกจากหัวด้านบนของ dialog ใช้ชื่ออาหารแทน พร้อมจำนวน “ข้อความ” คอมเมนต์/ตอบกลับ/แจ้งเตือนยังทำงานเดิม และ dialog ที่ปิดอยู่ไม่แสดงแถบบนหน้าอื่น

### Login / Register

- พื้นหลังฟ้า–เขียวไล่เฉด การ์ดมนมีเงา กรอบ input พร้อมไอคอนและ focus state
- ใช้มาสคอตเดิม เพิ่มการลอยเบา ๆ การ์ดข้อความลอย วงแสง และการปรากฏของการ์ด
- ปรับ layout ตั้งแต่กว้าง 320px ขนาด input บนมือถือ 16px และปุ่มสำคัญอย่างน้อย 44px
- รองรับ `prefers-reduced-motion` เพื่อปิด animation
- เพิ่มปุ่มแสดง/ซ่อนรหัสผ่าน เป็นการเปลี่ยนหน้าตา input เท่านั้น ไม่อ่าน/ส่ง/เก็บรหัสผ่านใหม่
- คง native POST, CSRF, autocomplete, binding และ backend validation เดิม รวมลิงก์ลืมรหัสผ่านและเงื่อนไขแสดง Google login

## ไฟล์ที่แก้

เส้นทางต่อไปนี้อยู่ใต้ `code/src/`:

- `main/resources/static/js/app.js`, `maps.js`, `comments.mjs`
- `main/resources/static/js/map-discovery.mjs` (เพิ่ม)
- `main/resources/static/js/auth.mjs` (เพิ่ม)
- `main/resources/static/css/feed-polish.css`
- `main/resources/static/css/auth-polish.css` (เพิ่ม)
- `main/resources/templates/fragments.html`, `home.html`, `explore.html`, `dashboard.html`, `login.html`, `register.html`
- `test/js/map-discovery.test.mjs`, `auth.test.mjs` (เพิ่ม)
- `test/js/maps.test.mjs`, `comments.test.mjs`, `feed-options.test.mjs`, `social-feed.test.mjs`
- `test/java/com/kku/foodshare/WebPagesTest.java` (เพิ่ม regression ว่า `/home` และ `/explore` มีแผนที่พร้อม sidebar/preview แบบเดียวกัน)

เพิ่มเอกสารนี้และอัปเดตลิงก์บันทึกรุ่นล่าสุดใน `PHASE8-SOCIAL-FEED-NOTES.md`

ไม่แก้ production Java, Authentication/Security, Compose, `.env`, QR, รหัสรับอาหาร 6 หลัก, reservation/stock/limit, owner management, extension, routing/Google Maps หรือ image viewer และไม่มี migration ใหม่ในรอบนี้ (V9 จากรอบคอมเมนต์ยังอยู่ครบ)

## ผลตรวจจริง

- `node --check src/main/resources/static/js/app.js`: ผ่าน
- ตรวจ syntax ของ `maps.js`, `map-discovery.mjs`, `auth.mjs`, `comments.mjs`: ผ่าน
- `node --test src/test/js/*.test.mjs`: ผ่าน 100 tests, fail 0 รวม regression เดิมและการกด nearby/เลือกจุดเอง/แผนที่ร่วมกัน
- HTML parser ตรวจไฟล์ที่แก้และโครงสร้าง form: ผ่าน; เมื่อแทน fragment แผนที่ในทั้งสองหน้าไม่มี id ซ้ำ และ sidebar/preview อยู่ถูกตำแหน่ง
- `mvn -o -B test` และ `mvn -o -B -DskipTests package`: รันแล้วหยุดก่อน compile เพราะเครื่องส่งมอบไม่มี offline cache ของ `spring-boot-starter-parent:4.1.1`
- จึงยังไม่ยืนยันว่า Java tests/build ผ่าน และยังไม่ได้ตรวจหน้าจอจริง/GPS จริงกับแอป Spring Boot ที่กำลังรัน ต้องตรวจในเครื่องผู้ใช้หลัง build

## เปิดบน PowerShell

แตก ZIP ลง `Principle` ตั้งชื่อโฟลเดอร์ `KKU-FoodShare-Phase8-Maps-Auth-Polished` ภายในมี `kku-foodshare` ชั้นแรก

คัดลอกเฉพาะคำสั่ง ไม่คัดลอกข้อความ `PS C:\...>`:

```powershell
cd "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\Principle\KKU-FoodShare-Phase8-Maps-Auth-Polished\kku-foodshare"

if (-not (Test-Path -LiteralPath ".\.env")) {
    $principleRoot = "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\Principle"
    $workingEnv = $null
    foreach ($pattern in @("KKU-FoodShare-Phase8-Comments-Replies*", "KKU-FoodShare-Phase8-Mobile-Feed-Polished*", "KKU-FoodShare-Phase8-Welcome-Polished*", "KKU-FoodShare-Phase8-Notifications-Guest-Home*")) {
        $workingEnv = Get-ChildItem -LiteralPath $principleRoot -Directory -Filter $pattern |
            Sort-Object LastWriteTime -Descending |
            ForEach-Object { Join-Path $_.FullName "kku-foodshare\.env" } |
            Where-Object { Test-Path -LiteralPath $_ } |
            Select-Object -First 1
        if ($workingEnv) { break }
    }
    if (-not $workingEnv) { throw "ไม่พบ .env จากรุ่นก่อน ให้ระบุไฟล์ .env ของโฟลเดอร์ที่รันได้ล่าสุดก่อน" }
    Copy-Item -LiteralPath $workingEnv -Destination ".\.env"
}

docker compose -p kku-foodshare-phase1 up --build -d
docker compose -p kku-foodshare-phase1 ps
```

ถ้าชื่อโฟลเดอร์ที่แตกมี `(1)` ให้ใส่ชื่อจริงใน `cd` ภายในเครื่องหมายคำพูด หากมี `.env` อยู่แล้ว script จะไม่เขียนทับ การค้นหา `.env` รองรับสำเนารุ่นเก่าที่มี `(1)` หรือเลขท้ายด้วย

เปิด `http://localhost:8081` หรือ `http://127.0.0.1:8081` รอแอป healthy แล้วกด `Ctrl+F5` เพื่อโหลด JS/CSS ใหม่ ไม่ต้องสร้างฐานข้อมูลใหม่ และไม่ใช้ `down -v`

ถ้ารอบก่อนใช้ Mailpit เพื่อทดสอบลืมรหัสผ่าน ให้รันไฟล์ override เดิมแทนคำสั่ง up ข้างบน:

```powershell
docker compose -p kku-foodshare-phase1 -f compose.yaml -f compose.mail-local.yaml up --build -d
docker compose -p kku-foodshare-phase1 -f compose.yaml -f compose.mail-local.yaml ps
```

กล่องเมลทดสอบอยู่ที่ `http://127.0.0.1:8025`

## ตรวจหลังเปิด

1. ล็อกอินแล้วเปิด `/home` กดไอคอนแผนที่ เปรียบเทียบกับแท็บ “แผนที่แบ่งปัน”: ต้องมีแผนที่/รายการด้านข้าง/preview เดียวกัน ขยายแล้วกดไอคอนอีกครั้งต้องคืนข้อมูลด้านข้าง
2. กดใกล้ฉันและอนุญาตตำแหน่ง ตรวจหมุดสีน้ำเงินและอาหารรอบตัว ถ้าไม่อนุญาตให้ลองเลือกจุดเองบนแผนที่
3. ทดสอบตัวกรองหมวด/รับได้ตอนนี้/ของฉัน/คนอื่น ในทั้งรายการและแผนที่
4. เปิด login/register ที่ desktop และหน้าจอ 320/390px ตรวจ animation, focus, แสดง/ซ่อนรหัสผ่าน, ข้อความ validation และลืมรหัสผ่าน
5. เปิดคอมเมนต์จาก feed/แจ้งเตือน ตรวจว่าหัวแสดงชื่ออาหาร ตอบกลับและเปิดรูปเหมือนเดิม
6. ทดสอบจอง/stock/QR/รหัสรับ 6 หลัก และเส้นทาง Google Maps ของเดิม

คำสั่งตรวจจาก `kku-foodshare`:

```powershell
cd .\code
.\mvnw.cmd -o -B test
node --check src/main/resources/static/js/app.js
node --test src/test/js/*.test.mjs
.\mvnw.cmd -o -B -DskipTests package
cd ..
```

ถ้า Maven dependencies ยังไม่อยู่ในเครื่อง ให้เอา `-o` ออกเพื่อดาวน์โหลดก่อน แล้วกลับมารันคำสั่ง offline เดิม

ZIP ไม่มี `.env`, uploads, target, node_modules หรือไฟล์ build
