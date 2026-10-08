# Design patterns

## Enterprise / Architectural patterns — 6 แบบ

| Pattern | ปัญหาที่แก้ | ไฟล์/คลาสที่ใช้ |
|---|---|---|
| Layered Architecture | แยก HTTP, business rules และ data access | MemberProfileController → MemberProfileServiceImpl → UserRepository → User |
| MVC | แยกหน้า UI จากการจัดการ request และข้อมูล model | HomeController, PageController, Thymeleaf templates |
| Repository | ติดต่อฐานข้อมูลผ่าน contract โดยไม่ฝัง SQL ใน Controller | FoodPostRepository, ReservationRepository, UserRepository |
| Service Layer | รวม authorization, transaction และ business operations | FoodCatalogServiceImpl, ReservationServiceImpl, OwnerStockServiceImpl |
| DTO + Mapper | API ส่งเฉพาะข้อมูลตาม contract โดยไม่ serialize Entity ทั้งก้อน | MemberProfileResponse, PostView, PostViewContext, PostViewMapper, mapping interfaces |
| Dependency Injection | เปลี่ยน implementation/ทดสอบแยกได้โดยไม่ new dependency ในผู้เรียก | constructor injection; ImageStorage กับ local qualifier |

## GoF — กลุ่ม Behavioral จำนวน 3 แบบ

| Pattern | ปัญหาที่แก้ | ไฟล์/คลาสที่ใช้ |
|---|---|---|
| Strategy | เปลี่ยนลำดับค้นหาโดยไม่ขยาย service if-else | FoodDiscoveryStrategy, ExpiryFoodStrategy, LatestFoodStrategy, NearbyFoodStrategy, FoodCatalogServiceImpl |
| Observer | ผู้ทำ business action ไม่ต้องเก็บ notification เองทุกจุด | ActivityNotice/PostClosed, ApplicationEventPublisher, NotificationServiceImpl.notify, ReservationServiceImpl.closed |
| State | การจองที่สิ้นสุดต้องปฏิเสธการเปลี่ยนข้อมูล ขณะที่ RESERVED อนุญาต | ReservationState, ReservationStatus.requireMutable, ReservationServiceImpl |

State ของโค้ดนี้เป็น enum ที่ implement behavior ของ ReservationState ใช้ guard polymorphism ก่อนเปลี่ยนข้อมูล การ transition และปรับ stock ยังอยู่ใน service ไม่อ้างว่าเป็นระบบ State objects ที่จัดการ transition ทั้งหมด Observer ใช้ Spring events แบบ synchronous ใน process ไม่ใช่ message queue หรือ mobile push และไม่ใช่ notification ทุกชนิดเกิดผ่าน event

## Class Diagram

ดู [Diagram รวม](diagrams/README.md) และ source `diagrams/class.mmd` ซึ่งแสดง stereotype และความสัมพันธ์ของทั้งสาม Behavioral patterns

| Pattern | ตำแหน่งอ้างอิงเพิ่มเติม |
|---|---|
| Strategy | `code/src/main/java/com/kku/foodshare/service/impl/FoodCatalogServiceImpl.java`:256 |
| Observer | `code/src/main/java/com/kku/foodshare/service/impl/NotificationServiceImpl.java`:37 |
| State | `code/src/main/java/com/kku/foodshare/domain/entity/ReservationStatus.java`:6 |
