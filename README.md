# KKU FoodShare

ระบบ **KKU FoodShare** สำหรับการแบ่งปันอาหารภายในมหาวิทยาลัย ช่วยให้ผู้ใช้งานสามารถแบ่งปันอาหารที่ต้องการส่งต่อ และช่วยลดปัญหาอาหารเหลือทิ้ง โดยเน้นการออกแบบระบบตามหลักการ Software Design

## สมาชิกกลุ่ม

| รหัสนักศึกษา | ชื่อ-นามสกุล          | Email                                                     | Section |
| ------------ | --------------------- | --------------------------------------------------------- | :-----: |
| 673380026-6  | นางสาวกัญญาวี ศรีเหรา | [kanyawi.s@kkumail.com](mailto:kanyawi.s@kkumail.com)     |    1    |
| 673380289-4  | นางสาวรสริน เมืองหงษ์ | [rossarin.m@kkumail.com](mailto:rossarin.m@kkumail.com)   |    1    |
| 673380296-7  | นางสาวสโรชา เสาทอง    | [sarocha.sao@kkumail.com](mailto:sarocha.sao@kkumail.com) |    1    |
| 673380048-7  | นายปวริศร์ แพงมา | [pawarit.pan@kkumail.com](mailto:[pawarit.pan@kkumail.com)     |    1    |

## หัวข้อโปรเจค

**KKU FoodShare – ระบบแบ่งปันอาหารภายในมหาวิทยาลัย**

## รายละเอียดโปรเจค

KKU FoodShare เป็นระบบที่พัฒนาขึ้นเพื่อสนับสนุนการแบ่งปันอาหารระหว่างนักศึกษาและบุคลากรภายในมหาวิทยาลัย โดยผู้ใช้งานสามารถนำอาหารที่ต้องการแบ่งปันมาเผยแพร่ให้ผู้อื่นรับไปใช้ประโยชน์ ช่วยลดการสูญเสียอาหารและส่งเสริมการใช้ทรัพยากรอย่างคุ้มค่า

# 🍱 FoodShare

FoodShare คือเว็บแอปพลิเคชันสำหรับแบ่งปันอาหารส่วนเกิน ช่วยให้ผู้ที่มีอาหารสามารถส่งต่อให้ผู้ที่ต้องการ และช่วยลดปริมาณอาหารที่ถูกทิ้ง

## 📌 สถานะโครงการ

โปรเจกต์มีระบบบัญชีผู้ใช้ หน้าเว็บหลัก Dashboard และแผนที่แสดงรายการอาหารบางส่วนแล้ว ขณะนี้ยังอยู่ระหว่างพัฒนา กระบวนการหลักอย่างการสร้างโพสต์และการขอรับอาหารให้ครบวงจรยังต้องทำต่อ

> สถานะนี้สรุปจากโค้ดที่มีในโปรเจกต์ อาจมีงานเพิ่มเติมอยู่ใน branch อื่นที่ยังไม่ได้รวมเข้ามา

## ✅ สิ่งที่ทำแล้ว

- วางโครงสร้าง Backend แยกเป็น Controller, Service, Repository, Entity, DTO และ Mapper
- สร้างหน้า Landing Page, สมัครสมาชิก, เข้าสู่ระบบ, ลืมรหัสผ่าน และ Dashboard
- รองรับการสมัครสมาชิกด้วยอีเมลและรหัสผ่าน พร้อมตรวจสอบข้อมูลและเข้ารหัสรหัสผ่าน
- รองรับการเข้าสู่ระบบด้วยอีเมลและ Google OAuth2
- ทำระบบรีเซ็ตรหัสผ่านผ่านอีเมล โดยใช้ token ที่มีวันหมดอายุ
- แสดงข้อมูลโปรไฟล์ผู้ใช้บน Dashboard
- ทำ API สำหรับอ่านข้อมูลโพสต์อาหารเพื่อแสดงบนแผนที่
- ใช้ Leaflet แสดงตำแหน่งอาหารบนแผนที่
- มีชุดทดสอบสำหรับ Controller, Service, Repository, Mapper, Entity และ Security บางส่วน

## 🛠️ สิ่งที่ยังต้องทำ

- ทำระบบโพสต์อาหารให้ครบ: สร้าง ดูรายละเอียด แก้ไข และลบ
- เพิ่มการอัปโหลดรูปภาพอาหาร
- เพิ่มระบบค้นหาและกรองรายการอาหาร
- ทำระบบคำขอรับอาหาร ตั้งแต่ส่งคำขอจนถึงอนุมัติหรือปฏิเสธ
- จัดการจำนวนอาหารและป้องกันการจองซ้ำ
- เพิ่มการยืนยันว่ารับอาหารสำเร็จ
- เพิ่มประวัติการแบ่งปันและประวัติการรับอาหาร
- เพิ่มระบบรีวิว คะแนน และรายงานปัญหา
- เพิ่มเครื่องมือสำหรับผู้ดูแลระบบตามขอบเขตที่ทีมตกลง
- เพิ่ม Global Exception Handler, Pagination และ Swagger/OpenAPI
- เพิ่ม Database migration เพื่อควบคุมการเปลี่ยนแปลงโครงสร้างฐานข้อมูล
- ทดสอบระบบตั้งแต่ต้นจนจบ และเตรียมการ Deploy

## 💻 เทคโนโลยีที่ใช้

- Java 17
- Spring Boot
- Spring MVC และ Thymeleaf
- Spring Security
- Spring Data JPA
- PostgreSQL
- Google OAuth2
- SMTP สำหรับส่งอีเมล
- Leaflet สำหรับแผนที่
- JUnit และ Mockito สำหรับทดสอบ

## 📂 โครงสร้างโปรเจกต์

```text
foodshare/
├── README.md
└── code/
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/com/kku/foodshare/
        │   │   ├── config/
        │   │   ├── controller/
        │   │   ├── domain/
        │   │   ├── dto/
        │   │   ├── mapper/
        │   │   ├── repository/
        │   │   ├── security/
        │   │   └── service/
        │   └── resources/
        │       ├── static/
        │       └── templates/
        └── test/
            └── java/com/kku/foodshare/



## 🗄️ ตั้งค่า PostgreSQL

### 1. สร้างฐานข้อมูล

เปิด pgAdmin หรือ `psql` แล้วสร้างฐานข้อมูลชื่อ `foodshare_db`:

```sql
CREATE DATABASE foodshare_db;
```

ค่าเริ่มต้นใน `application.properties` ใช้การเชื่อมต่อดังนี้:

| รายการ | ค่า |
|---|---|
| Host | `localhost` |
| Port | `5432` |
| Database | `foodshare_db` |
| Username | `postgres` |
| Password | รหัสผ่าน PostgreSQL ของคุณ |

ถ้าใช้ username หรือ port ต่างจากตัวอย่าง ให้แก้ `spring.datasource.url` และ `spring.datasource.username` ในไฟล์ `src/main/resources/application.properties` ให้ตรงกับเครื่องของคุณ

### 2. ตั้งค่ารหัสผ่านฐานข้อมูล

โปรเจกต์อ่านรหัสผ่านจาก Environment Variable ชื่อ `DB_PASSWORD` อย่าใส่รหัสผ่านจริงลง Git

**Windows PowerShell** — ใช้ได้ในหน้าต่าง Terminal ปัจจุบัน:

```powershell
$env:DB_PASSWORD="รหัสผ่าน PostgreSQL ของคุณ"
```

**macOS/Linux:**

```bash
export DB_PASSWORD="รหัสผ่าน PostgreSQL ของคุณ"
```

เมื่อตั้งค่าถูกต้อง Spring Boot จะเชื่อมต่อฐานข้อมูล และ Hibernate จะสร้างหรือปรับตารางตาม Entity ในโปรเจกต์

## 🔐 ตั้งค่า Google OAuth2

หากต้องการเข้าสู่ระบบด้วย Google ต้องสร้าง OAuth Client ของตัวเองใน Google Cloud Console ก่อน Google Login จะใช้งานไม่ได้หากยังไม่ได้ตั้งค่า Client ID และ Client Secret

### 1. สร้าง OAuth Client

1. เปิด [Google Cloud Console](https://console.cloud.google.com/)
2. สร้างหรือเลือก Google Cloud Project
3. ตั้งค่า OAuth consent screen ตามขั้นตอนของ Google
4. ไปที่ **Credentials** แล้วสร้าง OAuth Client ID
5. เลือกประเภทแอปเป็น **Web application**
6. เพิ่ม Authorized redirect URI สำหรับการรันในเครื่อง:

```text
http://localhost:8080/login/oauth2/code/google
```

7. บันทึก **Client ID** และ **Client Secret** ไว้เป็นความลับ

ชื่อเมนูใน Google Cloud Console อาจเปลี่ยนแปลงได้ตามเวอร์ชันของ Google

### 2. ตั้งค่า Environment Variables

กำหนดค่าต่อไปนี้ใน Terminal ก่อนเปิดแอป:

**Windows PowerShell:**

```powershell
$env:GOOGLE_CLIENT_ID="Client ID ของคุณ"
$env:GOOGLE_CLIENT_SECRET="Client Secret ของคุณ"
```

**macOS/Linux:**

```bash
export GOOGLE_CLIENT_ID="Client ID ของคุณ"
export GOOGLE_CLIENT_SECRET="Client Secret ของคุณ"
```

## ✉️ ตั้งค่าอีเมลสำหรับ Password Reset

ระบบลืมรหัสผ่านใช้ SMTP ส่งลิงก์สำหรับตั้งรหัสผ่านใหม่ ต้องตั้งค่าบัญชีอีเมลของตัวเองก่อน มิฉะนั้นการส่งอีเมลรีเซ็ตรหัสผ่านจะไม่ทำงาน

ตัวอย่างนี้ใช้ Gmail:

1. เปิดการยืนยันแบบ 2 ขั้นตอน (2-Step Verification) ให้บัญชี Google
2. สร้าง **App Password** สำหรับแอป อย่าใช้รหัสผ่าน Gmail ปกติ
3. เก็บอีเมลและ App Password ไว้เป็นความลับ

กำหนด Environment Variables:

**Windows PowerShell:**

```powershell
$env:MAIL_USERNAME="อีเมล Gmail ของคุณ"
$env:MAIL_PASSWORD="App Password ของคุณ"
$env:APP_BASE_URL="http://localhost:8080"
```

**macOS/Linux:**

```bash
export MAIL_USERNAME="อีเมล Gmail ของคุณ"
export MAIL_PASSWORD="App Password ของคุณ"
export APP_BASE_URL="http://localhost:8080"
```

`APP_BASE_URL` ใช้สร้างลิงก์รีเซ็ตรหัสผ่าน เมื่อนำระบบขึ้นใช้งานจริง ให้เปลี่ยนเป็น URL ของระบบที่ Deploy แล้ว

> อย่าใส่ Client Secret, App Password หรือรหัสผ่านฐานข้อมูลลงใน Repository

## ▶️ วิธีรันโปรเจกต์

ตั้งค่า PostgreSQL และ Environment Variables สำหรับบริการที่ต้องการใช้ก่อน จากนั้นเปิด Terminal ในโฟลเดอร์ `code`

**Windows PowerShell:**

```powershell
.\mvnw.cmd spring-boot:run
```

**macOS/Linux:**

```bash
./mvnw spring-boot:run
```

เปิดเว็บไซต์ที่ [http://localhost:8080/](http://localhost:8080/)
