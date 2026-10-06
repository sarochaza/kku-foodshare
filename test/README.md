> ผลและคำสั่งของรุ่นล่าสุด: [PHASE5-VERIFICATION.md](PHASE5-VERIFICATION.md)

# Tests

## JUnit / Mockito / H2

```bash
cd code
./mvnw clean verify
```

ผลอยู่ใน `code/target/surefire-reports`. H2 ใช้เฉพาะ tests และปิด Flyway โดย test properties; application runtime ใช้ PostgreSQL + Flyway + ddl validation

## PostgreSQL จริง

CI `.github/workflows/verify.yml` รัน migration และ integration tests กับ PostgreSQL 17 รวมการจองพร้อมกัน เพื่อแยกจาก H2. หากทดสอบด้วยตนเอง ให้ใช้ database ทดสอบว่างเท่านั้น แล้ว override datasource, `spring.flyway.enabled=true`, `spring.jpa.hibernate.ddl-auto=validate` ตามคำสั่งใน workflow

## Browser journey

ใช้ Node.js 20+ และแอปที่กำลังรันกับ **ฐานข้อมูลทดสอบเท่านั้น** สคริปต์สร้างสมาชิกและรายการที่ระบุว่าเป็นข้อมูลทดสอบ ไม่ใช้กับ production

```bash
cd test
npm install
npx playwright install chromium
npm run browser
```

ค่าเริ่มต้น `http://127.0.0.1:8080`. เปลี่ยนผ่าน `TEST_BASE_URL`. Linux/macOS ใช้ `TEST_BASE_URL=http://... npm run browser`; PowerShell ใช้ `$env:TEST_BASE_URL='http://...'; npm run browser`

ทดสอบสมัคร/เข้าสู่ระบบสองบัญชี → โพสต์รูปและ pin → ค้นหาและแผนที่ → จอง/แก้จำนวน → รหัสรับและส่งมอบ → โปรไฟล์/การแจ้งเตือน → map/GPS failure recovery → responsive 4 ขนาด สคริปต์ปิด test food post เมื่อสำเร็จ แต่คงประวัติไว้เป็นหลักฐาน

ภาพและผลอยู่ใน `img/`. ปรับ `BROWSER_EXECUTABLE` เฉพาะเมื่อใช้ Chromium ที่ติดตั้งไว้ต่างจากค่าเริ่มต้น

## Coverage ที่มีความหมาย

- Stock / authorization / idempotency / parallel bookings: `FoodJourneyTest`
- Stale user update ไม่เปิดบัญชีที่ถูกระงับกลับ: `ReviewRegressionTest`
- Disabled SMTP / SMTP outage ไม่เปิดเผยสมาชิก: `ReviewRegressionTest`, `PasswordResetProtectionTest`
- Token cooldown และ IP limiter: `PasswordResetProtectionTest`
- Public page rendering, CSRF และ guest redirect: `WebPagesTest`
- Mapper, repository, OAuth และ controller unit tests เดิมที่ยังเกี่ยวข้องคงไว้

เทสต์เดิมที่ตรวจชื่อ CSS หรือข้อความ source HTML แบบตายตัวถูกแทนที่ด้วยการ render HTTP และ browser behavior ไม่ใช้จำนวนเทสต์เป็นหลักฐานว่าไม่มีบั๊ก
