# Phase 8 — Compact Height + Click-to-read Comments + Replies

ต่อจาก KKU-FoodShare-Phase8-Mobile-Feed-Polished โดยแก้เฉพาะการ์ดอาหารและคอมเมนต์

## สิ่งที่เปลี่ยน

- เอา dropdown “แสดงต่อหน้า” ออกจากหน้าแรก/ค้นหาอาหาร/หน้า dashboard; pagination ปกติยังอยู่ (หน้าแรก 6 รายการ, ค้นหา 12 รายการ)
- คืนความกว้างการ์ดตาม grid เดิม ลดความสูงด้วยกรอบรูป 4:3 สูงไม่เกิน 300px และรายละเอียดสั้น 2 บรรทัด อ่านรายละเอียดเต็มได้ในหน้าโพสต์
- ตัวดูรูปเดิมไม่เปลี่ยน: กดรูปเปิด ปิดด้วยปุ่ม/พื้นหลัง/Escape และปัดเปลี่ยนรูปได้
- เอาตัวอย่างคอมเมนต์ใน feed ออก เหลือปุ่ม “ความคิดเห็น” พร้อมจำนวนจริง กดแล้วเปิด dialog ที่มีบริบทโพสต์ รูป และรายการคอมเมนต์
- หน้า detail ก็ใช้ปุ่มเปิดคอมเมนต์ถัดจากส่วนรูป/รายละเอียดอาหาร; ลิงก์แจ้งเตือน `#post-comments` เปิด dialog ให้ทันที
- คอมเมนต์แสดงรูปโปรไฟล์ ชื่อ เวลา ข้อความ และป้ายผู้แบ่งปัน มีปุ่มตอบกลับและเมนูจุดสามจุดสำหรับลบ/รายงาน
- ตอบกลับต่อกันได้ แต่จัดเป็นเธรดเยื้องระดับเดียว มีชื่อคนที่ตอบถึงและปุ่มเปิดอ่านคำตอบ ไม่เพิ่มระบบ like/reaction
- คอมเมนต์ใหม่แจ้งเจ้าของโพสต์ การตอบกลับแจ้งทั้งเจ้าของโพสต์และผู้ถูกตอบโดยไม่ส่งซ้ำให้คนเดียวกัน และไม่แจ้งผู้ทำรายการเอง ใช้ระบบแจ้งเตือนเดิมพร้อมข้อมูลผู้กระทำ
- ลบคอมเมนต์แบบ soft delete เหมือนเดิม คำตอบที่มีอยู่ยังอ่านได้ พร้อมข้อความว่าต้นทางถูกลบ การตอบใหม่ไปยังต้นทางที่ถูกลบจะถูกปฏิเสธ
- ใช้สิทธิ์เดิม: ผู้เขียน/เจ้าของโพสต์/admin ลบได้ ผู้เข้าระบบเท่านั้นเขียน/ตอบกลับได้ guest ได้คำเชิญเข้าสู่ระบบ และไม่เรียก comments API ที่ระบบเดิมป้องกันไว้
- โหลดเมื่อเปิด dialog เท่านั้น มีอ่านเพิ่มเติม ป้องกันกดส่งซ้ำ เก็บร่างเมื่อส่งไม่สำเร็จ และไม่แสดงผล request เก่าหลังปิดหรือเปลี่ยนโพสต์
- เมนูมือถือด้านล่างและ Profile popover คงเดิม ไม่มีการแก้ map, GPS, route, Google Maps, QR, reservation/stock, authentication/security, Compose หรือ .env

## ฐานข้อมูล

เพิ่ม migration `V9__comment_replies.sql` ซึ่งเพิ่ม `parent_comment_id`, `reply_to_comment_id` และ index ใน `post_comments` เท่านั้น คอมเมนต์เก่ากลายเป็นคอมเมนต์ระดับบนด้วยค่า null ไม่ลบหรือเขียนทับข้อมูลเก่า

