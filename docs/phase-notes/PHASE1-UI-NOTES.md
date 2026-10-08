# KKU FoodShare — Phase 1 UI

รอบนี้ปรับเฉพาะประสบการณ์ค้นหาอาหารและหน้า Home โดยใช้ API และโครงสร้างระบบเดิม

## สิ่งที่เพิ่ม

- Live Food Map บนหน้า Home พร้อมหมุดจากรายการที่เปิดรับจริง
- ปุ่มตำแหน่งของฉันและหมุดผู้ใช้ โดยยังใช้งานแผนที่ต่อได้เมื่อไม่อนุญาตตำแหน่ง
- การ์ดตัวอย่างแบบ bottom sheet เมื่อเลือกหมุด
- ตัวกรองอาหาร เครื่องดื่ม ของว่าง และรายการที่รับได้ตอนนี้
- การเรียงตามหมดเวลารับก่อน โพสต์ล่าสุด และใกล้ฉัน
- First-time onboarding ที่ชี้ไปยังส่วนควบคุมจริง
- Responsive layout สำหรับมือถือ 360–390px แท็บเล็ต และเดสก์ท็อป

## ขอบเขตที่รักษาไว้

ไม่มีการแก้ authentication, reservation, stock/concurrency, security, database migration, deployment configuration หรือ API/backend ใหม่

## ไฟล์หลักที่เปลี่ยน

- `code/src/main/resources/templates/home.html`
- `code/src/main/resources/static/css/app.css`
- `code/src/main/resources/static/js/app.js`
- `code/src/main/resources/static/js/maps.js`
- `test/browser-journey.cjs`

แก้ไขเพิ่มเติม: ซ่อมแท็กปิดลิงก์ในหน้า `account.html` ที่ทำให้หน้าโปรไฟล์ตอบกลับเป็น HTTP 500 และเพิ่ม regression test สำหรับการเปิดหน้า `/account` หลังเข้าสู่ระบบ

## เปิดใช้งาน

```powershell
cd "C:\Users\CHOMP2O\OneDrive\เดสก์ท็อป\1\kku-foodshare\kku-foodshare"
docker compose up --build -d
docker compose ps
```

จากนั้นเปิด URL ของระบบเดิม หากต้องการเปิดคำแนะนำการใช้งานอีกครั้งให้เติม `?guide` ต่อท้าย URL หน้า Home
