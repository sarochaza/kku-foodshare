# KKU FoodShare – Phase 7 Profile & Smart Search

## สิ่งที่เพิ่ม

- เมนูโปรไฟล์แบบย่อจากรูปบัญชีบนส่วนหัว: การจองของฉัน, โพสต์ของฉัน, การแจ้งเตือน, ตั้งค่าโปรไฟล์ และออกจากระบบ
- เมนูปิดได้ด้วยการกด Escape หรือคลิกนอกเมนู และยังคงหน้า `/account` เดิมไว้สำหรับแก้ไขโปรไฟล์เต็มรูปแบบ
- ค้นหาจุดรับแบบ local-first: คำที่พบบ่อยของ มข./ขอนแก่น เช่น `หอสมุด`, `library`, `complex`, `มข.` แสดงทันทีจากรายการในเว็บ
- ปุ่ม **ค้นหาเพิ่ม** เป็นการค้นหา Photon/OpenStreetMap แบบ explicit เมื่อสถานที่อยู่นอกรายการท้องถิ่น จึงไม่ยิงคำขอทุกตัวอักษร
- ยังคงเลือกผลลัพธ์เพื่อปักหมุด, แก้ชื่อจุดรับ, ใช้ตำแหน่งปัจจุบัน, คลิก/ลากหมุด และกรอกพิกัดขั้นสูงได้เหมือนเดิม

## ข้อจำกัดและการ deploy

- ไม่ต้องมี Google API key, Billing หรือ environment variable เพิ่ม
- การค้นหาในรายการ มข./ขอนแก่นทำงานได้แม้บริการภายนอกช้าหรือใช้งานไม่ได้
- ถ้าการค้นหาเพิ่มหรือ reverse geocoding ใช้งานไม่ได้ ผู้ใช้ยังปักหมุด/กรอกพิกัดขั้นสูงและบันทึกโพสต์ได้
- ไม่มีการแก้ Authentication, Reservation, Stock, Database schema หรือ Docker Compose

## ทดสอบแล้ว

- Maven regression tests: 60 tests ผ่าน
- JavaScript tests: 8 files ผ่าน
- ตรวจ syntax ของ `app.js`, `places.mjs`, และ `profile-menu.mjs` ผ่าน

## วิธีรันบน Windows

แตก ZIP แล้วเปิด PowerShell ไปยังโฟลเดอร์ `kku-foodshare` ที่มีไฟล์ `compose.yaml` จากนั้นคัดลอก `.env` เดิม (ห้ามใส่ `.env` ลง ZIP) แล้วรัน:

```powershell
Copy-Item "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\KKU-FoodShare-Phase6-FairBooking-Extension\kku-foodshare\.env" ".\.env"
docker compose -p kku-foodshare-phase1 up --build -d
docker compose -p kku-foodshare-phase1 ps
```

เปิด [http://127.0.0.1:8081](http://127.0.0.1:8081) หรือ [http://localhost:8081](http://localhost:8081)
