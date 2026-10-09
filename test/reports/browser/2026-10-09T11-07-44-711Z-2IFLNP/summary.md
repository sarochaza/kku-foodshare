# ผลทดสอบ Browser

เวลาในชื่อโฟลเดอร์และ JSON ใช้ UTC / ISO

ผ่าน 7 | ไม่ผ่าน 2 | ไม่ได้รัน 0

| ไฟล์ | ขอบเขต | ผล | Exit code | Log |
|---|---|---|---|---|
| about-image-check | source | PASS | 0 | [เปิด Log](logs/about-image-check.log) |
| admin-posts-check | fixture | PASS | 0 | [เปิด Log](logs/admin-posts-check.log) |
| dark-surfaces-check | fixture | PASS | 0 | [เปิด Log](logs/dark-surfaces-check.log) |
| onboarding-check | fixture | PASS | 0 | [เปิด Log](logs/onboarding-check.log) |
| phase10-ui-check | fixture | PASS | 0 | [เปิด Log](logs/phase10-ui-check.log) |
| pickup-reminder-check | fixture | PASS | 0 | [เปิด Log](logs/pickup-reminder-check.log) |
| browser-journey | live | PASS | 0 | [เปิด Log](logs/browser-journey.log) |
| quick-actions-journey | live | FAIL | 1 | [เปิด Log](logs/quick-actions-journey.log) |
| home-overflow-check | live | FAIL | 1 | [เปิด Log](logs/home-overflow-check.log) |

ภาพหน้าจออยู่ใน screenshots/ และรายงานแต่ละไฟล์อยู่ใน reports/
ไฟล์ที่ไม่ได้สร้างภาพหรือรายงานเพิ่มเติมจะมีโฟลเดอร์ว่าง
PASS ของ fixture/source ไม่ได้ยืนยัน API บนเว็บจริง; เทสที่ไม่ได้เลือกเป็น NOT_RUN
