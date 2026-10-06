# KKU FoodShare

เว็บไซต์ภาษาไทยสำหรับแบ่งปันอาหารในชุมชน มข. ใช้ Spring Boot, PostgreSQL, Thymeleaf และ Leaflet รองรับโทรศัพท์และโน้ตบุ๊ก พร้อมระบบสมาชิก โพสต์รูปและพิกัดจริง จองอาหาร แก้จำนวน ยกเลิก และรหัสรับอาหาร 6 หลัก

## เริ่มใช้งานด้วย Docker

ต้องมี Docker Engine/Desktop พร้อม Compose v2 และอินเทอร์เน็ตสำหรับดาวน์โหลด dependency ครั้งแรก

```bash
# macOS / Linux / Git Bash
bash scripts/setup-env.sh
# Windows PowerShell: powershell -ExecutionPolicy Bypass -File scripts/setup-env.ps1

docker compose up --build -d
docker compose ps
```

เปิด **http://localhost:8080** แล้วสมัครสมาชิก ไม่มีบัญชีหรือรหัสผ่านเริ่มต้นที่ฝังไว้ ระบบสร้างตารางด้วย Flyway ให้อัตโนมัติ รูปอาหารและฐานข้อมูลเก็บใน Docker volumes และอยู่ต่อหลัง restart

- หยุดชั่วคราว: `docker compose down` (ข้อมูลยังอยู่)
- ตรวจ log: `docker compose logs -f app`
- สุขภาพระบบ: `/actuator/health`
- Swagger: `/swagger-ui/index.html`, OpenAPI JSON: `/v3/api-docs`

อย่าใช้ `docker compose down -v` หากต้องการเก็บข้อมูล เพราะคำสั่งนี้ลบ volumes

## Deploy ขึ้นเซิร์ฟเวอร์จริง

เตรียม VPS ที่รัน Docker ได้ มี RAM เริ่มต้นประมาณ 2 GB และพื้นที่ถาวร โดเมนชี้ A/AAAA record มายังเซิร์ฟเวอร์ และเปิด port 80/443

1. นำโค้ดขึ้นเครื่อง แล้วรัน setup-env เพียงครั้งแรก
2. ใส่ `DOMAIN=foodshare.example.org` เป็นโดเมนจริงใน `.env`
3. รันคำสั่งด้านล่าง Caddy จะขอ TLS certificate และตั้ง HTTPS ให้

```bash
docker compose -f compose.yaml -f compose.production.yaml up --build -d
```

เว็บจะอยู่ที่ `https://<DOMAIN>` production override ตั้ง secure cookie และ base URL ให้แล้ว แอปพอร์ต 8080 ผูกเฉพาะ loopback; ฐานข้อมูลไม่เปิดพอร์ตออกภายนอก เก็บ `.env` เป็นความลับและสำรอง `APP_SECRET` คู่กับข้อมูล รหัสนี้ใช้ถอดรหัสรับอาหารเดิม การเปลี่ยนโดยไม่มีแผนย้ายข้อมูลทำให้รหัสที่ยังค้างอ่านไม่ได้

โครงการนี้เตรียมสำหรับ **แอป 1 instance** เนื่องจาก session, rate limiter อยู่ในหน่วยความจำ และรูปเก็บบน volume หากต้อง scale หลาย instance ให้ย้าย session/rate limit ไป shared store และรูปไป object storage ก่อน

แพ็กเกจนี้ยังไม่ได้ publish ไปยังบัญชี cloud ของเจ้าของโครงการ จึงยังไม่มี public URL ของระบบจริง

## ตั้งบัญชีผู้ดูแล

สมัครสมาชิกจากหน้าเว็บก่อน แล้วกำหนดสิทธิ์ให้ **อีเมลของคุณเอง** ในฐานข้อมูล:

```bash
docker compose exec db psql -U foodshare -d foodshare
```

```sql
UPDATE users SET role='ADMIN', version=version+1 WHERE lower(email)=lower('your-real-email@example.org');
```

ออกจากระบบและเข้าใหม่ หน้า “บัญชีของฉัน” จะแสดงเมนู “จัดการชุมชน” ผู้ดูแลตรวจรายงาน ปิดโพสต์ และระงับบัญชีได้ โดยต้องระบุเหตุผลและมี audit record

## Google Login และรีเซ็ตรหัสผ่าน

Email/password login ทำงานได้โดยไม่ต้องตั้ง Google หรือ SMTP ส่วนปุ่ม Google จะแสดงเมื่อเปิด profile `google` เท่านั้น และหน้าลืมรหัสผ่านจะแจ้งว่าระบบอีเมลยังไม่พร้อมหากไม่ได้ตั้ง SMTP

