# Phase 8 — About page and landing polish

## สิ่งที่ปรับ

- เปลี่ยนเมนู “วิธีใช้งาน” เป็น “เกี่ยวกับเรา” และสร้างหน้า `/about` แยกจากหน้าแรก
- หน้า About อธิบายแนวคิดของ KKU FoodShare จุดเด่น และทางลัดไปค้นหา/แบ่งปันอาหาร
- คงส่วนขั้นตอนการใช้งานไว้บนหน้าแรก และเชื่อมจากหน้า About กลับไปยังขั้นตอนดังกล่าว
- จัดแบนเนอร์แบ่งปันอาหารใหม่ให้ใช้ภาพถ่ายชุมชนแทนภาพมาสคอต พร้อมปรับการวางภาพบนมือถือ
- เพิ่มเมนู About ในแถบนำทางมือถือ โดยรักษาปุ่มค้นหา แบ่งปัน การจอง และโปรไฟล์
- เปลี่ยนหัวข้อทั่วไปด้านบนของหน้าต่างความคิดเห็นเป็น “ความคิดเห็น”
- เพิ่มการทดสอบว่า controller ส่งหน้า About template ได้

## ไฟล์ที่เปลี่ยน

- `code/src/main/java/com/kku/foodshare/controller/web/HomeController.java`
- `code/src/main/java/com/kku/foodshare/config/SecurityConfig.java` (อนุญาต GET `/about` ให้เปิดดูได้โดยไม่เข้าสู่ระบบ)
- `code/src/main/resources/templates/about.html` (เพิ่ม)
- `code/src/main/resources/templates/home.html`
- `code/src/main/resources/templates/fragments.html`
- `code/src/main/resources/static/css/about.css` (เพิ่ม)
- `code/src/main/resources/static/css/app.css`
- `code/src/main/resources/static/css/welcome.css`
- `code/src/main/resources/static/js/comments.mjs`
- `code/src/main/resources/static/images/foodshare-community-photo.png` (เพิ่ม ภาพสร้างใหม่)
- `code/src/test/java/com/kku/foodshare/controller/web/HomeControllerTest.java`

## Build และรัน

จากโฟลเดอร์ `kku-foodshare`:

```powershell
docker compose -p kku-foodshare-phase1 up --build -d
docker compose -p kku-foodshare-phase1 ps
```

หน้า About อยู่ที่ `http://localhost:8081/about` เมื่อ Compose map พอร์ต 8081 ไว้

ZIP สำหรับส่งมอบไม่รวมไฟล์ `.env`; ใช้ `.env` เดิมของผู้ใช้ในโฟลเดอร์ `kku-foodshare` ก่อนรัน Compose
