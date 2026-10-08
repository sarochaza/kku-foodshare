# Phase 4.1 — รายละเอียดแสดงระยะทางขับรถทันที / ค้นหาสถานที่เร็วขึ้น

ต่อจาก Phase 4 ที่ผู้ใช้ยืนยันว่าทำงานดีแล้ว ปรับเฉพาะ frontend ที่เกี่ยวข้อง

## สิ่งที่เปลี่ยน

- ขับรถเป็นค่าเริ่มต้นทั้งหน้ารายละเอียดและส่วนเส้นทางในหน้าการจอง ยังเลือกเดินเท้าได้
- เปิดหน้ารายละเอียดจะขอพิกัดปัจจุบันผ่าน browser แล้วคำนวณเส้นทางขับรถอัตโนมัติเมื่อได้พิกัด แสดงสรุประยะทาง/เวลาใกล้ปุ่มจอง และแสดงเส้นทางบนแผนที่ ไม่ต้องกดยืนยันก่อนดูประมาณการ
- ประมาณการก่อนยืนยันมีข้อความ **ยังไม่ได้ยืนยันจุดเริ่มต้น** ไม่ถือว่าหมุด GPS ถูกต้องแน่นอน ต้องตรวจ/ลากแก้/ยืนยันก่อนจองหรือเปิดนำทาง หรือเลือกข้ามการตรวจตำแหน่ง
- ถ้าไม่ให้ตำแหน่งจะแจ้งวิธีเลือกจุดเอง ไม่แสดงระยะหรือเวลาปลอม
- เปลี่ยนหมุด/วิธีเดินทางแล้วคำนวณ preview ใหม่ ยังคงแยกเส้นทางเดินและรถ/อายุพิกัดตาม Phase 4
- ค้นหาสถานที่หน่วง **400 ms** (เดิม 650 ms) แคชคำค้นตรงกันในหน่วยความจำหน้าเว็บ 5 นาที สูงสุด 50 คำ คำค้นซ้ำแสดงทันทีโดยไม่ต้องส่ง network request ใหม่
- เพิ่มปุ่มค้นหาและกด Enter เพื่อเริ่มคำขอทันทีเมื่อยังไม่มีผลลัพธ์ หากมีรายการแล้ว Enter เลือกรายการแรก
- รายการแนะนำแบ่งชื่อสถานที่ตัวหนาและพื้นที่ย่อยให้อ่านคล้าย autocomplete ในภาพ รองรับคำตอบเก่า/การยกเลิกคำขอเช่นเดิม

ความเร็วปรับได้เฉพาะฝั่งเว็บและการใช้ cache คำค้นครั้งแรกยังขึ้นกับ Photon/เครือข่าย ข้อมูลและความสามารถค้นหาของ OSM ไม่เท่ากับ Google Maps การปรับนี้ไม่ได้เปลี่ยนไปใช้ Google Places ไม่มี Google API key/Billing ไม่รับประกันชื่อสถานที่ครบหรือความเร็วเท่ากับ Google Maps เวลาเดินทางยังประมาณจาก OSRM ไม่มีจราจรสดตาม Phase 4

## ไฟล์ที่แก้ในรอบนี้

- code/src/main/resources/static/js/app.js
- code/src/main/resources/static/js/trip.js
- code/src/main/resources/static/js/routes.mjs
- code/src/main/resources/static/js/places.mjs
- code/src/main/resources/templates/editor.html
- code/src/main/resources/static/css/app.css
- code/src/test/js/location.test.mjs
- code/src/test/js/trip.test.mjs
- test/browser-journey.cjs
- QUICK-PREVIEW-NOTES.md (ใหม่)

ไม่มีการแก้ backend, Authentication, Reservation API/stock, schema, QR, Owner Management หรือ Deployment

## ผลตรวจ

JavaScript regression รวมเดิม **ผ่าน 22/22** โดยเพิ่มทดสอบ driving default/auto preview และ exact query cache ผ่านการตรวจ syntax ของ JS ที่แก้และ browser journey

Browser journey เพิ่มการตรวจ driving default และสรุปเวลาในหน้ารายละเอียดแล้ว แต่ไม่ได้รันเบราว์เซอร์จริงในสภาพแวดล้อมส่งมอบ (ไม่มี Chromium/Docker; Maven dependency ถูก DNS block ตามบันทึกก่อน) การทดสอบอัตโนมัติใช้ network fixtures ไม่ใช่หลักฐาน latency หรือความครบถ้วน live service ไม่ได้ยืนยันบริการสดเร็วขึ้นตามจำนวนมิลลิวินาทีจริง

## อัปเดตและรัน

โปรเจกต์อยู่ใน kku-foodshare ภายใน ZIP คัดลอก `.env` จาก Phase 4 ที่ใช้อยู่มาวางข้าง compose.yaml ในรุ่นนี้ ใช้ชื่อ Compose project เดิมเพื่อใช้ volumes เดิม อย่า `down -v` หรือลบ volumes

```powershell
# รันจาก kku-foodshare ของรุ่นนี้
Copy-Item "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\KKU-FoodShare-Phase4-Travel\kku-foodshare\.env" ".\.env"
docker compose -p kku-foodshare-phase1 up --build -d
docker compose -p kku-foodshare-phase1 ps
```

เปิด http://127.0.0.1:8081 แล้ว Ctrl+F5 ZIP ไม่รวม `.env`

ทดสอบจาก kku-foodshare:

```powershell
node --test code/src/test/js/*.test.mjs
cd code
.\mvnw.cmd test
```

ตรวจจริงบนเครื่อง: เปิดดูรายละเอียด > อนุญาตตำแหน่ง > เห็น “ขับรถ … กม. … นาที” ใกล้ปุ่มจอง > เปลี่ยนเดินเท้า > แก้หมุด/ยืนยัน > จอง; สร้าง/แก้โพสต์ค้นหาคำเดิมสองครั้งเพื่อทดสอบ cache, กด Enter ค้นหาโดยไม่รอ, ค้นหาไม่เจอ/เน็ตขาด/ปฏิเสธตำแหน่ง และตรวจ QR/Owner เดิม
