# Phase 3 — Location Search / Place Picker

ต่อจาก `KKU-FoodShare-Phase2-Owner.zip` โดยตรง โปรเจกต์อยู่ใน `kku-foodshare/code`

## สิ่งที่เปลี่ยน

- ฟอร์มสร้างและแก้ไขโพสต์ใช้ช่อง “ค้นหาอาคาร จุดรับ หรือชื่อสถานที่” พิมพ์ 3 ตัวอักษรขึ้นไป หน่วง 650 ms จำกัด 5 ผลลัพธ์
- ใช้ Photon (ข้อมูล OpenStreetMap) จำกัดประเทศ TH และ bias รอบ มข. จากนั้นเรียงผลลัพธ์ที่ได้รับตามระยะจาก มข. ไม่ต้องมี API key
- เลือกผลลัพธ์แล้วเลื่อนแผนที่ วางหมุด เติมชื่อจุดรับ และเก็บ latitude/longitude ใน hidden input ตาม contract เดิม
- คลิกแผนที่ ลากหมุด และใช้ตำแหน่งปัจจุบันได้ Reverse geocoding แสดงชื่อพื้นที่โดยประมาณ โดยไม่ทับคำแนะนำจุดรับที่ผู้ใช้เขียนเอง
- คำตอบจากคำค้น/Reverse เก่าถูกยกเลิกหรือเพิกเฉย ป้องกันตำแหน่งและชื่อเก่าทับจุดใหม่
- ตัวเลือกขั้นสูงยังกรอกพิกัดได้เมื่อค้นหา/แผนที่ไม่พร้อม ต้องกด “แสดงหมุดตามพิกัด” เพื่อใช้ค่าที่กรอก ไม่มีชื่อ field เพิ่มใน payload
- หน้าแก้ไขโหลดชื่อจุดรับและพิกัดเดิม ปักหมุดเดิมโดยไม่ค้นหาใหม่ทับข้อมูล
- ตรวจชื่อจุดรับ พิกัดที่ไม่ว่าง เป็น finite number และอยู่ในช่วงที่ถูกต้อง ก่อนส่ง หากไม่ได้เลือกจุด จะเลื่อนกลับและแจ้งภาษาไทย
- มือถือใช้ช่อง/ปุ่มขนาดอย่างน้อย 48px รายการเลื่อนได้ สูงไม่เกิน 32dvh ไม่ซ้อนทับแผนที่ เลื่อนช่องค้นหามาใกล้ส่วนบนเมื่อผลลัพธ์มา และปิดคีย์บอร์ดเมื่อเลือก รองรับ Arrow/Enter/Escape
- แก้ช่องค้นหาอาหาร `/explore` และ dashboard ให้ไอคอน/ข้อความอยู่แถวเดียวกัน (global label CSS เดิมทำให้เรียงแนวตั้ง)
- หน้าแรกคงแผนที่อาหารเดิม ปุ่ม “ใกล้ฉัน” ขอพิกัดใหม่ (`maximumAge: 0`, high accuracy) แสดงหมุดผู้ใช้แยกจากอาหาร พร้อมวงความคลาดเคลื่อนและ tooltip
- หน้า explore เพิ่มหมุดผู้ใช้เมื่อกด “ใกล้ฉัน” และเปิดแผนที่ไปยังตำแหน่งนั้น

## บริการค้นหาและข้อจำกัดตำแหน่ง

Photon รองรับ autocomplete ต่างจาก Nominatim สาธารณะที่ห้าม autocomplete:
- https://github.com/komoot/photon/blob/master/docs/api-v1.md
- https://operations.osmfoundation.org/policies/nominatim/

คำค้นและพิกัด reverse ถูกส่งไป Photon ผ่าน HTTPS โดยตรงจาก browser มี timeout 8 วินาที ไม่มี backend/API ใหม่ ไม่มีการส่งข้อมูลจองหรือบัญชีไปบริการนี้ ชื่อสถานที่ใช้ชื่อท้องถิ่นจาก OSM พร้อม `Accept-Language: th` แต่ความครบถ้วนและภาษาไทยขึ้นกับข้อมูล/การรองรับของบริการ ไม่รับประกันว่าจะค้นพบทุกอาคาร บริการสาธารณะไม่มี SLA; ใช้หมุดหรือพิกัดขั้นสูงได้เสมอ หากบริการปิด/มี CORS error จะแสดง fallback

