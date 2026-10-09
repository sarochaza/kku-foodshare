# KKU FoodShare — เอกสารอ้างอิง API ฉบับภาษาไทย

วันที่จัดทำ: 9 ตุลาคม 2026 อ้างอิงโค้ด Phase 12 ที่ใช้ตรวจในรอบนี้

เอกสารนี้อธิบาย API ที่มีอยู่จริงใน FoodShare ตัวอย่างใช้ประกอบการอธิบาย ไม่ใช่ผลตอบกลับที่บันทึกจากเว็บ Production ดูหลักฐานการทดสอบและส่วนที่ยังต้องตรวจเพิ่มเติมใน [ผลการทดสอบ](#ผลการทดสอบ)

## Base URL และรูปแบบข้อมูล

| สภาพแวดล้อม | Base URL ของ API |
| --- | --- |
| แอปในเครื่อง / พอร์ตเริ่มต้นของ Compose | `http://localhost:8080/api/v1` |
| Docker ในเครื่อง เมื่อกำหนด `APP_PORT=8081` | `http://localhost:8081/api/v1` |
| URL สาธารณะที่เจ้าของโปรเจกต์ระบุ | `https://kku-foodshare.onrender.com/api/v1` |

ใช้พอร์ตที่แสดงใน `docker compose ps` โดย Compose อ่านค่าจาก `APP_PORT` ซึ่งมีค่าเริ่มต้นเป็น 8080 รอบนี้ยังไม่ได้ตรวจยืนยันว่าเว็บสาธารณะกำลังรันโค้ด Commit ใด

- ID ของข้อมูลเป็นตัวเลขชนิด `long` / `Long` ใน Java
- ชื่อฟิลด์ JSON ทั้งคำขอและผลตอบกลับใช้ camelCase และข้อความใช้ UTF-8
- วันเวลาใน API ใช้รูปแบบ ISO ของเวลาท้องถิ่น เช่น `2026-10-10T12:00:00` เวลารับอาหารและนาฬิกาของแอปใช้เขตเวลา **Asia/Bangkok** ฟิลด์เหล่านี้เป็น `LocalDateTime` ไม่มี `Z` หรือค่า UTC offset จึงต้องตีความเป็นเวลาท้องถิ่นตามที่ระบบกำหนด
- คำขอที่ส่ง JSON ใช้ `Content-Type: application/json` ส่วนการอัปโหลดไฟล์ใช้ `multipart/form-data`
- ผลสำเร็จ `204` ไม่มีเนื้อหาในผลตอบกลับ ส่วนการสร้างโพสต์ ความคิดเห็น และการจอง จะส่ง Header `Location` กลับมาด้วย
- นอกจากตรวจรูปแบบข้อมูลแล้ว ระบบยังตรวจสิทธิ์และสถานะของรายการตามกฎการทำงาน

## สิทธิ์การเข้าถึง

| ชื่อ | ความหมาย |
| --- | --- |
| สาธารณะ | ไม่ต้องล็อกอิน สำหรับเส้นทาง GET ที่กำหนดให้เป็นสาธารณะ |
| ผู้ใช้ที่ล็อกอินแล้ว | บัญชีที่ล็อกอินและใช้งานได้ มีบทบาท `USER` หรือ `ADMIN` |
| เจ้าของโพสต์ | บัญชีที่ใช้งานได้และเป็นเจ้าของโพสต์อาหาร |
| ผู้เขียนความคิดเห็น | บัญชีที่ใช้งานได้และเป็นผู้เขียนความคิดเห็น |
| ผู้มีสิทธิ์ลบความคิดเห็น | ผู้เขียนความคิดเห็น เจ้าของโพสต์ หรือ `ADMIN` ใช้กับการลบ |
| ผู้จอง | บัญชีที่ใช้งานได้และเป็นเจ้าของการจอง |
| ผู้เกี่ยวข้องกับการจอง | ผู้จองหรือเจ้าของโพสต์อาหาร |
| แอดมิน | บัญชีที่ใช้งานได้และมีบทบาท `ADMIN` |
| บัญชีปัจจุบัน | บัญชีที่กำลังล็อกอิน เส้นทาง `/me` ใช้กับบัญชีนี้ |

API ที่เปลี่ยนแปลงข้อมูลทุกเส้นทางต้องส่ง CSRF การเป็นแอดมินไม่ได้ให้สิทธิ์แก้ความคิดเห็น แก้โพสต์ หรืออ่านการจองของผู้อื่นโดยอัตโนมัติ

API สาธารณะมี 4 เส้นทาง ได้แก่ `GET /food-posts`, `GET /food-posts/map`, `GET /food-posts/{id}` และ `GET /stats` เส้นทางอื่นต้องล็อกอินก่อน สำหรับโพสต์ที่ยกเลิกแล้ว เส้นทางอ่านรายละเอียดอนุญาตเฉพาะเจ้าของโพสต์หรือแอดมิน ผู้ใช้อื่นได้รับ 404

## การล็อกอินด้วย Session และ CSRF

FoodShare ใช้ Session ของ Spring Security การล็อกอินส่งผ่านฟอร์มที่เส้นทางหลักของแอป โค้ดที่ตรวจในรอบนี้ไม่มี Endpoint ล็อกอินด้วย JWT และไม่มี `/api/v1/csrf`

1. เรียก GET `/login` ที่เส้นทางหลักของแอป และเก็บ Cookie ของ Session
2. อ่านค่า `_csrf` และ `_csrf_header` จากแท็ก meta ใน HTML ที่ได้รับ
3. ส่ง POST `/login` ด้วยฟิลด์ฟอร์ม **email** และ **password** พร้อม Cookie และ Header CSRF
4. เมื่อล็อกอินสำเร็จ ระบบจะเปลี่ยนหน้าไป `/home` ให้เรียก `/home` ด้วย Cookie ที่อัปเดตแล้ว และอ่านค่า CSRF ชุดใหม่จาก meta
5. ส่ง Cookie ของ Session เมื่อเรียกเส้นทางที่ต้องล็อกอิน และส่ง CSRF Token ปัจจุบันเมื่อใช้ POST, PUT, PATCH หรือ DELETE
6. ออกจากระบบด้วย POST `/logout` พร้อม Cookie และ CSRF Token

CSRF Token ไม่จำเป็นต้องใช้ได้เพียงครั้งเดียว ให้อ่านค่าใหม่หลังล็อกอินหรือเมื่อ Session เปลี่ยน และใช้ชื่อ Header ตามค่า `_csrf_header` ซึ่งปกติคือ `X-CSRF-TOKEN`

### ตัวอย่างสำหรับ Windows PowerShell

ใช้ `curl.exe` เพื่อเรียกโปรแกรม curl โดยตรงใน Windows PowerShell รันคำสั่งตามลำดับ และแทนที่ค่าอีเมล รหัสผ่าน และ Token ด้วยค่าจริง อ่าน Token จาก HTML ที่ดาวน์โหลดหรือเครื่องมือสำหรับนักพัฒนาในเบราว์เซอร์ ไฟล์ Cookie มีข้อมูล Session จึงไม่ควรนำขึ้น Git

```powershell
$foodshareBase = "http://localhost:8080"

# เก็บ Cookie และ HTML ที่มี Token ก่อนล็อกอิน
curl.exe -sS -c cookies.txt -o login.html "$foodshareBase/login"

# แทนที่ Token ด้วยค่า _csrf จาก meta ใน login.html
curl.exe -i -b cookies.txt -c cookies.txt -H "X-CSRF-TOKEN: replace-with-login-token" --data-urlencode "email=replace-with-your-email" --data-urlencode "password=replace-with-your-password" "$foodshareBase/login"

# อ่านหน้าเว็บหลังล็อกอิน แล้วใช้ค่า _csrf / _csrf_header ชุดใหม่
curl.exe -sS -b cookies.txt -c cookies.txt -o home.html "$foodshareBase/home"

# อ่านความคิดเห็น โดยเปลี่ยน 123 เป็น ID ของโพสต์จริงที่เข้าถึงได้
curl.exe -i -b cookies.txt "$foodshareBase/api/v1/food-posts/123/comments?page=0"
```

เมื่อส่ง JSON ให้เขียนลงไฟล์ UTF-8 แล้วส่งไฟล์ เพื่อหลีกเลี่ยงปัญหาเครื่องหมายคำพูดระหว่าง PowerShell กับโปรแกรมภายนอก:

```powershell
$foodshareJson = '{"body":"ความคิดเห็นสำหรับทดสอบ API"}'
[System.IO.File]::WriteAllText((Join-Path $PWD "comment-request.json"), $foodshareJson, [System.Text.UTF8Encoding]::new($false))

curl.exe -i -b cookies.txt -H "X-CSRF-TOKEN: replace-with-fresh-token" -H "Content-Type: application/json" --data-binary "@comment-request.json" "$foodshareBase/api/v1/food-posts/123/comments"
```

ใช้ ID ความคิดเห็นที่ได้จากผลตอบกลับสำหรับอ่าน แก้ไข และลบในขั้นต่อไป คำสั่งนี้สร้างความคิดเห็นจริง ให้ใช้โพสต์ทดสอบในเครื่องและลบความคิดเห็นทดสอบเมื่อเสร็จ

## ตารางเส้นทาง API

เส้นทางในตารางต่อจาก `/api/v1` คอลัมน์ข้อมูลที่ส่งเชื่อมไปยังรายละเอียดฟิลด์หรือพารามิเตอร์ ส่วนชนิดข้อมูลที่ส่งกลับอธิบายใน [ผลตอบกลับ](#รูปแบบผลตอบกลับ)

### โพสต์อาหาร

| HTTP Method | เส้นทาง | สิทธิ์ | ข้อมูลที่ส่ง | ผลสำเร็จ |
| --- | --- | --- | --- | --- |
| GET | `/food-posts` | สาธารณะ; `ownership=mine` ต้องล็อกอิน | [ค้นหาโพสต์](#การค้นหาโพสต์) | 200, `PageView<PostView>` |
| GET | `/food-posts/map` | สาธารณะ; `ownership=mine` ต้องล็อกอิน | [ค้นหาแผนที่](#การค้นหาโพสต์บนแผนที่) | 200, `PageView<PostView>` |
| GET | `/food-posts/{id}` | สาธารณะ; มีข้อจำกัดสำหรับโพสต์ที่ยกเลิกแล้ว | — | 200, PostView |
| POST | `/food-posts` | ผู้ใช้ที่ล็อกอินแล้ว | [FoodPostRequest](#foodpostrequest) | 201 + Location, PostView |
| PUT | `/food-posts/{id}` | เจ้าของโพสต์ | [FoodPostRequest](#foodpostrequest) | 200, PostView |
| POST | `/food-posts/{id}/extend` | เจ้าของโพสต์ | [ExtendPostRequest](#extendpostrequest) | 200, PostView |
| DELETE | `/food-posts/{id}` | เจ้าของโพสต์ | — | 204 |
| POST | `/food-posts/{id}/images` | เจ้าของโพสต์ | [อัปโหลดรูปภาพ](#การอัปโหลดรูปภาพ) | 200, PostView |
| DELETE | `/food-posts/{id}/images/{imageId}` | เจ้าของโพสต์ | — | 204 |
| GET | `/me/posts` | บัญชีปัจจุบัน | [แบ่งหน้า](#การแบ่งหน้า) | 200, `PageView<PostView>` |
| GET | `/members/{ownerId}/posts` | ผู้ใช้ที่ล็อกอินแล้ว | [แบ่งหน้า](#การแบ่งหน้า) | 200, `PageView<PostView>` |
| GET | `/me/posts/management-summary` | บัญชีปัจจุบัน | — | 200, ManagementSummary |
| GET | `/stats` | สาธารณะ | — | 200, Stats |
| GET | `/food-posts/{id}/owner-photo` | ผู้ใช้ที่ล็อกอินแล้ว | — | 200 รูปภาพ หรือ 302 ไปยังรูปเริ่มต้น |

การลบโพสต์เปลี่ยนสถานะเป็น `CANCELLED` และยกเลิกการจองที่ยังรอรับผ่าน Event ปิดโพสต์ โดยยังเก็บประวัติของโพสต์ไว้

### การจัดการสต็อกของเจ้าของโพสต์

| HTTP Method | เส้นทาง | สิทธิ์ | ข้อมูลที่ส่ง | ผลสำเร็จ |
| --- | --- | --- | --- | --- |
| GET | `/food-posts/{id}/stock` | เจ้าของโพสต์ | — | 200, StockSnapshot |
| POST | `/food-posts/{id}/stock` | เจ้าของโพสต์ | [ปรับสต็อก](#change-สำหรับปรับสต็อก) | 200, StockSnapshot |

ก่อนเปลี่ยนสต็อก ให้อ่าน `version` ปัจจุบัน หากส่ง `expectedVersion` ที่ล้าสมัย ระบบจะตอบกลับว่าข้อมูลขัดแย้ง

### ความคิดเห็น

| HTTP Method | เส้นทาง | สิทธิ์ | ข้อมูลที่ส่ง | ผลสำเร็จ |
| --- | --- | --- | --- | --- |
| POST | `/food-posts/{postId}/comments` | ผู้ใช้ที่ล็อกอินแล้ว | [CreateCommentRequest](#createcommentrequest) | 201 + Location, CommentView |
| GET | `/food-posts/{postId}/comments` | ผู้ใช้ที่ล็อกอินแล้ว | [แบ่งหน้า](#การแบ่งหน้า) | 200, `PageView<CommentView>` |
| GET | `/comments/{id}` | ผู้ใช้ที่ล็อกอินแล้ว | — | 200, CommentView |
| PUT | `/comments/{id}` | ผู้เขียนความคิดเห็น | [UpdateCommentRequest](#updatecommentrequest) | 200, CommentView |
| DELETE | `/comments/{id}` | ผู้มีสิทธิ์ลบความคิดเห็น | — | 204 |

การแก้ไขเปลี่ยนเฉพาะข้อความ โดยคงผู้เขียน โพสต์ เวลาสร้าง และความสัมพันธ์การตอบกลับ การลบใช้ Soft Delete และเก็บความสัมพันธ์ของคำตอบที่ยังเหลืออยู่ การอ่านหรือแก้ไขความคิดเห็นที่ไม่มีหรือถูกลบแล้วจะได้ 404

### การจองและรับอาหาร

| HTTP Method | เส้นทาง | สิทธิ์ | ข้อมูลที่ส่ง | ผลสำเร็จ |
| --- | --- | --- | --- | --- |
| POST | `/food-posts/{id}/reservations` | ผู้ใช้ที่ล็อกอินแล้ว; จองโพสต์ตนเองไม่ได้ | [จำนวน + Idempotency-Key](#quantity-สำหรับการจอง) | 201 + Location, ReservationView |
| GET | `/food-posts/{id}/reservations` | เจ้าของโพสต์ | — | 200, ReservationView[] |
| GET | `/food-posts/{id}/my-reservation` | บัญชีปัจจุบัน | — | 200, ReservationView; 204 หากไม่มีการจอง |
| GET | `/reservations/{id}` | ผู้เกี่ยวข้องกับการจอง | — | 200, ReservationView |
| PUT | `/reservations/{id}` | ผู้จอง | [จำนวน](#quantity-สำหรับการจอง) | 200, ReservationView |
| DELETE | `/reservations/{id}` | ผู้เกี่ยวข้องกับการจอง | — | 204 |
| POST | `/reservations/{id}/collection` | เจ้าของโพสต์ | [รหัสรับอาหาร](#code-สำหรับรับอาหาร) | 200, ReservationView |
| GET | `/me/reservations` | บัญชีปัจจุบัน | [แบ่งหน้า](#การแบ่งหน้า) | 200, `PageView<ReservationView>` |
| GET | `/reservations/{id}/member-photo` | ผู้เกี่ยวข้องกับการจอง | — | 200 รูปภาพ หรือ 302 ไปยังรูปเริ่มต้น |
| POST | `/pickup/scan` | ผู้ใช้ที่ล็อกอินแล้ว | [อัปโหลดภาพ QR](#การอัปโหลดภาพ-qr) | 200, ScanResult |

การลบการจองคือการยกเลิกและเก็บประวัติไว้ ผู้จองหรือเจ้าของโพสต์ยกเลิกได้ ส่วนการแก้จำนวนอนุญาตเฉพาะผู้จอง

การสร้างการจองใช้ Header ป้องกันคำขอซ้ำ หากส่ง Key เดิมโดยผู้จองคนเดิม โพสต์เดิม และจำนวนเดิม จะได้รับการจองเดิมกลับมา โดย Controller ยังตอบ 201 หากใช้ Key เดิมกับโพสต์หรือจำนวนที่ต่างออกไป จะได้ 409

การยืนยันรับอาหารต้องทำโดยเจ้าของโพสต์ การจองและเวลารับต้องอยู่ในเงื่อนไขที่อนุญาต และต้องมีรหัส 6 หลักของผู้จอง เจ้าของโพสต์ไม่ได้รับรหัสผ่าน ReservationView การสแกน QR สำเร็จเป็นเพียงการอ่านค่า ยังต้องยืนยันรับอาหารผ่านเส้นทาง collection

### โพสต์ที่บันทึกไว้

| HTTP Method | เส้นทาง | สิทธิ์ | ข้อมูลที่ส่ง | ผลสำเร็จ |
| --- | --- | --- | --- | --- |
| PUT | `/food-posts/{id}/saved` | บัญชีปัจจุบัน | — | 204 |
| DELETE | `/food-posts/{id}/saved` | บัญชีปัจจุบัน | — | 204 |
| GET | `/me/saved-posts` | บัญชีปัจจุบัน | [แบ่งหน้า](#การแบ่งหน้า) | 200, `PageView<PostView>` |

### สมาชิกและคำแนะนำเริ่มต้น

| HTTP Method | เส้นทาง | สิทธิ์ | ข้อมูลที่ส่ง | ผลสำเร็จ |
| --- | --- | --- | --- | --- |
| GET | `/members/{id}` | ผู้ใช้ที่ล็อกอินแล้ว | — | 200, MemberProfile |
| GET | `/members/{id}/photo` | ผู้ใช้ที่ล็อกอินแล้ว | — | 200 รูปภาพ หรือ 302 ไปยังรูปเริ่มต้น |
| PATCH | `/me/profile` | บัญชีปัจจุบัน | [ชื่อโปรไฟล์](#name-สำหรับเปลี่ยนชื่อโปรไฟล์) | 204 |
| POST | `/me/onboarding` | บัญชีปัจจุบัน | — | 204 |

MemberProfile ส่งกลับเฉพาะ `id` และ `name` สมาชิกที่ไม่มีหรือถูกระงับจะได้ 404 การแก้โปรไฟล์เปลี่ยนชื่อบัญชีปัจจุบัน ส่วนการจบคำแนะนำจะบันทึกสถานะว่าบัญชีปัจจุบันดูคำแนะนำแล้ว

### การแจ้งเตือน

| HTTP Method | เส้นทาง | สิทธิ์ | ข้อมูลที่ส่ง | ผลสำเร็จ |
| --- | --- | --- | --- | --- |
| GET | `/me/notifications` | บัญชีปัจจุบัน | [แบ่งหน้า](#การแบ่งหน้า) | 200, `PageView<NotificationView>` |
| POST | `/me/notifications/{id}/read` | บัญชีปัจจุบัน; เฉพาะการแจ้งเตือนตนเอง | — | 204 |
| GET | `/me/notifications/unread` | บัญชีปัจจุบัน | — | 200, Count |
| GET | `/me/notification-preferences` | บัญชีปัจจุบัน | — | 200, Preferences |
| PUT | `/me/notification-preferences` | บัญชีปัจจุบัน | [PreferenceInput](#preferenceinput) | 200, Preferences |

### การรายงานและการดูแลระบบ

| HTTP Method | เส้นทาง | สิทธิ์ | ข้อมูลที่ส่ง | ผลสำเร็จ |
| --- | --- | --- | --- | --- |
| POST | `/reports` | ผู้ใช้ที่ล็อกอินแล้ว | [ReportInput](#reportinput) | 201, ReportView |
| GET | `/admin/pending-reports` | แอดมิน | — | 200, Count |
| GET | `/admin/posts` | แอดมิน | [ค้นหาโพสต์สำหรับแอดมิน](#การค้นหาโพสต์สำหรับแอดมิน) | 200, `PageView<AdminPostView>` |
| GET | `/admin/reports` | แอดมิน | [แบ่งหน้า](#การแบ่งหน้า) | 200, `PageView<ReportView>` |
| PATCH | `/admin/reports/{id}` | แอดมิน | [Resolve](#resolve) | 200, ReportView |
| GET | `/admin/users` | แอดมิน | [แบ่งหน้า](#การแบ่งหน้า) | 200, `PageView<AdminUserView>` |
| PATCH | `/admin/users/{id}` | แอดมิน | [Active](#active) | 204 |

รายงานที่จัดการแล้วไม่สามารถจัดการซ้ำได้ (409) การส่ง `closePost=true` จะปิดโพสต์และยกเลิกการจองที่รอรับ ส่วนการเปลี่ยนสถานะบัญชีไม่อนุญาตให้แอดมินดำเนินการกับบัญชีของตนเอง (409)

### เส้นทางเดิมเพื่อรองรับการใช้งานเก่า

เส้นทางนี้ใช้ URL เต็มตามตาราง ไม่ต่อจาก `/api/v1`:

| HTTP Method | เส้นทางเต็ม | สิทธิ์ | ข้อมูลที่ส่ง | ผลสำเร็จ |
| --- | --- | --- | --- | --- |
| GET | `/api/food-posts/map` | ผู้ใช้ที่ล็อกอินแล้ว | — | 200, MapFoodPostResponse[] |

สำหรับการเชื่อมต่อใหม่ แนะนำให้ใช้เส้นทางแผนที่ v1 เส้นทางเดิมส่งกลับเป็นอาร์เรย์ ส่วน v1 ใช้ PageView

### ฟอร์มบัญชีบนหน้าเว็บ

เส้นทางต่อไปนี้อยู่ที่รากของแอป รับส่งผ่าน HTML และฟอร์ม:

| HTTP Method | เส้นทาง | สิทธิ์ | รูปแบบ / ผลลัพธ์ |
| --- | --- | --- | --- |
| GET / POST | `/login` | สาธารณะ; POST ต้องส่ง CSRF | HTML / ฟอร์ม `email,password`; สำเร็จแล้วไป `/home` |
| POST | `/logout` | ใช้ Session; ต้องส่ง CSRF | สำเร็จแล้วไป `/` |
| GET / POST | `/register` | สาธารณะ; POST ต้องส่ง CSRF | HTML / ฟอร์ม RegisterRequest; สำเร็จแล้วไป `/login` |
| GET / POST | `/forgot-password` | สาธารณะ; POST ต้องส่ง CSRF | HTML / ฟอร์ม email; สำเร็จแล้วไป `/forgot-password?sent` |
| GET / POST | `/reset-password` | สาธารณะ; POST ต้องส่ง CSRF | HTML / ฟอร์ม token,password,confirmPassword; สำเร็จแล้วไป `/login?resetSuccess` |

หากข้อมูลฟอร์มไม่ผ่านการตรวจสอบ Controller จะแสดงหน้า HTML เดิมพร้อมข้อผิดพลาด โดยไม่ใช้รูปแบบข้อผิดพลาดของ REST API ฟิลด์สมัครสมาชิกคือ `email` (รูปแบบอีเมลถูกต้อง ไม่เกิน 254 ตัวอักษร), `password` (8–72 ตัวอักษร และผ่านการตรวจของ Service) และ `displayName` (ห้ามว่าง ไม่เกิน 80 ตัวอักษร) การรีเซ็ตรหัสผ่านต้องยืนยันรหัสตรงกัน ใช้ Token ที่ถูกต้องและยังไม่ถูกใช้ และรหัสผ่านต้องมี 8–72 ตัวอักษร โดยไม่เกิน 72 ไบต์เมื่อเข้ารหัส UTF-8

## รูปแบบข้อมูลที่ต้องส่ง

ชื่อฟิลด์ JSON ใช้ตามโค้ดจริง คอลัมน์ต้องส่งระบุเงื่อนไขตาม Validation ของ DTO ส่วนกฎการทำงานเพิ่มเติมอธิบายใต้ตาราง

### FoodPostRequest

ใช้ทั้งสร้างและแก้ไขโพสต์ เมื่อใช้ PUT ต้องส่งข้อมูลครบตาม DTO

| ฟิลด์ | ชนิดข้อมูล | ต้องส่ง | เงื่อนไขตรวจสอบ |
| --- | --- | --- | --- |
| title | ข้อความ | ใช่ | ห้ามว่าง ไม่เกิน 150 ตัวอักษร |
| description | ข้อความ | ใช่ | ห้ามว่าง ไม่เกิน 1000 ตัวอักษร |
| category | ข้อความตาม Enum | ใช่ | `FOOD`, `DRINK`, `SNACK` |
| quantity | จำนวนเต็ม | ใช่ | 1–10000 |
| unit | ข้อความ | ใช่ | ห้ามว่าง ไม่เกิน 50 ตัวอักษร |
| pickupLocationName | ข้อความ | ใช่ | ห้ามว่าง ไม่เกิน 255 ตัวอักษร |
| latitude | ทศนิยม | ใช่ | -90 ถึง 90 |
| longitude | ทศนิยม | ใช่ | -180 ถึง 180 |
| availableFrom | วันเวลาท้องถิ่นรูปแบบ ISO | ใช่ | เวลาเริ่มรับอาหาร |
| availableUntil | วันเวลาท้องถิ่นรูปแบบ ISO | ใช่ | หลังเวลาเริ่มรับและเวลาปัจจุบันของแอป |
| allergens | ข้อความ | ไม่ | ไม่เกิน 500 ตัวอักษร |
| maxPerPerson | จำนวนเต็ม / null | ไม่ | 1–10000 และไม่เกิน quantity; null หมายถึงไม่กำหนดเพดานต่อคน |

เงื่อนไขเพิ่มเติมเมื่อแก้ไข: โพสต์ต้องอยู่ในสถานะที่แก้ได้ จำนวนรวมต้องไม่น้อยกว่าจำนวนที่จอง รับแล้ว และแจกนอกเว็บรวมกัน หากมีผู้จองแล้ว ระบบจำกัดการเปลี่ยนเวลาหรือสถานที่รับและการลดจำนวนสูงสุดต่อคน หากขัดกับสถานะหรือกฎการทำงานจะได้ 409

ตัวอย่างคำขอ: ก่อนทดสอบให้ปรับวันเวลารับอาหารให้อยู่ในอนาคต:

```json
{
  "title": "ข้าวกล่องแบ่งปัน",
  "description": "อาหารทำใหม่ รับตามเวลาที่ระบุ",
  "category": "FOOD",
  "quantity": 5,
  "unit": "กล่อง",
  "pickupLocationName": "มหาวิทยาลัยขอนแก่น",
  "latitude": 16.47,
  "longitude": 102.82,
  "availableFrom": "2026-10-10T12:00:00",
  "availableUntil": "2026-10-10T14:00:00",
  "allergens": "ไข่",
  "maxPerPerson": 2
}
```

### ExtendPostRequest

| ฟิลด์ | ชนิดข้อมูล | ต้องส่ง | เงื่อนไขตรวจสอบ |
| --- | --- | --- | --- |
| availableUntil | วันเวลาท้องถิ่นรูปแบบ ISO | ใช่ | เวลาสิ้นสุดใหม่ต้องอยู่หลังเวลาปัจจุบัน |

เจ้าของสามารถขยายเวลาโพสต์ที่หมดเวลารับแล้ว แต่ยังไม่ถูกยกเลิกและมีอาหารเหลือ การจองเก่าจะหมดอายุก่อน และการขยายเวลาไม่ทำให้การจองเก่ากลับมาใช้งานได้

### CreateCommentRequest

| ฟิลด์ | ชนิดข้อมูล | ต้องส่ง | เงื่อนไขตรวจสอบ |
| --- | --- | --- | --- |
| body | ข้อความ | ใช่ | ห้ามว่าง ไม่เกิน 800 ตัวอักษร |
| parentCommentId | จำนวนเต็ม / null | ไม่ | หากส่งต้องเป็นจำนวนบวก และเป็นความคิดเห็นที่ตอบกลับได้ในโพสต์เดียวกัน |

ความคิดเห็นหลัก:

```json
{"body":"ยังมีอาหารไหม"}
```

การตอบกลับ: เปลี่ยน 456 เป็น ID ความคิดเห็นจริง:

```json
{"body":"ขอรับ 1 กล่องค่ะ","parentCommentId":456}
```

### UpdateCommentRequest

| ฟิลด์ | ชนิดข้อมูล | ต้องส่ง | เงื่อนไขตรวจสอบ |
| --- | --- | --- | --- |
| body | ข้อความ | ใช่ | ห้ามว่าง ไม่เกิน 800 ตัวอักษร; ตัดช่องว่างหัวท้ายก่อนบันทึก |

```json
{"body":"แก้ไขเป็นขอรับ 2 กล่องค่ะ"}
```

### Quantity สำหรับการจอง

| ฟิลด์ | ชนิดข้อมูล | ต้องส่ง | เงื่อนไขตรวจสอบ |
| --- | --- | --- | --- |
| quantity | จำนวนเต็ม | ใช่ | 1–10000 โดยต้องผ่านเงื่อนไขสต็อกและจำนวนสูงสุดต่อคน |

```json
{"quantity":2}
```

การสร้างการจองต้องส่ง Header `Idempotency-Key` ยาว 16–64 ตัวอักษร ตรงกับรูปแบบ `[a-zA-Z0-9-]{16,64}` เช่น `550e8400-e29b-41d4-a716-446655440000` Key นี้เป็นข้อความสำหรับป้องกันคำขอซ้ำ ส่วน ID ของข้อมูลยังเป็นตัวเลข หากมีการจองที่ยังใช้งานอยู่ในโพสต์เดียวกัน ต้องแก้ไขผ่านการจองเดิม

### Code สำหรับรับอาหาร

| ฟิลด์ | ชนิดข้อมูล | ต้องส่ง | เงื่อนไขตรวจสอบ |
| --- | --- | --- | --- |
| code | ข้อความ | ใช่ | ห้ามว่าง; Service ตรวจรหัส 6 หลักกับการจอง |

```json
{"code":"123456"}
```

รหัสนี้เป็นเพียงตัวอย่าง ให้ใช้รหัสจริงของการจอง ระบบบันทึกการกรอกรหัสผิด หากผิดหลายครั้งอาจตอบ 429 และระงับการลองรหัสชั่วคราว

### Change สำหรับปรับสต็อก

| ฟิลด์ | ชนิดข้อมูล | ต้องส่ง | เงื่อนไขตรวจสอบ |
| --- | --- | --- | --- |
| action | ข้อความตาม Enum | ใช่ | `ADD`, `REMOVE`, `OFFLINE`, `UNDO_OFFLINE` |
| amount | จำนวนเต็ม | ใช่ | 1–10000 และต้องผ่านกฎสต็อก |
| expectedVersion | จำนวนเต็ม | ใช่ | ค่า StockSnapshot.version ปัจจุบัน ต้องไม่ติดลบ |

```json
{"action":"OFFLINE","amount":1,"expectedVersion":0}
```

ใช้ Version จริงจาก GET stock: ADD เพิ่มจำนวนรวม, REMOVE ลดจำนวนที่ยังไม่ถูกจัดสรร, OFFLINE บันทึกยอดแจกนอกเว็บ และ UNDO_OFFLINE ย้อนยอดแจกนอกเว็บ โดยคงจำนวนที่จองหรือรับแล้วไว้

### Name สำหรับเปลี่ยนชื่อโปรไฟล์

| ฟิลด์ | ชนิดข้อมูล | ต้องส่ง | เงื่อนไขตรวจสอบ |
| --- | --- | --- | --- |
| name | ข้อความ | ใช่ | ห้ามว่าง ไม่เกิน 80 ตัวอักษร |

### PreferenceInput

| ฟิลด์ | ชนิดข้อมูล | ต้องส่ง | เงื่อนไข / การทำงาน |
| --- | --- | --- | --- |
| categories | อาร์เรย์ข้อความ / null | ไม่ | รับ FOOD, DRINK, SNACK; ตัดค่าที่ไม่รู้จักและค่าซ้ำออก |
| keywords | ข้อความ / null | ไม่ | ไม่เกิน 300 ตัวอักษร; ปรับช่องว่าง; null เปลี่ยนเป็นข้อความว่าง |

```json
{"categories":["FOOD","SNACK"],"keywords":"ข้าว ขนม"}
```

### ReportInput

| ฟิลด์ | ชนิดข้อมูล | ต้องส่ง | เงื่อนไขตรวจสอบ |
| --- | --- | --- | --- |
| postId | จำนวนเต็ม | ใช่ | อย่างน้อย 1; โพสต์ที่อ้างถึงต้องมีอยู่ |
| commentId | จำนวนเต็ม / null | ไม่ | ความคิดเห็นที่อ้างถึงต้องอยู่ในโพสต์นั้น |
| reason | ข้อความ | ใช่ | ห้ามว่าง ไม่เกิน 1000 ตัวอักษร |

### Resolve

| ฟิลด์ | ชนิดข้อมูล | ต้องส่ง | เงื่อนไข / การทำงาน |
| --- | --- | --- | --- |
| reason | ข้อความ | ใช่ | ห้ามว่าง ไม่เกิน 1000 ตัวอักษร |
| closePost | บูลีน (true/false) | ไม่ | true ปิดโพสต์; หากไม่ส่งจะใช้ false ตามค่าเริ่มต้นของ primitive boolean |

### Active

| ฟิลด์ | ชนิดข้อมูล | ต้องส่ง | เงื่อนไข / การทำงาน |
| --- | --- | --- | --- |
| reason | ข้อความ | ใช่ | ห้ามว่าง ไม่เกิน 1000 ตัวอักษร |
| active | บูลีน (true/false) | ไม่ | true เปิดใช้งาน, false ระงับ; หากไม่ส่งจะใช้ false ตามค่าเริ่มต้นของ primitive boolean |

เมื่อเปลี่ยนสถานะบัญชี ให้ระบุ `active` ในคำขอทุกครั้ง

### การอัปโหลดรูปภาพ

ส่งรูป JPG/JPEG หรือ PNG ในฟิลด์ Multipart ชื่อ `file` ตัวตรวจรูปภาพตรวจเนื้อหา ขนาดไม่เกิน 5 MiB และไม่เกิน 20 ล้านพิกเซล แต่ละโพสต์มีรูปในแกลเลอรีได้สูงสุด 5 รูป ค่ากำหนดขนาดคำขอ Multipart รวมคือ 6 MB

เนื้อหารูปภาพไม่ถูกต้องอาจได้ 400 ส่วนการเกินขนาดที่กำหนดใน Multipart จะได้ 413 เมื่ออัปโหลดสำเร็จ จะได้ PostView ที่อัปเดตแล้วพร้อมรายการรูปภาพ

### การอัปโหลดภาพ QR

ส่งภาพ QR ในฟิลด์ Multipart ชื่อ `frame` เมื่ออ่านสำเร็จจะได้ `{"value":"decoded-content"}` หากอ่าน QR ไม่ได้จะได้ 422 หลังสแกนยังต้องดำเนินการยืนยันรับอาหารแยกต่างหาก

## พารามิเตอร์สำหรับค้นหาและแบ่งหน้า

### การค้นหาโพสต์

| พารามิเตอร์ | ค่าเริ่มต้น | ความหมาย / ข้อจำกัด |
| --- | --- | --- |
| q | ว่าง | ค้นหาชื่อโพสต์หรือสถานที่ ไม่เกิน 100 ตัวอักษร |
| category | ว่าง | ไม่กรอง หรือ FOOD / DRINK / SNACK |
| sort | expiry | expiry / latest / nearby |
| lat | ไม่ส่ง | ละติจูด -90 ถึง 90 |
| lng | ไม่ส่ง | ลองจิจูด -180 ถึง 180 |
| now | false | เลือกเฉพาะโพสต์ที่เริ่มช่วงเวลารับอาหารแล้ว |
| page | 0 | เริ่มนับจาก 0; ช่วง 0–10000 |
| size | 12 | 1–200 |
| ownership | ว่าง | ว่าง / mine / others |

พิกัดต้องเป็นตัวเลขที่มีค่าจำกัดและส่ง lat/lng คู่กัน การเรียงแบบ nearby ต้องมีทั้งสองค่า ส่วน `ownership=mine` ต้องล็อกอิน หากล็อกอินแล้ว `others` จะตัดโพสต์ของบัญชีปัจจุบันออก หากยังไม่ล็อกอิน จะไม่มีบัญชีให้ใช้ตัดโพสต์

ตัวอย่าง:

```text
GET /api/v1/food-posts?category=FOOD&sort=expiry&now=true&page=0&size=12
```

### การค้นหาโพสต์บนแผนที่

รับ `q`, `category`, `now` และ `ownership` โดยใช้ค่าเริ่มต้นตามตารางค้นหาโพสต์ Controller กำหนด sort เป็น expiry, page เป็น 0 และ size เป็น 200 จึงส่งกลับสูงสุด 200 รายการ

### การแบ่งหน้า

รับ `page` ค่าเริ่มต้น 0 เส้นทางต่อไปนี้กำหนดจำนวนรายการต่อหน้าและการเรียงไว้ในระบบ โดยผู้เรียกไม่ได้กำหนด `size` หรือ `sort`:

| เส้นทาง | รายการต่อหน้า | การเรียง |
| --- | --- | --- |
| รายการความคิดเห็นของโพสต์ | 30 | createdAt จากเก่าไปใหม่ แล้ว id จากน้อยไปมาก |
| /me/posts, /members/{ownerId}/posts | 12 | createdAt จากใหม่ไปเก่า |
| /me/reservations | 12 | createdAt จากใหม่ไปเก่า |
| /me/saved-posts | 12 | createdAt ของรายการบันทึก จากใหม่ไปเก่า |
| /me/notifications | 20 | createdAt จากใหม่ไปเก่า |
| /admin/reports | 20 | createdAt จากใหม่ไปเก่า |
| /admin/users | 20 | id จากน้อยไปมาก |

เส้นทางในตาราง ยกเว้น `/me/saved-posts` ปรับค่าหน้าติดลบเป็น 0 ส่วน `/me/saved-posts` และการค้นหา `/food-posts` ตรวจช่วง page 0–10000 และตอบ 400 หากค่าไม่ถูกต้อง

### การค้นหาโพสต์สำหรับแอดมิน

| พารามิเตอร์ | ค่าเริ่มต้น | ความหมาย / ข้อจำกัด |
| --- | --- | --- |
| page | 0 | เริ่มจาก 0; หน้าละ 20; ค่าติดลบปรับเป็น 0 |
| q | ว่าง | ค้นหาชื่อโพสต์ ชื่อเจ้าของ หรือสถานที่รับ ไม่เกิน 120 ตัวอักษร |
| status | ว่าง | ไม่กรอง หรือ AVAILABLE / LOW_STOCK / CLAIMED / EXPIRED / CANCELLED |

เรียงตาม createdAt จากใหม่ไปเก่า แล้วตาม id จากมากไปน้อย ตัวกรอง status ใช้ FoodPostStatus ที่เก็บในฐานข้อมูล ซึ่งอาจต่างจากสถานะเพื่อแสดงผลที่คำนวณใน PostView

## รูปแบบผลตอบกลับ

### PageView

```json
{
  "items": [],
  "page": 0,
  "totalPages": 0,
  "totalElements": 0
}
```

ตัวอย่างนี้เป็นหน้าที่ไม่มีรายการ จำนวนจริงขึ้นอยู่กับข้อมูล ระบบส่งฟิลด์ของ PageView โดยตรง ไม่มีตัวครอบระดับบนชื่อ `data`, `success` หรือ `content`

### PostView

| ฟิลด์ | ชนิด JSON / ความหมาย |
| --- | --- |
| id, ownerId | ID แบบตัวเลข |
| title, description, category, unit, pickupLocationName | ข้อความ |
| quantity, reservedQuantity, collectedQuantity, availableQuantity, offlineQuantity | จำนวนเต็มแสดงยอดสต็อก |
| latitude, longitude | พิกัดเป็นทศนิยม |
| availableFrom, availableUntil, createdAt | วันเวลาท้องถิ่นรูปแบบ ISO |
| status | สถานะสำหรับแสดงผลที่ Mapper คำนวณ |
| ownerName, allergens | ข้อความ |
| imageUrl | URL รูปภาพหลัก |
| images | อาร์เรย์ของ `{id,url,sortOrder}` |
| commentCount | จำนวนเต็ม |
| mine, saved | บูลีนตามบัญชีที่เรียกอ่าน |
| distanceKm | ตัวเลขหรือ null ขึ้นอยู่กับพิกัดที่ส่ง |
| maxPerPerson | จำนวนเต็มหรือ null |

จำนวนอาหารที่ยังจองได้เท่ากับจำนวนรวมลบด้วยจำนวนที่จอง รับแล้ว และแจกนอกเว็บ สถานะที่แสดงอาจต่างจาก Enum ในฐานข้อมูล เพราะ Mapper พิจารณาเวลาและอาหารคงเหลือด้วย

### CommentView

| ฟิลด์ | ชนิดข้อมูล / ความหมาย |
| --- | --- |
| id, postId, authorId | ID แบบตัวเลข |
| authorName, body | ข้อความ |
| createdAt | วันเวลาท้องถิ่นรูปแบบ ISO |
| canDelete | บูลีนแสดงสิทธิ์ลบของบัญชีที่อ่าน |
| parentCommentId | ID ความคิดเห็นหลักของเธรด หรือ null |
| replyToCommentId | ID ความคิดเห็นที่ตอบกลับโดยตรง หรือ null |
| replyToAuthorName | ชื่อผู้เขียนความคิดเห็นที่ตอบกลับ หรือ null |
| parentDeleted | ความคิดเห็นหลักถูกลบแล้วหรือไม่ |
| canEdit | true เฉพาะผู้เขียนความคิดเห็น |

### ReservationView

| ฟิลด์ | ชนิดข้อมูล / ความหมาย |
| --- | --- |
| id | ID การจองแบบตัวเลข |
| post | Object PostView ภายในผลตอบกลับ |
| quantity | จำนวนเต็ม |
| status | RESERVED / COLLECTED / CANCELLED / EXPIRED |
| memberName, memberId | ชื่อแสดงผลและ ID แบบตัวเลขของผู้จอง |
| pickupCode | ข้อความรหัสสำหรับผู้จองเมื่อยังใช้รับอาหารได้; กรณีอื่นเป็น null |
| owner | บัญชีที่อ่านเป็นเจ้าของโพสต์หรือไม่ |
| createdAt | วันเวลาท้องถิ่นรูปแบบ ISO |

### ผลตอบกลับชนิดอื่น

| ชนิดข้อมูล | ฟิลด์ |
| --- | --- |
| MemberProfile | `id,name` |
| MapFoodPostResponse (เส้นทางเดิม) | `id,title,description,category,quantity,unit,pickupLocationName,latitude,longitude,availableFrom,availableUntil,status,categoryIcon` |
| StockSnapshot | `postId,quantity,reservedQuantity,collectedQuantity,offlineQuantity,availableQuantity,version,unit` |
| ManagementSummary | `totalPosts,openPosts,waitingCount,collectedCount,offlineCount` |
| Stats | `shared,posts` |
| Count | `count` |
| Preferences | `categories,keywords` |
| ScanResult | `value` |
| NotificationView | `id,title,message,href,type,actorId,actorName,createdAt,read` |
| ReportView | `id,postId,commentId,title,reporter,reason,status,resolution,createdAt` |
| AdminPostView | `id,title,owner,status,availableQuantity,reservedQuantity,unit,pickupLocationName,availableUntil` |
| AdminUserView | `id,name,email,active,role` |

เส้นทางรูปภาพส่งกลับเป็นข้อมูลรูปภาพ หากไม่มีรูป จะเปลี่ยนเส้นทางไป `/images/default-profile.png` ด้วย 302

## รูปแบบข้อผิดพลาด

ตัวจัดการข้อผิดพลาดส่วนกลางของ API ส่งข้อมูลรูปแบบนี้:

```json
{
  "status": 400,
  "message": "กรุณาตรวจสอบข้อมูลที่กรอก",
  "fields": {
    "body": "กรุณาเขียนความคิดเห็น"
  }
}
```

หากข้อผิดพลาดไม่ผูกกับฟิลด์ `fields` จะเป็น Object ว่าง ผลตอบกลับจาก Security Filter อาจไม่มี `fields` และการปฏิเสธ CSRF อาจใช้รูปแบบต่างจาก JSON ของตัวจัดการนี้

| Status Code | ความหมาย |
| --- | --- |
| 400 | JSON ไม่ถูกต้อง ฟิลด์ไม่ผ่าน Validation ค่าตัวเลข/Enum หรือข้อมูลตามกฎการทำงานไม่ถูกต้อง |
| 401 | ต้องล็อกอินก่อนเรียก API |
| 403 | ไม่มีสิทธิ์ บัญชีถูกระงับ หรือ CSRF ขาดหาย/ไม่ถูกต้อง |
| 404 | ไม่พบข้อมูล ความคิดเห็นถูกลบ หรือโพสต์ถูกซ่อนตามสิทธิ์ |
| 409 | สต็อกไม่พอ สถานะขัดแย้ง Key คำขอซ้ำใช้กับข้อมูลต่างกัน หรือข้อมูล/Version เปลี่ยนพร้อมกัน |
| 413 | ขนาด Multipart เกินค่าที่กำหนด |
| 422 | อ่าน QR ไม่ได้ |
| 429 | ส่งคำขอหรือลองรหัสถี่เกินไป รวมถึงการระงับการลองรหัสรับอาหาร |
| 500 | API เกิดข้อผิดพลาดที่ไม่คาดคิด รายละเอียดบันทึกใน Log ฝั่งเซิร์ฟเวอร์ |

Status Code ในตารางเส้นทางระบุผลเมื่อคำขอสำเร็จ ส่วนข้อผิดพลาดจาก Framework อาจมีรูปแบบเพิ่มเติม เอกสารนี้อ้างอิงโค้ดและไม่ได้ยืนยันว่าทดสอบข้อมูลผิดทุกกรณีแล้ว

## Swagger / OpenAPI

- Swagger ในเครื่อง: `http://localhost:8080/swagger-ui.html` หรือ `/swagger-ui/index.html` โดยปรับพอร์ตให้ตรงกับเครื่อง
- OpenAPI JSON ในเครื่อง: `http://localhost:8080/v3/api-docs`
- Swagger ของเว็บสาธารณะ: [https://kku-foodshare.onrender.com/swagger-ui.html](https://kku-foodshare.onrender.com/swagger-ui.html).
- OpenAPI JSON ของเว็บสาธารณะ: [https://kku-foodshare.onrender.com/v3/api-docs](https://kku-foodshare.onrender.com/v3/api-docs).

โค้ดอนุญาตให้เปิด Swagger และ OpenAPI โดยไม่ล็อกอิน แต่การเรียก API ที่มีสิทธิ์จำกัดยังต้องมี Session ที่ล็อกอินแล้วและ CSRF Token ให้ล็อกอินบนเว็บไซต์เดียวกันก่อน ระบบนี้ไม่มี Bearer JWT สำหรับกรอกใน Authorize

ค่ากำหนดเปิดการรองรับ CSRF ของ Swagger ไว้ แต่ยังต้องตรวจการเขียนข้อมูลผ่าน Swagger บนเว็บที่ Deploy จริง การเปิดหน้า Swagger ได้เพียงอย่างเดียวไม่ได้ยืนยันว่าคำขอเหล่านั้นสำเร็จ

## ผลการทดสอบ

### หลักฐานที่มีอยู่

| สิ่งที่ตรวจ | ผล / หลักฐาน | ขอบเขตและข้อจำกัด |
| --- | --- | --- |
| สคริปต์ Docker Phase 12 บน Windows ของเจ้าของโปรเจกต์ | ผู้ใช้ส่งผล Terminal ที่ลงท้ายด้วย `All Docker test commands passed` | สคริปต์ตรวจ java-tests, js-tests และ postgres-tests ตามลำดับ; ไม่ได้รัน Docker ซ้ำในสภาพแวดล้อมของรอบจัดทำเอกสาร |
| ชุดทดสอบย่อยที่ใช้ PostgreSQL | ผู้ใช้ส่ง `Tests run: 48, Failures: 0, Errors: 0, Skipped: 0` และ BUILD SUCCESS | 48 tests อยู่ใน CommentCrudJourneyTest, FoodJourneyTest, ReviewRegressionTest และ WebPagesTest เป็นชุดย่อย ไม่ใช่จำนวน Java tests ทั้งหมด |
| ชุด Unit Test ของ JavaScript ที่รันในรอบจัดทำเอกสาร | 132 tests ผ่าน 132 ไม่ผ่าน 0 ข้าม 0 | รันในเครื่องด้วย Node; ไม่ได้เรียก Java API หรือเซิร์ฟเวอร์ที่ Deploy |
| โค้ดเทสผลลัพธ์ HTTP ของความคิดเห็น | 8 tests ใน CommentCrudJourneyTest ตรวจ JSON, Status Code, Location, การบันทึกข้อมูล, สิทธิ์, Validation, CSRF และ OpenAPI | ใช้ Spring Boot + MockMvc พร้อมผู้ใช้ทดสอบและ CSRF Helper; ไม่ใช่การล็อกอินผ่านเบราว์เซอร์หรือคำขอจริงไป Render |
| รายการ Endpoint ในเอกสารนี้ | เทียบกับเส้นทางใน Controller, DTO, กฎ Service และ SecurityConfig | ตรวจจากโค้ด; ไม่ได้ระบุ Commit ที่กำลัง Deploy |
| ผลตอบกลับจริงของทุก Endpoint บนเว็บที่ Deploy | รอบนี้ยังไม่ได้เก็บผลตอบกลับหรือเรียกตรวจครบทุกเส้นทาง | ยังต้องทดสอบการใช้งานหลักบนเว็บเวอร์ชันที่จะส่ง |

**มีเทสตรวจผลตอบกลับของ API แล้ว และผล Docker ที่ผู้ใช้ส่งมาผ่าน แต่ยังไม่ได้ยืนยันการเรียกทุก Endpoint บนเว็บที่ Deploy จริง**

โค้ดที่มีอยู่แนบรายงานการลองรัน Maven ในเครื่องก่อนหน้านี้ ซึ่งไม่ใช่หลักฐานเดียวกับผล Docker ที่รันสำเร็จบน Windows เมื่อนำส่งงาน ให้ใช้ Log และ Surefire Report จากรอบที่รันผ่าน

### ผลลัพธ์ Comment CRUD ที่เทสตรวจอยู่แล้ว

| กรณีทดสอบ | ผลลัพธ์ที่เทสตรวจ |
| --- | --- |
| สร้างความคิดเห็น | 201 พร้อม Header Location และ canEdit=true |
| อ่านความคิดเห็นที่สร้าง | 200 และ body ตรงกับข้อความ |
| แก้ความคิดเห็นตนเอง | 200 ข้อความใหม่ตัดช่องว่างหัวท้าย และบันทึกลงฐานข้อมูล |
| อ่านรายการความคิดเห็น | 200 พร้อมข้อความใหม่ใน PageView |
| ลบความคิดเห็น | 204 ไม่มีเนื้อหาผลตอบกลับ |
| อ่านหลังลบ | 404 |
| สมาชิกอื่น เจ้าของโพสต์ หรือแอดมิน แก้ความคิดเห็นของผู้อื่น | 403 และคงข้อความเดิม |
| แก้ด้วยข้อความว่าง ยาวเกิน หรือรูปแบบไม่ถูกต้อง | 400 และคงข้อความเดิม |
| แก้คำตอบกลับ | คงผู้เขียน ความสัมพันธ์ของเธรด และเวลาสร้างเดิม |
| อ่านหรือแก้ความคิดเห็นที่ไม่มีหรือถูกลบแล้ว | 404 |
| แก้คำตอบหลังความคิดเห็นหลักถูกลบ | เก็บคำตอบไว้ และ parentDeleted=true |
| เรียกเส้นทางที่ต้องล็อกอินขณะยังไม่ล็อกอิน | 401 ในกรณีที่เทสตรวจ |
| ล็อกอินแล้วแก้ไขโดยไม่ส่ง CSRF | 403 |
| เส้นทางอ่าน/แก้ความคิดเห็นใน OpenAPI | มีอยู่ใน /v3/api-docs |

### วิธีรันทดสอบก่อนส่งงาน

เปิด Windows PowerShell ในโฟลเดอร์หลักของ Repository แล้วรัน:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test-phase12.ps1
```

เก็บรายงานและ Log ใหม่จาก:

- `test/reports/phase12/java-tests-docker.log`
- `test/reports/phase12/js-tests-docker.log`
- `test/reports/phase12/postgres-tests-docker.log`
- `code/target/surefire-reports/`

ใช้รายงานจาก Commit เดียวกับที่จะส่ง ขั้นทดสอบ PostgreSQL อาจเขียนทับรายงานของคลาสที่รันซ้ำ จึงควรเก็บ Log แยกของแต่ละ Service ด้วย

จากนั้นตรวจแอปที่กำลังรันด้วยบัญชีทดสอบจริง:

1. เรียก GET ค้นหาโพสต์แบบสาธารณะ ตรวจ 200, JSON PageView, การแบ่งหน้า และการเรียงข้อมูล
2. ล็อกอินด้วยบัญชีทดสอบ แล้วทำความคิดเห็นตามลำดับ: สร้าง → อ่าน → แก้ไข → อ่านรายการ → ลบ → อ่านหลังลบ พร้อมบันทึก Status Code, Location และ JSON ที่ได้รับ
3. ใช้บัญชีที่สองตรวจว่าแก้ความคิดเห็นของผู้อื่นไม่ได้ และใช้บัญชีแอดมินตรวจเส้นทางสำหรับแอดมิน
4. ทดสอบรายการที่ไม่มีอยู่ ข้อมูลใน Body ไม่ถูกต้อง และการเขียนข้อมูลโดยไม่ส่ง CSRF
5. เปิดเว็บสาธารณะ Swagger และ OpenAPI JSON ตรวจ Commit และค่ากำหนดที่ Deploy แล้วทดสอบการใช้งานหลักด้วยข้อมูลทดสอบโดยเฉพาะ

เก็บหลักฐานผลตอบกลับใน `test/reports/` หลังนำ Cookie ของ Session, CSRF Token, รหัสผ่าน และรหัสรับอาหารออก หากบริการ Hosting พักการทำงานอยู่ ให้รอเริ่มระบบเสร็จก่อนประเมินผลตอบกลับ

## ตำแหน่งโค้ดที่เกี่ยวข้อง

ตำแหน่งไฟล์ต่อไปนี้เริ่มจากโฟลเดอร์หลักของ Repository:

| หน้าที่ | ตำแหน่ง |
| --- | --- |
| เส้นทาง API, Record ข้อมูลรับเข้า และผลตอบกลับเมื่อสำเร็จ | `code/src/main/java/com/kku/foodshare/controller/api/` |
| DTO ข้อมูลรับเข้า | `code/src/main/java/com/kku/foodshare/dto/request/` |
| DTO ข้อมูลส่งกลับ | `code/src/main/java/com/kku/foodshare/dto/response/` |
| กฎเจ้าของข้อมูล สต็อก การจอง การดูแลระบบ และความคิดเห็น | `code/src/main/java/com/kku/foodshare/service/impl/` |
| ล็อกอินด้วย Session และเส้นทางสาธารณะ/จำกัดสิทธิ์ | `code/src/main/java/com/kku/foodshare/config/SecurityConfig.java` |
| เขตเวลาของแอป | `code/src/main/java/com/kku/foodshare/config/TimeConfig.java` |
| ค่า CSRF ในแท็ก meta ของ HTML | `code/src/main/resources/templates/fragments.html` |
| รูปแบบข้อผิดพลาดของ REST | `code/src/main/java/com/kku/foodshare/exception/ApiExceptionHandler.java` |
| การตรวจผลตอบกลับของ Comment CRUD | `code/src/test/java/com/kku/foodshare/CommentCrudJourneyTest.java` |
| ตัวรันทดสอบ / Service ทดสอบแยกจากแอป | `scripts/test-phase12.ps1`, `compose.test.yaml` |

