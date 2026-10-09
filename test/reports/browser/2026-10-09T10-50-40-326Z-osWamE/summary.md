# ผลทดสอบ Browser

เวลาในชื่อโฟลเดอร์และ JSON ใช้ UTC / ISO

ผ่าน 0 | ไม่ผ่าน 1 | ไม่ได้รัน 8

| ไฟล์ | ขอบเขต | ผล | Exit code | Log |
|---|---|---|---|---|
| browser-journey | live | FAIL | 1 | [เปิด Log](logs/browser-journey.log) |
| about-image-check | source | NOT_RUN | — | — |
| admin-posts-check | fixture | NOT_RUN | — | — |
| dark-surfaces-check | fixture | NOT_RUN | — | — |
| onboarding-check | fixture | NOT_RUN | — | — |
| phase10-ui-check | fixture | NOT_RUN | — | — |
| pickup-reminder-check | fixture | NOT_RUN | — | — |
| quick-actions-journey | live | NOT_RUN | — | — |
| home-overflow-check | live | NOT_RUN | — | — |

ภาพหน้าจออยู่ใน screenshots/ และรายงานแต่ละไฟล์อยู่ใน reports/
ไฟล์ที่ไม่ได้สร้างภาพหรือรายงานเพิ่มเติมจะมีโฟลเดอร์ว่าง
PASS ของ fixture/source ไม่ได้ยืนยัน API บนเว็บจริง; เทสที่ไม่ได้เลือกเป็น NOT_RUN
