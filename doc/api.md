# API contract

Base path `/api/v1`. JSON UTF-8. ใช้ session cookie เดียวกับการเข้าสู่ระบบหน้าเว็บ ทุก POST/PUT/PATCH/DELETE ต้องส่ง CSRF token ของ session ผ่าน header ตามค่า `meta[name=_csrf_header]` และ `meta[name=_csrf]` ใน HTML ห้ามฝัง token คงที่

| Method | Path | สิทธิ์ / ผลลัพธ์ |
|---|---|---|
| GET | `/food-posts` | สาธารณะ, หน้าอาหารที่ยังมีจำนวนและยังไม่หมดเวลา |
| GET | `/food-posts/map` | สาธารณะ, q/category/now, สูงสุด 200 จุด |
| GET | `/food-posts/{id}` | สาธารณะ; โพสต์ปิดแสดงเฉพาะเจ้าของ |
| POST | `/food-posts` | สมาชิก, สร้างโพสต์ → 201 |
| PUT | `/food-posts/{id}` | เจ้าของ, แก้ไขทั้งหมด |
| DELETE | `/food-posts/{id}` | เจ้าของ, ปิดโพสต์และยกเลิกการจองค้าง → 204 |
| POST | `/food-posts/{id}/images` | เจ้าของ, multipart field `file` JPG/PNG ≤5MB |
| GET | `/me/posts` | โพสต์ของสมาชิกปัจจุบัน |
| POST | `/food-posts/{id}/reservations` | ผู้รับ, `{quantity:2}` + `Idempotency-Key` UUID → 201 |
| GET | `/food-posts/{id}/reservations` | เจ้าของ, รายการผู้จอง ไม่มี pickupCode |
| GET | `/reservations/{id}` | ผู้รับหรือเจ้าของ |
| PUT | `/reservations/{id}` | ผู้รับ, `{quantity:3}` |
| DELETE | `/reservations/{id}` | ผู้รับหรือเจ้าของ, ยกเลิก → 204 |
| POST | `/reservations/{id}/collection` | เจ้าของ, `{code:"123456"}` |
| GET | `/me/reservations` | การจองของผู้รับปัจจุบัน |
| GET | `/me/notifications` | การแจ้งเตือนของสมาชิกปัจจุบัน |
| POST | `/me/notifications/{id}/read` | ทำเครื่องหมายอ่านแล้ว |
| PATCH | `/me/profile` | `{name:"ชื่อใหม่"}` |
| POST | `/reports` | `{postId:1, reason:"รายละเอียดปัญหา"}` |
| GET | `/admin/reports` | ผู้ดูแล, รายงานทุกสถานะ |
| PATCH | `/admin/reports/{id}` | `{reason:"ผลตรวจ",closePost:true}` |
| GET | `/admin/users` | ผู้ดูแล, สมาชิกแบบแบ่งหน้า |
| PATCH | `/admin/users/{id}` | `{active:false,reason:"เหตุผล"}` |
| GET | `/stats` | จำนวนสมาชิก/โพสต์/อาหารที่รับแล้วจากฐานข้อมูล |

ค้นหา `/food-posts?q=ข้าว&category=FOOD&sort=expiry&now=true&page=0&size=12` ประเภท `FOOD,DRINK,SNACK` ลำดับ `expiry,latest,nearby`; nearby ต้องส่ง `lat,lng` หน้านับจาก 0; size สูงสุด 200

```json
{
  "title": "ข้าวกล่องจากงานประชุม",
  "description": "ระบุเวลาปรุงและการเก็บรักษา",
  "category": "FOOD",
  "quantity": 5,
  "unit": "กล่อง",
  "pickupLocationName": "โต๊ะหน้าหอสมุด",
  "latitude": 16.4745,
  "longitude": 102.8237,
  "availableFrom": "2026-10-06T12:00:00",
  "availableUntil": "2026-10-06T14:00:00",
  "allergens": "ไข่"
}
```

ตัวอย่างเวลาเป็นรูปแบบข้อมูล ให้ใช้เวลาในอนาคตที่เหมาะกับการใช้งานจริง ทุกเวลานัดรับเป็น Bangkok local time, ไม่มี offset ใน JSON; token reset ใช้ Instant UTC ภายใน

Page response: `{items:[],page:0,totalPages:1,totalElements:3}`. Post response มี `quantity,reservedQuantity,collectedQuantity,availableQuantity,imageUrl,mine,distanceKm,status` เพิ่มจากข้อมูลกรอก

Error response: `{status:400,message:"…",fields:{quantity:"…"}}`. ความหมายหลัก: 400 validation, 401 ต้องเข้าสู่ระบบ, 403 ไม่มีสิทธิ์/CSRF, 404 ไม่พบ, 409 สถานะเปลี่ยน/จำนวนไม่พอ/ข้อมูลชนกัน, 413 ไฟล์ใหญ่เกิน, 429 จำกัดการลองรหัส

เก็บ idempotency key เดิมเมื่อ retry คำขอจองเดิมเท่านั้น การเปลี่ยน post/quantity โดยใช้ key เดิมตอบ 409 การรับซ้ำหรือยกเลิกซ้ำไม่เปลี่ยนจำนวนซ้ำ รหัสรับแสดงเฉพาะเจ้าของการจองที่ยัง RESERVED; รหัสผิด 5 ครั้งล็อก 15 นาที

Endpoint รูปโปรไฟล์อยู่นอก API: `POST /account/photo` multipart `photo` และ `GET /account/photo` ใช้สมาชิกและ CSRF เหมือนฟอร์มทั่วไป