พิกัดผู้ใช้มาจากอุปกรณ์ ไม่ได้ใช้ตำแหน่งในโปรไฟล์หรือพิกัดผู้จองเป็นตำแหน่งปัจจุบัน ค่าคลาดเคลื่อนขึ้นกับ GPS/Wi-Fi/browser: `enableHighAccuracy` เป็นคำขอ ไม่รับประกันความแม่นยำ แสดงวงความคลาดเคลื่อนให้เห็น บนมือถือผ่าน IP LAN ที่เป็น HTTP browser อาจไม่ให้ geolocation ต้องใช้ HTTPS; localhost บนเครื่องเดิมใช้ได้ตาม browser ไม่แก้ Security/Deployment เพื่อเลี่ยงข้อจำกัดนี้

## ไฟล์ที่แก้/เพิ่ม

1. `code/src/main/resources/templates/editor.html`
2. `code/src/main/resources/static/js/app.js`
3. `code/src/main/resources/static/js/maps.js`
4. `code/src/main/resources/static/js/places.mjs` (ใหม่)
5. `code/src/main/resources/static/css/app.css`
6. `code/src/test/js/location.test.mjs` (ใหม่)
7. `code/src/test/js/place-picker.test.mjs` (ใหม่)
8. `code/src/test/js/maps.test.mjs` (ใหม่)
9. `test/browser-journey.cjs` (ปรับ regression ให้ใช้ hidden input / พิกัดขั้นสูงใหม่ และเพิ่ม location journey)
10. `PHASE3-LOCATION-NOTES.md` (ไฟล์นี้)

ไม่มีการแก้ Java/backend, Authentication, Reservation, Stock, Security, schema, Docker/Compose หรือ QR Scanner / Owner Management

## Build โดยรักษาฐานข้อมูลเดิม

1. สำรองโฟลเดอร์โค้ดเดิม แล้วคัดลอกไฟล์ Phase 3 ทับโฟลเดอร์ `kku-foodshare` เดิม อย่าแตก ZIP เป็นโปรเจกต์อีกชุดสำหรับใช้งานจริง
2. เก็บ `.env` เดิมในตำแหน่งเดิม (ZIP นี้ไม่มี `.env`) โดยคงค่าเดิม รวมถึง APP_PORT=8081 ที่ใช้ในระบบคุณ
3. เปิด terminal ใน `kku-foodshare` ที่มี `compose.yaml` แล้วรัน:

```bash
docker compose -p kku-foodshare-phase1 up --build -d
docker compose -p kku-foodshare-phase1 ps
```

เปิด http://localhost:8081 แล้ว hard refresh (Ctrl+F5)

คง Compose project name เดิมเพื่อให้ใช้ volumes เดิม อย่าใช้ `down -v`, อย่าลบ volumes และอย่ารัน setup-env สร้าง secret/password ใหม่ทับ `.env` เดิม คำสั่ง build ด้านบนไม่ลบข้อมูลฐานข้อมูลและรูปที่อยู่ใน volumes เดิม

## ผลการตรวจในสภาพแวดล้อมส่งมอบ

- `node --test code/src/test/js/*.test.mjs`: **ผ่าน 12/12** รวม QR regression เดิม 3 รายการ
- `node --check`: ผ่าน app.js, maps.js, places.mjs และ browser-journey.cjs
- ตรวจเทียบไฟล์กับ ZIP ต้นฉบับ: เปลี่ยนเฉพาะ 10 ไฟล์ข้างต้น Backend/Compose/QR/Owner code ไม่เปลี่ยน
- `bash code/mvnw test -q` (รันจาก code): **ยังรันไม่สำเร็จ** เพราะ DNS ของ repo.maven.apache.org ใช้งานไม่ได้ ทำให้โหลด parent Spring Boot 4.1.1 ไม่ได้ ไม่ได้เปลี่ยนเวอร์ชัน dependency
- เครื่องนี้ไม่มี Docker และ browser binary ของ Playwright จึง **ยังไม่ได้ทดสอบ full backend regression, Docker build, UI จริง, persistence จริง, GPS จริง และ QR camera จริง** ผลเก่าใน test/verification.md หรือ test/junit-results.csv เป็นผลของเวอร์ชันก่อน ไม่ใช่หลักฐานว่า Phase 3 ผ่าน

