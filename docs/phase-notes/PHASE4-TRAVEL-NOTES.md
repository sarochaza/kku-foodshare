# Phase 4 — เลือกเดินเท้า / ขับรถ และตรวจตำแหน่งผู้รับ

ต่อจาก Phase 3 ที่ผู้ใช้ยืนยันว่าค้นหาสถานที่และหมุดโพสต์ทำงานแล้ว

## การใช้งาน

- หน้าแรกคงแผนที่เดิมเหนือรายการโพสต์ เพิ่มหัวข้อและคำอธิบายหมุด กดตำแหน่งของฉันเพื่อแสดงหมุดผู้ใช้ เลือกหมุดอาหารแล้วเปิดรายละเอียด
- หน้ารายละเอียดมีแผนที่จุดรับและจุดเริ่มต้นของผู้รับ พร้อมตัวเลือก **เดินเท้า / ขับรถ**
- กด **ใช้ตำแหน่งปัจจุบัน** ตรวจหมุดสีน้ำเงินและค่าความคลาดเคลื่อน ถ้าไม่ตรงให้แตะบนแผนที่หรือลากหมุดแก้จุดเริ่มต้น แล้วกด **ยืนยันจุดเริ่มต้น**
- เว็บแสดงเส้นทาง ระยะทางตามเส้นทาง และเวลาเดินทางโดยประมาณ ก่อนตัดสินใจจอง หากคำนวณว่าอาจถึงหลังเวลาปิดรับ จะแจ้งเตือนโดยไม่ตัดสินแทนผู้ใช้
- หากกดจองก่อนตรวจตำแหน่ง เว็บจะขอตำแหน่งและพาไปส่วนยืนยันก่อน ต้องกดจองอีกครั้งหลังยืนยัน
- หากไม่อนุญาตตำแหน่ง สามารถเลือกจุดเริ่มต้นเอง หรือกด **จองต่อโดยไม่ตรวจตำแหน่ง** แล้วกดจอง ระบบจองเดิมยังใช้งานได้
- หน้า **การจองของฉัน** ปุ่ม **เส้นทาง / เวลาเดินทาง** เปิดส่วนตรวจตำแหน่งใหม่ ไม่ใช้ตำแหน่งเก่าตอนจอง
- Google Maps ได้รับทั้ง origin, destination และ travelmode ที่เลือก หมุดจุดเริ่มต้นแก้เองถือเป็นจุดที่ผู้ใช้ระบุ ไม่รีเฟรช GPS ทับ
- พิกัด GPS ที่ยืนยันไว้ใช้ได้ไม่เกิน 1 นาที ก่อนจอง/เปิดนำทาง ถ้าเก่าเว็บขอตรวจใหม่และให้ยืนยันอีกครั้ง ไม่เก็บพิกัดผู้รับลงฐานข้อมูล/localStorage

## การคำนวณ

ใช้บริการสาธารณะ FOSSGIS / OSRM ข้อมูล OpenStreetMap โดยเรียกจาก frontend ไม่มี API key หรือ backend ใหม่:

- เดินเท้า: `https://routing.openstreetmap.de/routed-foot/route/v1/driving/…`
- ขับรถ: `https://routing.openstreetmap.de/routed-car/route/v1/driving/…`

คำว่า driving ใน path เป็นรูปแบบ OSRM; โปรไฟล์ที่ใช้จริงถูกเลือกด้วย routed-foot หรือ routed-car ห้ามเปลี่ยนเพียง travelmode แต่ใช้เส้นทางขับรถสำหรับเดินเท้า

ระยะทางใช้ค่า distance หน่วยเมตร เวลาใช้ duration วินาทีปัดขึ้นเป็นนาที วาด polyline จาก route geometry ไม่มีข้อมูลจราจรสด/เวลารอ/ความเร็วเฉพาะบุคคล จึงติดป้ายว่าเวลาโดยประมาณ

จำกัดการเริ่มคำขอไม่ถี่กว่า 1.1 วินาทีต่อหน้า รวมทุก widget มี cache 5 นาที สูงสุด 30 เส้นทาง timeout 10 วินาที ยกเลิก/เพิกเฉยคำตอบเก่าเมื่อเปลี่ยนหมุดหรือโหมด ไม่คำนวณเส้นทางให้ทุกโพสต์อัตโนมัติ

หากบริการไม่พร้อมหรือไม่มีเส้นทาง จะแสดง **ระยะทางเส้นตรง** พร้อมคำอธิบายว่าไม่ใช่ระยะเดิน/ขับรถ และไม่แสดงเวลาเดินทางที่เดาขึ้นมา การจองยังใช้ได้

พิกัดเริ่มต้นและจุดรับส่งไปผู้ให้บริการเส้นทาง; ไม่มีข้อมูลบัญชี รหัส QR หรือข้อมูลการจองส่งไป บริการสาธารณะไม่มี SLA และจำกัดโหลด เหมาะกับโปรเจกต์/เดโมตาม usage policy หากขยายใช้งานจริงต้องเลือกบริการที่รองรับโหลด การสลับ OSRM กับ Google Maps อาจได้เส้นทางและเวลาแตกต่างกันจากข้อมูล/อัลกอริทึม