Flyway จะใช้ migration เมื่อเริ่มแอปรุ่นนี้กับฐานข้อมูลเดิม ไม่ต้องสร้างฐานข้อมูลใหม่ และควรใช้ชื่อ Compose project เดิม `kku-foodshare-phase1`

## ไฟล์ที่เปลี่ยน/เพิ่ม

เส้นทางต่อไปนี้อยู่ใน `code/src/`:

- `main/java/com/kku/foodshare/domain/entity/PostComment.java`
- `main/java/com/kku/foodshare/repository/PostCommentRepository.java`
- `main/java/com/kku/foodshare/service/CommentService.java`
- `main/java/com/kku/foodshare/service/impl/CommentServiceImpl.java`
- `main/java/com/kku/foodshare/controller/api/CommentController.java`
- `main/resources/db/migration/V9__comment_replies.sql`
- `main/resources/static/js/comments.mjs` (เพิ่ม)
- `main/resources/static/js/app.js`, `ui.js`
- `main/resources/static/js/feed-options.mjs` (ลบฟังก์ชันแสดงต่อหน้า)
- `main/resources/static/css/feed-polish.css`
- `main/resources/templates/fragments.html`, `home.html`, `explore.html`, `dashboard.html`
- `test/java/com/kku/foodshare/service/CommentRepliesTest.java` (เพิ่ม 7 tests)
- `test/java/com/kku/foodshare/service/CommentRepliesPersistenceTest.java` (เพิ่ม persistence regression)
- `test/java/com/kku/foodshare/WebPagesTest.java`
- `test/js/comments.test.mjs` (เพิ่ม 13 tests)
- `test/js/feed-options.test.mjs`, `guest-home.test.mjs`, `social-feed.test.mjs`

เอกสารนี้เป็นสเปกล่าสุดของการ์ด/คอมเมนต์ แทนคำแนะนำเรื่อง dropdown และจำกัดความกว้างในการส่งมอบ Mobile Feed ก่อนหน้า

## ผลตรวจจริงในสภาพแวดล้อมส่งมอบ

- `node --check src/main/resources/static/js/app.js`: ผ่าน
- `node --check src/main/resources/static/js/ui.js`: ผ่าน
- `node --check src/main/resources/static/js/comments.mjs`: ผ่าน
- `node --test src/test/js/*.test.mjs`: ผ่าน 88 tests, fail 0 รวม regression แผนที่/routes/GPS/QR/stock/reservation และตัวดูรูปเดิม
- ตรวจ templates ที่เปลี่ยนด้วย HTML parser: ผ่าน ไม่มี id ซ้ำ และไม่มี page-size selector
- `mvn -o -B test`: รันแล้วแต่หยุดก่อน compile เพราะไม่ได้ cache `spring-boot-starter-parent:4.1.1` ในเครื่องนี้
- `mvn -o -B -DskipTests package`: รันแล้วติด dependency เดียวกัน
- จึงยังไม่ยืนยันว่า Java tests/build ผ่าน และยังไม่ได้ตรวจหน้าจอเว็บจริงด้วย browser ที่รันแอป Spring Boot

ไม่เปลี่ยน POM หรือเวอร์ชัน dependencies เพื่อเลี่ยงปัญหาการตรวจ ให้รัน Java tests ในเครื่องที่มี dependency เดิมก่อนใช้งานจริง

## เปิดบน Windows PowerShell

แตก ZIP ลง `Principle` ตั้งชื่อโฟลเดอร์ `KKU-FoodShare-Phase8-Comments-Replies` ภายในต้องมี `kku-foodshare` ชั้นแรก คัดลอกเฉพาะคำสั่งในกล่อง ไม่คัดลอกข้อความ `PS C:\...>`