## Acceptance criteria และสถานะ

| ข้อ | การตรวจ | สถานะ ณ ส่งมอบ |
|---|---|---|
| 1 ค้นหาและรายการแนะนำ | unit + picker handler test ใช้ response fixture | ผ่านอัตโนมัติ; live Photon ต้องตรวจบนเครื่องจริง |
| 2 เลือกสถานที่ย้ายหมุด | picker test ตรวจ setPin และ hidden values | ผ่าน handler test; Leaflet จริงรอ browser journey |
| 3 hidden Latitude/Longitude | handler test; template แยก input ขั้นสูงไม่มี name | ผ่าน |
| 4 คลิกแผนที่ | maps test เรียก Leaflet event callback | ผ่าน adapter test; browser จริงรอตรวจ |
| 5 ลากหมุด | maps test ตรวจ dragend callback | ผ่าน adapter test; browser จริงรอตรวจ |
| 6 ตำแหน่งปัจจุบัน | geolocation boundary + picker tests | ผ่านด้วย fixture; GPS จริงรอตรวจ |
| 7 สร้างโพสต์ | browser journey รวม API readback | เพิ่มแล้ว ยังไม่ได้รัน |
| 8 หมุดหน้าแรกถูกตำแหน่ง | map ใช้พิกัดโพสต์เดิม; browser journey | รอตรวจกับ DB จริง |
| 9 โหลดตำแหน่งเดิม | picker load test | ผ่าน handler test; API/browser รอตรวจ |
| 10 แก้ตำแหน่งและบันทึก | picker + browser journey ตรวจ API readback | handler ผ่าน; persistence รอตรวจ |
| 11 ค้นหาไม่ได้ใช้พิกัดขั้นสูง | offline/invalid coordinates tests | ผ่าน handler test |
| 12 Mobile | CSS รองรับ; browser journey 390px และ viewport loop | ยังไม่ได้รัน browser |
| 13 QR / Owner เดิม | QR unit tests 3 รายการ + ไฟล์ production เดิมไม่เปลี่ยน | unit ผ่าน; UI/camera รอตรวจ |
| 14 syntax | node --check | ผ่าน |
| 15 regression เดิม | JS tests ผ่าน; Maven และ browser ถูก environment block | ยังยืนยัน full regression ไม่ได้ |

## ทดสอบบนเครื่องที่มี dependency และ browser

จาก `kku-foodshare`:

```bash
node --test code/src/test/js/*.test.mjs
cd code
# Windows ใช้ .\mvnw.cmd test; Linux/macOS ใช้คำสั่งด้านล่าง
bash mvnw test
cd ../test
npm install
npx playwright install chromium
# ตั้ง TEST_BASE_URL ให้ชี้ instance ทดสอบ จากนั้น
npm run browser
```

`browser-journey.cjs` สร้างบัญชี/โพสต์ทดสอบและจองจริง: ใช้กับ instance/ฐานข้อมูลทดสอบแยกเท่านั้น ไม่ใช้กับฐานข้อมูลใช้งานจริง โดยค่า URL เริ่มต้นคือ http://localhost:8081; ระบุ TEST_BASE_URL หาก instance ทดสอบใช้พอร์ตอื่น ห้ามนำผล geocoder fixture มาอ้างว่า live service ผ่าน

ตรวจด้วยมือเพิ่มเติม: ค้นหา “มหาวิทยาลัยขอนแก่น”/“หอสมุด” โดยไม่ mock, เลือกผลลัพธ์แล้วสร้างโพสต์, ตรวจชื่อและหมุดหน้าแรก, เปิดแก้ไขแล้วลากหมุดบันทึก, ปิดเน็ตแล้วใช้พิกัดขั้นสูง, ปฏิเสธตำแหน่ง, ทดลองบนมือถือพร้อมคีย์บอร์ด และทดสอบ QR กล้อง/เลือกรูป/กรอกรหัส รวมถึงยืนยันส่งมอบใน Owner Management
