# Deployment and operations

## ค่าที่ต้องเตรียม

| ค่า | ต้องใช้เมื่อ | ความหมาย |
|---|---|---|
| DATABASE_URL/USER/PASSWORD | รัน Java โดยตรง | JDBC PostgreSQL; Docker Compose จัดการให้ |
| APP_SECRET | ทุก environment | สุ่มอย่างน้อย 32 ตัวอักษร เก็บถาวรเพื่อถอดรหัสรับอาหาร |
| UPLOAD_DIR | Java โดยตรง | โฟลเดอร์ถาวรที่แอปเขียนได้ |
| DOMAIN | Production Compose | ชื่อโดเมนจริง ไม่ใส่ https หรือ path |
| APP_BASE_URL | Java โดยตรง / SMTP | URL สาธารณะที่ถูกต้องของระบบ |
| COOKIE_SECURE | HTTPS | ตั้ง true; production Compose ตั้งให้ |
| SPRING_PROFILES_ACTIVE | ทางเลือก | google, mail หรือ google,mail |
| MAP_TILE_URL/ATTRIBUTION | ทางเลือก | ผู้ให้บริการ tiles และเครดิตที่ต้องแสดง |

Production ใช้ reverse proxy ที่เชื่อถือได้และปิดการเข้าถึง app port จากอินเทอร์เน็ต เพื่อให้ forwarded headers และ IP throttle ถูกต้อง ไม่ต้องเปิด PostgreSQL port ออกภายนอก

## ตรวจหลัง deploy

1. `/actuator/health` ตอบ UP ผ่าน HTTPS
2. สมัครผู้แบ่งปันและผู้รับเป็นคนละบัญชี แล้วทดสอบโพสต์รูป + พิกัด → จอง → ยกเลิก → จองใหม่ → กรอกรหัสรับ
3. เปิดบนโทรศัพท์จริง ตรวจ GPS permission และเส้นทาง
4. Restart app แล้วรูป/โพสต์/ประวัติยังอยู่ (session login ใหม่ได้)
5. หากเปิด Google/SMTP ให้ทดสอบ callback และอีเมลรีเซ็ตด้วยบัญชีจริง
6. กำหนดแอดมิน ตรวจรายงาน และทดสอบการคืนจำนวนเมื่อปิดโพสต์

## Backup

เก็บฐานข้อมูล รูปอาหาร และ `.env`/APP_SECRET ที่เข้ารหัสหรืออยู่ใน secret manager ให้เป็นชุดเดียวกัน สำรองก่อนอัปเกรดทุกครั้ง:

```bash
mkdir -p backups
docker compose exec -T db pg_dump -U foodshare -d foodshare -Fc > backups/foodshare.dump
docker compose cp app:/app/uploads backups/uploads
```

ทำช่วงบำรุงรักษา/หยุดการเขียนเพื่อให้ DB กับรูปตรงกัน และคัดลอก backup ไปพื้นที่อีกเครื่อง ทดสอบ restore ที่ staging เป็นระยะ

## Restore ไป environment ว่าง

ตั้ง `.env` ด้วย APP_SECRET ชุดเดิม เปิด DB อย่างเดียว แล้ว restore ก่อนเปิด app:

```bash
docker compose up -d db
# รอ healthcheck ผ่านก่อน
docker compose exec -T db pg_restore -U foodshare -d foodshare --no-owner < backups/foodshare.dump
docker compose up -d app
docker compose cp backups/uploads/. app:/app/uploads/
```

ตรวจสิทธิ์ไฟล์ใน volume ให้ UID/GID 10001 อ่าน/เขียนได้ เครื่องมือ restore ต้องใช้ database ว่างและ backup ที่ตรวจสอบแล้ว ไม่รัน `--clean` กับฐานข้อมูลที่ยังมีงานใช้งาน

## อัปเกรด

สำรองข้อมูล → build image ใหม่ → `docker compose up --build -d` → ตรวจ health/log/journey. Flyway ตรวจ checksum และรัน migration ใหม่ตามลำดับ ไม่แก้ไฟล์ migration ที่ใช้ไปแล้ว หากอัปเกรดผิดให้หยุดและประเมิน restore ทั้ง DB/images จากชุดเดียวกัน; อย่า downgrade app บน schema ที่เข้ากันไม่ได้

## ปัญหาที่พบบ่อย

- Startup บอก APP_SECRET: ตั้ง secret อย่างน้อย 32 ตัวอักษร
- Connection refused: ตรวจ DB health และ JDBC URL; host ใน Compose คือ `db`
- Flyway existing schema: ฐานข้อมูลเก่าต้องวางแผนย้ายข้อมูล; ห้ามลบข้อมูลจริงเพื่อแก้ startup
- Session หายเมื่อ restart: เป็นพฤติกรรม session ในหน่วยความจำ ข้อมูลสมาชิก/โพสต์อยู่ใน DB
- GPS ไม่ขึ้น: ใช้ HTTPS/localhost ตรวจสิทธิ์เบราว์เซอร์ หรือกรอกพิกัดเอง
- Map tile โหลดไม่ได้: ตรวจ network/provider; ใช้ปุ่มลองใหม่หรือเปิด Google Maps จากพิกัด
- อีเมลไม่ส่ง: เปิด `mail` profile ตรวจ SMTP log และ sender ที่ provider อนุญาต
- 403 หลังเปิดหน้าทิ้งไว้: session/CSRF หมดอายุ ให้เข้าสู่ระบบใหม่และโหลดหน้าฟอร์มใหม่
- รหัสรับอ่านไม่ได้หลังเปลี่ยน secret: คืน APP_SECRET ชุดเดิมจาก backup แล้ว restart

## ข้อจำกัดการตรวจในสภาพแวดล้อมพัฒนา

ไม่ได้รัน Docker daemon หรือเผยแพร่ production จากสภาพแวดล้อมนี้ มี CI สำหรับ PostgreSQL 17 มาตรฐานและ Docker build ให้รันเมื่อ push repository ส่วนผลที่รันจริงระบุใน `test/verification.md` โดยแยก H2, PGlite และ browser ออกจากกัน