```powershell
cd "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\Principle\KKU-FoodShare-Phase8-Comments-Replies\kku-foodshare"

if (-not (Test-Path -LiteralPath ".\.env")) {
    $previousEnv = "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\Principle\KKU-FoodShare-Phase8-Mobile-Feed-Polished\kku-foodshare\.env"
    if (-not (Test-Path -LiteralPath $previousEnv)) {
        throw "ไม่พบ .env เดิม ให้แก้ previousEnv เป็นโฟลเดอร์ที่รันได้ล่าสุดก่อน"
    }
    Copy-Item -LiteralPath $previousEnv -Destination ".\.env"
}

docker compose -p kku-foodshare-phase1 up --build -d
docker compose -p kku-foodshare-phase1 ps
```

ถ้าโฟลเดอร์มี `(1)` หรือเลขอื่น ให้ใส่ชื่อจริงใน path ภายในเครื่องหมายคำพูด ถ้าสำเนาที่ใช้ได้อยู่คนละชื่อ ให้เปลี่ยน `$previousEnv` เป็น `.env` ของสำเนานั้น ไม่ต้องส่งค่า secret มาในแชต และไม่ต้องคัดลอก `.env.example` แทน secrets ของฐานข้อมูลเดิม

เปิด `http://127.0.0.1:8081` หรือ `http://localhost:8081` รอ app เป็น `healthy`; หลังอัปเดตกด `Ctrl+F5` หนึ่งครั้ง

ถ้ารอบก่อนเปิด Mailpit สำหรับทดสอบลืมรหัสผ่าน ให้ใช้คำสั่งรวมไฟล์เดิมนี้แทนคำสั่ง up ข้างบน:

```powershell
docker compose -p kku-foodshare-phase1 -f compose.yaml -f compose.mail-local.yaml up --build -d
docker compose -p kku-foodshare-phase1 -f compose.yaml -f compose.mail-local.yaml ps
```

อ่านกล่องเมลทดสอบที่ `http://127.0.0.1:8025` อย่าใช้ `down -v` เพราะจะลบ volume ฐานข้อมูลเดิม

## ทดสอบก่อนใช้จริง

จากโฟลเดอร์ `kku-foodshare`:

```powershell
cd .\code
.\mvnw.cmd -o -B test
node --check src/main/resources/static/js/app.js
node --test src/test/js/*.test.mjs
.\mvnw.cmd -o -B -DskipTests package
cd ..
```

หากเครื่องยังไม่ได้โหลด Maven dependencies ต้องเชื่อมต่อและรัน wrapper โดยเอา `-o` ออกหนึ่งครั้ง แล้วกลับมารันคำสั่ง offline เดิม

ตรวจด้วยสองบัญชีแยก browser profile หรือหน้าต่างปกติ+Incognito (สองแท็บใน profile เดียวแชร์ session):

1. ผู้แบ่งปันสร้างโพสต์หลายรูป; เปิด feed ที่กว้าง 320/390/768px ดูว่าการ์ดไม่ล้น ภาพสูงเท่ากันใน grid และเมนูมือถืออยู่ล่าง
2. กดรูป ตรวจปิด/เปิด/เปลี่ยนรูปเหมือนเดิม กดคอมเมนต์ ตรวจ dialog และปิดกลับตำแหน่งเดิม
3. ผู้รับคอมเมนต์ใหม่ ผู้แบ่งปันควรเห็นแจ้งเตือนพร้อมรูป/ชื่อผู้รับ และกดแล้วเปิดคอมเมนต์ของโพสต์นั้น
4. ผู้แบ่งปันตอบผู้รับ ผู้รับควรได้แจ้งเตือนตอบกลับ; ตอบต่อในคำตอบแล้วตรวจว่าไม่เยื้องหลายชั้น
5. ลบต้นทางแล้วตรวจว่าคำตอบยังอ่านได้ ลบ/รายงานจากเมนูจุดสามจุด ตรวจสิทธิ์ทั้งผู้เขียน/เจ้าของ/admin
6. ทดสอบแผนที่/ตำแหน่ง/Google Maps, จอง/ปรับจำนวน/stock/QR/รหัส 6 หลักตามเดิม

ZIP ไม่มี `.env`, target, uploads, node_modules หรือไฟล์ build