อ้างอิง:
- https://routing.openstreetmap.de/about.html
- https://project-osrm.org/docs/v5.24.0/api/
- https://developers.google.com/maps/documentation/urls/get-started

GPS คอมพิวเตอร์อาจคลาดเคลื่อนมาก แม้ขอ high accuracy และพิกัดใหม่ จึงต้องตรวจและยืนยันหมุดเอง โทรศัพท์ผ่าน HTTP LAN อาจไม่อนุญาต geolocation ต้องใช้ HTTPS ตามข้อจำกัด browser ส่วน localhost บนเครื่องเดียวกันใช้งานตาม browser

## ไฟล์รอบนี้

แก้:
- `code/src/main/resources/static/js/app.js`
- `code/src/main/resources/static/css/app.css`
- `code/src/main/resources/templates/home.html`
- `test/browser-journey.cjs`

เพิ่ม:
- `code/src/main/resources/static/js/routes.mjs`
- `code/src/main/resources/static/js/trip.js`
- `code/src/test/js/routes.test.mjs`
- `code/src/test/js/trip.test.mjs`
- `PHASE4-TRAVEL-NOTES.md`

ไม่แก้ Java/backend, API จอง/quantity/idempotency, stock, schema, Authentication, Security, QR, Owner Management หรือ Docker/Compose

## Build และฐานข้อมูลเดิม

แตก ZIP แล้วใช้โฟลเดอร์ `kku-foodshare` ภายใน คัดลอก `.env` เดิมจาก Phase 3 ที่รันสำเร็จไว้ข้าง compose.yaml ไม่สร้าง secrets/password ใหม่

```powershell
# ตัวอย่างตามโฟลเดอร์เดิมของผู้ใช้ ให้รันในโฟลเดอร์ kku-foodshare ของ Phase 4
Copy-Item "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\KKU-FoodShare-Phase3-Location\kku-foodshare\.env" ".\.env"
docker compose -p kku-foodshare-phase1 up --build -d
docker compose -p kku-foodshare-phase1 ps
```

เปิด http://localhost:8081 หรือ http://127.0.0.1:8081 ใช้ host เดียวต่อเนื่อง แล้ว Ctrl+F5 ใช้ Compose project เดิมเพื่อใช้ volumes เดิม ห้าม `down -v` / ลบ volumes ZIP ไม่รวม `.env`

## ผลการตรวจและข้อจำกัด

- `node --test code/src/test/js/*.test.mjs`: **ผ่าน 20/20** รวม Phase 3 + QR regression เดิม
- ตรวจ syntax app.js, trip.js, routes.mjs และ browser-journey.cjs: ผ่าน
- ทดสอบ routing profiles แยกเดิน/รถ, origin ใน Google URL, metre/seconds conversion, GPS ต้องยืนยัน, GPS หมดอายุ, การเลือกหมุดใหม่ทำให้ confirmation เดิมใช้ไม่ได้, offline fallback, ข้ามตำแหน่งเมื่อปฏิเสธ และ stale route response: ผ่าน unit/handler tests โดย mock เฉพาะ browser/network boundaries
- เพิ่ม browser journey ทดสอบ UI โหมดเดิน/รถ ระยะ/เวลา และ origin handoff ด้วย route fixture แล้วดำเนินการจองจริงต่อ แต่ **ยังไม่ได้รัน** เนื่องจากไม่มี Chromium binary
- ไม่มี Docker ในสภาพแวดล้อมนี้; backend regression ของ Maven ยังถูก DNS/dependency download block ตาม Phase 3 จึง **ยังยืนยัน Docker build/full backend regression ไม่ได้**
- การเรียก live routing ผ่านเครื่องมือตรวจเว็บไม่สำเร็จ จึง **ยังไม่ยืนยันบริการสด/CORS/เส้นทางจริงในพื้นที่ มข.** ไม่อ้างผล fixture เป็นผลเส้นทางจริง
- ยืนยันด้วยมือบนเครื่องจริง: เปิดโพสต์ > ขอพิกัด > ยืนยัน > ดูเดินเท้า > เปลี่ยนขับรถ > ตรวจว่า route/distance/time เปลี่ยนตามบริการ > ลากหมุด > ยืนยันใหม่ > กดจอง > เปิดเส้นทางจากการจอง > ขอพิกัดใหม่ > ตรวจ Google Maps origin; จากนั้นตรวจมือถือ, offline และ QR/Owner flow เดิม

ทดสอบอัตโนมัติจาก kku-foodshare:

```powershell
node --test code/src/test/js/*.test.mjs
cd code
.\mvnw.cmd test
cd ..\test
npm install
npx playwright install chromium
npm run browser
```

Browser journey สร้างบัญชี/อาหาร/การจองทดสอบ ให้ใช้ instance/DB ทดสอบแยกโดยตั้ง TEST_BASE_URL เท่านั้น ไม่รันกับฐานข้อมูลใช้งานจริง