- Google: ใส่ `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` และเพิ่ม `google` ใน `SPRING_PROFILES_ACTIVE`
- Authorized redirect URI: `https://<DOMAIN>/login/oauth2/code/google` (local: `http://localhost:8080/login/oauth2/code/google`)
- SMTP: ใส่ `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` แล้วเพิ่ม `mail` ใน profiles
- เปิดทั้งคู่: `SPRING_PROFILES_ACTIVE=google,mail` แล้ว restart app
- Reset token อายุ 15 นาที เก็บเฉพาะ hash จำกัดส่งซ้ำบัญชีเดิม 5 นาที และคำขอสาธารณะ 5 ครั้งต่อ IP ต่อ 15 นาที

ยังไม่ได้ตรวจการส่ง SMTP หรือ Google callback ด้วยบัญชีจริง เพราะไม่มี credentials ของเจ้าของระบบในแพ็กเกจ

## วิธีใช้งานหลัก

1. ผู้แบ่งปันสมัครสมาชิก → “แบ่งปันอาหาร” → กรอกจำนวน เวลารับ รายละเอียด/สารก่อภูมิแพ้ → แตะแผนที่หรือลากหมุด → แนบ JPG/PNG → เผยแพร่
2. ผู้รับค้นหา/กรองอาหาร ดูแผนที่ หรือใช้ตำแหน่งปัจจุบัน แล้วกดจอง ระบุจำนวน ยืนยัน
3. “การจองของฉัน” แสดงรหัส 6 หลัก แก้จำนวน/ยกเลิก และเปิดเส้นทางได้
4. ผู้แบ่งปันเปิด “โพสต์ของฉัน” → ดูผู้จอง → กรอกรหัสของผู้รับเพื่อยืนยันส่งมอบ
5. เมื่อหมดเวลารับ การจองค้างจะหมดอายุอัตโนมัติภายในประมาณ 60 วินาที

การจองหนึ่งคนต่อโพสต์มีได้หนึ่งรายการที่ยังใช้งาน ป้องกันจองอาหารตนเอง จำนวนจะหักทันทีเมื่อจอง และคืนเมื่อยกเลิกครั้งแรกเท่านั้น ผู้แบ่งปันเปลี่ยนเวลา/จุดรับไม่ได้ขณะมีผู้จอง

GPS ต้องได้รับสิทธิ์จากผู้ใช้และใช้งานบน HTTPS หรือ localhost หากปฏิเสธสิทธิ์ ยังแตะแผนที่หรือกรอกพิกัดเองได้ แผนที่ออนไลน์ใช้ OpenStreetMap และต้องมีอินเทอร์เน็ต สามารถตั้ง `MAP_TILE_URL` และ `MAP_ATTRIBUTION` ใน environment เมื่อเปลี่ยนผู้ให้บริการ แผนที่แสดงสูงสุด 200 จุดต่อคำค้น

## พัฒนาโดยไม่ใช้ Docker

ติดตั้ง JDK 17+ และ PostgreSQL 17 สร้างฐานข้อมูลว่างชื่อ foodshare แล้วตั้ง `DATABASE_URL` (รูปแบบ JDBC), `DATABASE_USER`, `DATABASE_PASSWORD`, `APP_SECRET` อย่างน้อย 32 ตัวอักษร และ `UPLOAD_DIR` ที่เขียนได้

```bash
cd code
./mvnw test
./mvnw spring-boot:run
# Windows ใช้ mvnw.cmd
```

`application.properties` ไม่เก็บ credentials จริง ตารางต้องตรง migration; ไม่ใช้ ddl-auto=update ใน production หากมีฐานข้อมูลเดิมก่อนแพ็กเกจนี้ ให้สำรองและย้ายข้อมูลไป schema ใหม่ใน staging ก่อน ห้าม baseline/ลบตารางจริงเพื่อให้ startup ผ่านโดยไม่ตรวจข้อมูล

เวลานัดรับใน API และหน้าจอใช้ **Asia/Bangkok (UTC+7)** รูปอาหารสูงสุด 5 MB; decode/re-encode เป็น JPEG และเก็บชื่อ UUID รูปโปรไฟล์สูงสุด 2 MB

## โครงสร้างและเอกสาร

- `code/` — Spring Boot source, migration, JUnit/Mockito tests และ frontend
- `test/` — วิธีตรวจระบบและ browser journey
- `doc/architecture.md` — สถาปัตยกรรม, SOLID/GoF, schema และ UML/Mermaid
- `doc/deployment.md` — deploy, สำรอง/กู้คืน และ troubleshooting
- `doc/api.md` — API/session/CSRF contract
- `doc/demo-script.md` — ลำดับนำเสนอและ slide outline
- `img/` — ภาพหน้าจอจากการตรวจ UI
- `docs/superpowers/` — design ที่อนุมัติ แผน และบันทึกงาน

รายชื่อทีม รหัสนักศึกษา และลิงก์ repository ให้เจ้าของโครงการกรอกตามจริงก่อนส่งงาน ไม่ได้สร้างชื่อผู้ร่วมงานหรือประวัติ commit เพิ่มเติม
