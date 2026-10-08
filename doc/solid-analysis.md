# SOLID analysis

หลักฐานอ้างอิง source ใน ZIP ชุดนี้ ตรวจเลขบรรทัดใหม่เมื่อแก้โค้ดก่อนส่ง

| หลักการ | ไฟล์และบรรทัด | เหตุผล |
|---|---|---|
| S — Single Responsibility | `code/src/main/java/com/kku/foodshare/controller/api/MemberProfileController.java`:9; `code/src/main/java/com/kku/foodshare/service/impl/MemberProfileServiceImpl.java`:12; `code/src/main/java/com/kku/foodshare/mapper/PostViewMapper.java`:10; `code/src/main/java/com/kku/foodshare/service/impl/PostViewServiceImpl.java`:14 | Controller รับ HTTP; Service อ่านและตรวจสมาชิก; Mapper แปลงข้อมูลโดยไม่เรียก Repository; PostViewService โหลดข้อมูลประกอบ DTO |
| O — Open/Closed | `code/src/main/java/com/kku/foodshare/service/discovery/FoodDiscoveryStrategy.java`:6; `code/src/main/java/com/kku/foodshare/service/impl/FoodCatalogServiceImpl.java`:256 | เพิ่มวิธีเรียงอาหารด้วย implementation ของ Strategy และ key ใหม่โดยไม่เพิ่ม if-else ใน service selector; UI/API docs ต้องประกาศตัวเลือกใหม่เมื่อเปิดให้ผู้ใช้เลือก |
| L — Liskov Substitution | `code/src/main/java/com/kku/foodshare/service/discovery/LatestFoodStrategy.java`:8; `code/src/main/java/com/kku/foodshare/service/discovery/ExpiryFoodStrategy.java`:8; `code/src/main/java/com/kku/foodshare/service/discovery/NearbyFoodStrategy.java`:9 | implementations ให้ Criteria Order ตาม contract เดียวกัน; nearby ต้องมี lat/lng ภายในช่วง จึงเป็น precondition ที่ต้องระบุให้ผู้เรียกทราบ ไม่อ้างว่า null coordinates ใช้ได้ทุก strategy |
| I — Interface Segregation | `code/src/main/java/com/kku/foodshare/service/MemberProfileService.java`:5; `code/src/main/java/com/kku/foodshare/service/OwnerStockService.java`:5; `code/src/main/java/com/kku/foodshare/service/QrScanService.java`:3; `code/src/main/java/com/kku/foodshare/service/PickupReminderService.java`:3 | แยก interface ตามผู้ใช้บริการ: โปรไฟล์, stock, QR, reminder ไม่ให้ผู้เรียกบังคับรับความสามารถที่ไม่ใช้ |
| D — Dependency Inversion | `code/src/main/java/com/kku/foodshare/service/impl/NotificationServiceImpl.java`:27; `code/src/main/java/com/kku/foodshare/service/storage/CloudinaryImageStorage.java`:35; `code/src/main/java/com/kku/foodshare/service/impl/PostViewServiceImpl.java`:19 | ผู้เรียกพึ่ง interface ของบริการ/mapper/storage และใช้ constructor injection; qualifier ระบุ local implementation เพื่อหลีกเลี่ยงเลือก primary Cloudinary กลับเข้าตนเอง |

## ขอบเขตและเหตุผล

Repository interfaces เป็น Spring Data JPA; Clock และ ApplicationEventPublisher เป็น abstractions ของ runtime/framework ไม่เพิ่ม interface ครอบเพื่อเพิ่มจำนวนไฟล์เฉย ๆ `@Autowired` ใน Brevo/Cloudinary อยู่บน constructor ส่วน Account/FoodPost/UserProfile mapper เดิม implement mapping interfaces และ services รับ interface เหล่านั้น

Business validation เช่น เจ้าของโพสต์ จำนวนอาหาร เวลา สถานะ และ concurrency อยู่ใน service/domain ส่วน Bean Validation ตรวจรูปแบบ request ที่ขอบ HTTP การ persistence อยู่หลัง repository interface ภายใน transaction การตรวจไฟล์รูปยังอยู่ใน storage boundary เพื่อป้องกันรับ bytes ที่ไม่รองรับ

Interface ของ Strategy มี contract ของ preconditions ระบุใน source แล้ว; การตรวจ bounds อยู่ใน catalog service คำขอ sort=nearby ไม่มี coordinates ตอบ 400 ตาม behavior เดิม
