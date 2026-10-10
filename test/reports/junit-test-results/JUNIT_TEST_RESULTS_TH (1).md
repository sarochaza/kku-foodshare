# ผลทดสอบ JUnit — KKU FoodShare

## สรุปผล

อ้างอิงผลรันใน `junit-test-results.zip` ที่ผู้จัดทำส่งมา ไม่ใช่การรันทดสอบใหม่ในการจัดทำเอกสารนี้

| คลาสทดสอบ | รายการที่รัน | ผ่าน | Failures | Errors | Skipped |
|---:|---:|---:|---:|---:|---:|
| **35** | **124** | **124** | **0** | **0** | **0** |

Log แสดง `BUILD SUCCESS` บน Java 17.0.20.1 และจบรันเวลา `2026-10-08T19:24:00Z` (UTC) หรือ **9 ตุลาคม 2026 เวลา 02:24 น. ตามเวลาประเทศไทย**

ผลแต่ละแถวอ่านจาก `<testcase>` ใน Surefire XML ส่วนคำอธิบายและผลที่คาดหวังสรุปจากโค้ดทดสอบ FoodShare ที่มีให้ตรวจ เทสแบบ parameterized นับแยกตามการรันแต่ละค่า จึงมี 124 รายการรันจาก 122 เมธอดทดสอบ

**ตำแหน่งไฟล์ที่แนะนำ:** วางรายงานนี้ที่ `test/reports/junit-test-results/JUNIT_TEST_RESULTS_TH.md` ในโฟลเดอร์เดียวกับ `surefire/` และ `java-tests-docker.txt` เพื่อให้ลิงก์หลักฐานด้านล่างเปิดได้

## สรุปตามคลาส

| คลาส | สิ่งที่ตรวจ | รายการ | ผ่าน |
|---|---|---:|---:|
| `CommentCrudJourneyTest` | CRUD ความคิดเห็นและสิทธิ์การใช้งาน | 8 | 8 |
| `FoodJourneyTest` | โพสต์อาหาร การจอง สต็อก และรับอาหาร | 27 | 27 |
| `FoodshareApplicationTests` | เริ่มต้นแอปพลิเคชัน | 1 | 1 |
| `MailDeploymentConfigTest` | ค่าตั้งค่าอีเมลและ Docker Compose | 2 | 2 |
| `OfflineMigrationTest` | Migration ของยอดแจกออฟไลน์ | 1 | 1 |
| `OnboardingJourneyTest` | คำแนะนำเริ่มต้นใช้งานแยกตามบัญชี | 5 | 5 |
| `PasswordResetBrevoJourneyTest` | รีเซ็ตรหัสผ่านผ่าน Brevo | 2 | 2 |
| `PasswordResetJourneyTest` | กระบวนการรีเซ็ตรหัสผ่าน | 3 | 3 |
| `PasswordResetSmtpDeliveryTest` | ส่งอีเมลผ่าน SMTP ทดสอบ | 1 | 1 |
| `ReviewRegressionTest` | ป้องกันข้อผิดพลาดเดิมกลับมา | 2 | 2 |
| `SubmissionContractTest` | Layer และสัญญาการทำงานของระบบ | 4 | 4 |
| `WebPagesTest` | เรนเดอร์หน้าเว็บและสิทธิ์เข้าถึง | 11 | 11 |
| `FoodPostApiControllerTest` | API แผนที่อาหาร | 2 | 2 |
| `DashboardControllerTest` | Controller หน้า Dashboard | 3 | 3 |
| `HomeControllerTest` | Controller หน้าแรกและเกี่ยวกับ | 2 | 2 |
| `LoginControllerTest` | Controller หน้าเข้าสู่ระบบ | 1 | 1 |
| `FoodPostTest` | ข้อมูลใน FoodPost Entity | 1 | 1 |
| `UserRoleTest` | บทบาทเริ่มต้นของสมาชิก | 1 | 1 |
| `FoodPostMapperTest` | Mapper ข้อมูลโพสต์สำหรับแผนที่ | 1 | 1 |
| `UserProfileMapperTest` | Mapper โปรไฟล์สมาชิก | 2 | 2 |
| `FoodPostRepositoryTest` | Repository ค้นหาอาหารสำหรับแผนที่ | 1 | 1 |
| `GoogleOAuth2SuccessHandlerTest` | Handler หลัง Google Login | 1 | 1 |
| `AdminPostListingTest` | รายการโพสต์สำหรับแอดมิน | 1 | 1 |
| `BrevoEmailServiceTest` | บริการส่งอีเมลผ่าน Brevo adapter | 1 | 1 |
| `CommentRepliesPersistenceTest` | ความสัมพันธ์คำตอบและ persistence | 1 | 1 |
| `CommentRepliesTest` | ตอบกลับ ลบ และ event แจ้งเตือนความคิดเห็น | 7 | 7 |
| `CustomUserDetailsServiceTest` | โหลดสมาชิกและ authority | 2 | 2 |
| `FoodPostServiceTest` | Service อ่านรายการแผนที่ | 1 | 1 |
| `GoogleUserServiceTest` | ค้นหา/สร้างสมาชิกจาก Google Login | 2 | 2 |
| `PasswordResetProtectionTest` | ความปลอดภัยของการรีเซ็ตรหัสผ่าน | 5 | 5 |
| `PickupReminderIntegrationTest` | แจ้งเตือนก่อนหมดเวลารับอาหาร | 7 | 7 |
| `QrScanServiceTest` | อ่าน QR รับอาหาร | 1 | 1 |
| `UserProfileServiceTest` | Service อ่านโปรไฟล์ | 2 | 2 |
| `CloudinaryImageStorageTest` | จัดเก็บภาพผ่าน Cloudinary adapter | 10 | 10 |
| `ImageStorageConfigurationTest` | เลือกผู้ให้บริการภาพ | 2 | 2 |

## รายละเอียดรายเมธอด

Test ID เป็นรหัสอ้างอิงที่จัดขึ้นสำหรับรายงานนี้ ใช้ลำดับคลาสและลำดับรายการจาก XML ไม่ใช่ชื่อที่ JUnit สร้างให้

### 1. CommentCrudJourneyTest — CRUD ความคิดเห็นและสิทธิ์การใช้งาน

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/CommentCrudJourneyTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.CommentCrudJourneyTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.CommentCrudJourneyTest.txt)

ผลรวมคลาส: **8 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-01-01 | `authenticationAndCsrfAreStillRequired` | สิทธิ์อ่าน/แก้ไขคอมเมนต์และ CSRF | ไม่ล็อกอินได้ 401; แก้ไขโดยไม่มี CSRF ได้ 403; ข้อความเดิมคงอยู่ | ✅ ผ่าน |
| TC-JUNIT-01-02 | `completeCrudUsesCorrectStatusesAndPersistsTheEditedBody` | เพิ่ม อ่าน แก้ไข ลบ และโหลดความคิดเห็นจากฐานข้อมูลใหม่ | POST ได้ 201 พร้อม Location; GET/PUT ได้ 200; ข้อความที่แก้คงอยู่หลัง reload; DELETE ได้ 204 และอ่านรายการที่ลบได้ 404 | ✅ ผ่าน |
| TC-JUNIT-01-03 | `blankOverlongAndMalformedUpdatesLeaveTheOriginalBodyUntouched` | แก้ไขด้วยข้อความว่าง เกิน 800 ตัวอักษร ไม่มีฟิลด์ หรือ JSON ผิด | ได้ 400 และไม่เปลี่ยนข้อความเดิม; ข้อความยาว 800 ตัวอักษรแก้ไขได้ | ✅ ผ่าน |
| TC-JUNIT-01-04 | `onlyTheAuthorCanEditEvenWhenOthersHaveModerationDeletionRights` | สิทธิ์แก้ไขของสมาชิกอื่น เจ้าของโพสต์ และแอดมิน | ผู้ที่ไม่ใช่ผู้เขียนแก้ไขไม่ได้ ได้ 403; สิทธิ์ลบไม่ทำให้มีสิทธิ์แก้ไข | ✅ ผ่าน |
| TC-JUNIT-01-05 | `editingAReplyAfterRootDeletionKeepsTheRemainingThread` | แก้ไขคำตอบหลังลบความคิดเห็นต้นทาง | แก้ไขได้; parentDeleted เป็น true; ไม่แสดงชื่อผู้เขียนต้นทางที่ลบ; คำตอบยังอยู่ | ✅ ผ่าน |
| TC-JUNIT-01-06 | `missingAndDeletedCommentsCannotBeReadOrUpdated` | อ่าน/แก้ไขความคิดเห็นที่ไม่มีอยู่หรือถูกลบ | GET และ PUT ได้ 404 | ✅ ผ่าน |
| TC-JUNIT-01-07 | `updateCannotMoveAReplyChangeItsAuthorOrCreationTime` | พยายามเปลี่ยนผู้เขียนและความคิดเห็นต้นทางผ่าน PUT | เปลี่ยนเฉพาะข้อความ; parent/replyTo ผู้เขียน และเวลาสร้างคงเดิม | ✅ ผ่าน |
| TC-JUNIT-01-08 | `swaggerPublishesCommentReadAndUpdateContracts` | เอกสาร API ของความคิดเห็น | /v3/api-docs ได้ 200 และมี GET, PUT, DELETE ของ /api/v1/comments/{id} | ✅ ผ่าน |

### 2. FoodJourneyTest — โพสต์อาหาร การจอง สต็อก และรับอาหาร

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/FoodJourneyTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.FoodJourneyTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.FoodJourneyTest.txt)

ผลรวมคลาส: **27 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-02-01 | `stockChangesAreOwnerOnlyVersionedAndOfflineCountCanBeCorrected` | สิทธิ์ปรับสต็อก version และการแก้ยอดแจกออฟไลน์ | คนอื่นได้ 403; snapshot เก่าได้ 409; ย้อนยอดออฟไลน์ได้; ยอด/สถานะไม่ถูกต้องถูกปฏิเสธ | ✅ ผ่าน |
| TC-JUNIT-02-02 | `onlyOwnerCanExtendAndLiveOrEmptyPostCannotBeExtended` | สิทธิ์ต่อเวลาและเงื่อนไขของโพสต์ | คนอื่นได้ 403; โพสต์ที่ยังไม่หมดเวลาหรือไม่มีอาหารเหลือได้ 409 | ✅ ผ่าน |
| TC-JUNIT-02-03 | `quantityCannotDropBelowExistingReservations` | ลดจำนวนอาหารต่ำกว่ายอดจอง | ได้ 409 และจำนวนที่ยังรับได้ไม่เปลี่ยน | ✅ ผ่าน |
| TC-JUNIT-02-04 | `createPersistsCoordinatesAndAvailableQuantity` | สร้างโพสต์พร้อมพิกัดและจำนวนอาหาร | ได้ 201; availableQuantity เป็น 5; latitude เป็น 16.474 | ✅ ผ่าน |
| TC-JUNIT-02-05 | `activeBookingIsFoundAndEditedDirectlyEvenWhenFull` | ค้นหาและแก้ไขการจองเดิมเมื่ออาหารจองเต็มแล้ว | คืน id และ pickupCode เดิม; ลดจองจาก 5 เป็น 3 แล้วอาหารว่างเป็น 2; ยกเลิกแล้วไม่มีการจองค้าง | ✅ ผ่าน |
| TC-JUNIT-02-06 | `retryUsesSameReservation` | ส่งการจองซ้ำด้วย Idempotency-Key เดิม | ได้ reservation id เดิมและหักสต็อกเพียงครั้งเดียว | ✅ ผ่าน |
| TC-JUNIT-02-07 | `invalidQuantityIsRejected` | สร้างโพสต์ด้วยจำนวนอาหารเป็น 0 | ได้ 400 | ✅ ผ่าน |
| TC-JUNIT-02-08 | `signedInOwnerCanDecodeQrCameraFrame` | อ่านภาพ QR ผ่าน API เมื่อเข้าสู่ระบบ | ได้ 200 และค่า FS1:42:001234 | ✅ ผ่าน |
| TC-JUNIT-02-09 | `wrongPickupCodeLocksAfterFiveAttempts` | กรอกรหัสรับอาหารผิด 5 ครั้ง | ครั้งที่ผิดได้ 400; ส่งรหัสถูกหลังถูกล็อกได้ 429; ยอดจองและยอดรับไม่เปลี่ยน | ✅ ผ่าน |
| TC-JUNIT-02-10 | `concurrentOfflineAndReservationCannotUseTheSameLastFood` | แจกออฟไลน์และจองอาหารชุดสุดท้ายพร้อมกัน | สำเร็จเพียงงานเดียว อีกงานได้ 409; อาหารว่างเหลือ 0 | ✅ ผ่าน |
| TC-JUNIT-02-11 | `administratorCanModerateButSuspendedSessionCannotContinue` | แอดมินระงับบัญชีและตรวจ session ของบัญชีที่ถูกระงับ | แอดมินเข้าจัดการได้; ระงับได้ 204; บัญชีที่ถูกระงับเรียก API ต่อได้ 403 | ✅ ผ่าน |
| TC-JUNIT-02-12 | `cancellingTwiceRestoresStockOnlyOnce` | ยกเลิกการจองซ้ำสองครั้ง | คืนสต็อกครั้งเดียว จากอาหารว่าง 3 กลับเป็น 5 | ✅ ผ่าน |
| TC-JUNIT-02-13 | `imageUploadValidatesPixelsAndServesPersistedImage` | อัปโหลดไฟล์ปลอมและภาพจริง | ไฟล์ที่ไม่ใช่ภาพได้ 400; ภาพจริงบันทึกได้และเปิด URL ได้ 200 ชนิด image/jpeg | ✅ ผ่าน |
| TC-JUNIT-02-14 | `closePostCancelsPendingReservation` | ปิดโพสต์ที่มีการจองรอรับ | ปิดได้ 204; การจองเป็น CANCELLED; อ่านโพสต์ได้ 404 | ✅ ผ่าน |
| TC-JUNIT-02-15 | `foreignReservationCannotBeRead` | สมาชิกที่ไม่เกี่ยวข้องอ่านการจอง | ได้ 403 | ✅ ผ่าน |
| TC-JUNIT-02-16 | `ownReservationAndForeignMutationAreForbidden` | เจ้าของจองอาหารตนเองและสมาชิกแก้โพสต์ผู้อื่น | ทั้งสองกรณีได้ 403 | ✅ ผ่าน |
| TC-JUNIT-02-17 | `memberCannotModerate` | สมาชิกทั่วไปเข้ารายงานของแอดมิน | ได้ 403 | ✅ ผ่าน |
| TC-JUNIT-02-18 | `ownerCanSeeReservationMemberPhotoButStrangerCannot` | สิทธิ์ดูรูปโปรไฟล์ผู้จอง | เจ้าของโพสต์ดูภาพได้ 200; สมาชิกที่ไม่เกี่ยวข้องได้ 403 | ✅ ผ่าน |
| TC-JUNIT-02-19 | `offlineDistributionProtectsBookingsAndStillAllowsQrCollection` | แจกออฟไลน์โดยรักษาอาหารที่จองไว้ และยืนยันรับด้วยรหัส | แจกเกินส่วนว่างได้ 409; แจกส่วนว่างได้; ผู้จองยังรับได้; เพิ่มอาหารแล้วกลับเป็น AVAILABLE | ✅ ผ่าน |
| TC-JUNIT-02-20 | `lastItemCanOnlyBeReservedOnce` | จองอาหารชุดสุดท้ายพร้อมกันสองคำขอ | ได้สถานะ 201 และ 409 อย่างละหนึ่งคำขอ | ✅ ผ่าน |
| TC-JUNIT-02-21 | `ownerCanSetPerPersonLimitAndReservationsCannotExceedIt` | กำหนดจำนวนสูงสุดต่อคนและจองเกินกำหนด | บันทึก maxPerPerson เป็น 2; จอง 3 ได้ 409 | ✅ ผ่าน |
| TC-JUNIT-02-22 | `ownerCanExtendAnExpiredPostWithFoodRemaining` | ต่อเวลาโพสต์หมดอายุที่ยังมีอาหารเหลือ | ได้ 200; สถานะกลับเป็น AVAILABLE และเวลาสิ้นสุดตรงกับค่าที่ส่ง | ✅ ผ่าน |
| TC-JUNIT-02-23 | `invalidOrUnfairPerPersonLimitIsRejected` | ตั้งเพดานเกินจำนวนอาหารหรือลดต่ำกว่ายอดที่จองแล้ว | ค่าที่ไม่ถูกต้องได้ 400; ลดต่ำกว่ายอดจองเดิมได้ 409 | ✅ ผ่าน |
| TC-JUNIT-02-24 | `ownerBookingNotificationNamesTheBookerAndTheFoodPost` | ข้อความแจ้งเตือนเจ้าของเมื่อมีการจอง | มีชื่อผู้จอง ชื่ออาหาร และประเภท RESERVATION | ✅ ผ่าน |
| TC-JUNIT-02-25 | `normalEditCannotForgetOfflineStockAndExhaustedPostLeavesDiscovery` | แก้จำนวนอาหารหลังแจกออฟไลน์จนหมด และค้นหารายการอาหาร | ลดจำนวนต่ำกว่ายอดแจกได้ 409; โพสต์หมดอาหารไม่อยู่ในผลค้นหา; เจ้าของยังอ่านรายการตนเองได้ | ✅ ผ่าน |
| TC-JUNIT-02-26 | `collectIsIdempotentAndPrivate` | ยืนยันรับอาหารซ้ำและความเป็นส่วนตัวของ pickupCode | เจ้าของอ่านใบจองไม่เห็น pickupCode; ยืนยันด้วยรหัสถูกซ้ำได้ COLLECTED โดยยอดรับคงเป็น 2 และอาหารว่างเป็น 3 | ✅ ผ่าน |
| TC-JUNIT-02-27 | `unauthenticatedMutationIsRejected` | สร้างโพสต์โดยไม่ล็อกอิน | ได้ 401 | ✅ ผ่าน |

### 3. FoodshareApplicationTests — เริ่มต้นแอปพลิเคชัน

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/FoodshareApplicationTests.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.FoodshareApplicationTests.xml) · [สรุป TXT](surefire/com.kku.foodshare.FoodshareApplicationTests.txt)

ผลรวมคลาส: **1 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-03-01 | `contextLoads` | เริ่ม Spring Application Context | โหลด context ได้โดยไม่เกิดข้อผิดพลาด | ✅ ผ่าน |

### 4. MailDeploymentConfigTest — ค่าตั้งค่าอีเมลและ Docker Compose

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/MailDeploymentConfigTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.MailDeploymentConfigTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.MailDeploymentConfigTest.txt)

ผลรวมคลาส: **2 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-04-01 | `bothComposeEntrypointsPassTheEmailSettingsToTheApplication` | การส่งค่าตั้งค่าอีเมลใน Compose ทั้งสองไฟล์ | ค่าของทั้งสองไฟล์ตรงกันและมี environment ที่จำเป็น; MAIL_ENABLED เริ่มต้นเป็น false | ✅ ผ่าน |
| TC-JUNIT-04-02 | `localMailTestingUsesACapturedInboxAndThePublishedApplicationPort` | การตั้งค่าอีเมลสำหรับทดสอบในเครื่อง | ใช้ SMTP ของ Mailpit พอร์ต 1025; หน้า inbox พอร์ต 8025; APP_BASE_URL อ้างพอร์ตที่เผยแพร่ของแอป | ✅ ผ่าน |

### 5. OfflineMigrationTest — Migration ของยอดแจกออฟไลน์

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/OfflineMigrationTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.OfflineMigrationTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.OfflineMigrationTest.txt)

ผลรวมคลาส: **1 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-05-01 | `migrationPreservesLegacyRowsAndAddsTheCombinedStockConstraint` | Migration เพิ่มยอดแจกออฟไลน์และข้อจำกัดสต็อก | ข้อมูลเดิมคงอยู่; offline_quantity เริ่มเป็น 0; ยอดติดลบหรือยอดรวมเกิน quantity ถูกฐานข้อมูลปฏิเสธ | ✅ ผ่าน |

### 6. OnboardingJourneyTest — คำแนะนำเริ่มต้นใช้งานแยกตามบัญชี

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/OnboardingJourneyTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.OnboardingJourneyTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.OnboardingJourneyTest.txt)

ผลรวมคลาส: **5 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-06-01 | `openingOrRefreshingDoesNotMarkAnAccountAsFinished` | เปิด/รีเฟรชคำแนะนำโดยยังไม่กดยืนยันจบ | onboarding-needed ยังเป็น true | ✅ ผ่าน |
| TC-JUNIT-06-02 | `guestsAndRequestsWithoutCsrfCannotChangeCompletion` | บันทึกสถานะคำแนะนำโดยไม่ล็อกอินหรือไม่มี CSRF | ไม่ล็อกอินได้ 401; ไม่มี CSRF ได้ 403; สถานะบัญชีไม่เปลี่ยน | ✅ ผ่าน |
| TC-JUNIT-06-03 | `registrationAndFirstLoginOpenTheGuideOnTheUnchangedLoginDestination` | สมัครสมาชิกและล็อกอินครั้งแรก | สมัครแล้วไป /login; ล็อกอินแล้วไป /home; มี home-guide และ onboarding-needed เป็น true | ✅ ผ่าน |
| TC-JUNIT-06-04 | `completionBelongsToOneAccountAndSurvivesAnotherLoginOrBrowser` | สถานะดูคำแนะนำแยกตามบัญชีและการยืนยันซ้ำ | เปลี่ยนเฉพาะบัญชีที่ล็อกอิน แม้ส่ง userId คนอื่น; คำขอซ้ำไม่เพิ่ม version; อ่านด้วย session ใหม่ยังได้สถานะเดิม | ✅ ผ่าน |
| TC-JUNIT-06-05 | `migrationKeepsExistingAccountsAndDefaultsFutureAccountsToTheGuide` | Migration สถานะ onboarding ของบัญชีเดิมและบัญชีใหม่ | บัญชีเดิมคงข้อมูลและ completed เป็น true; บัญชีใหม่ completed เป็น false | ✅ ผ่าน |

### 7. PasswordResetBrevoJourneyTest — รีเซ็ตรหัสผ่านผ่าน Brevo

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/PasswordResetBrevoJourneyTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.PasswordResetBrevoJourneyTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.PasswordResetBrevoJourneyTest.txt)

ผลรวมคลาส: **2 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-07-01 | `brevoHttpTransportProducesAUsablePublicLinkAndSingleUsePasswordReset` | ส่งลิงก์รีเซ็ตรหัสผ่านผ่าน Brevo adapter บน HTTP จำลอง | ผู้รับ/ผู้ส่งถูกต้อง; ลิงก์สาธารณะใช้ได้ครั้งเดียว; รหัสเก่าล็อกอินไม่ได้ รหัสใหม่ล็อกอินได้ | ✅ ผ่าน |
| TC-JUNIT-07-02 | `unknownAccountsAndProviderFailureDoNotExposeAccountExistenceOrLeaveAUsableToken` | รีเซ็ตบัญชีไม่พบและบริการส่งอีเมลล้มเหลว | ตอบยืนยันเหมือนกัน; บัญชีไม่พบไม่ส่งอีเมล; ส่งล้มเหลวไม่เหลือ token ใช้งานได้และรหัสเดิมคงอยู่ | ✅ ผ่าน |

### 8. PasswordResetJourneyTest — กระบวนการรีเซ็ตรหัสผ่าน

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/PasswordResetJourneyTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.PasswordResetJourneyTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.PasswordResetJourneyTest.txt)

ผลรวมคลาส: **3 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-08-01 | `unknownEmailReturnsSameConfirmationWithoutSendingMail` | ขอรีเซ็ตด้วยอีเมลที่ไม่อยู่ในระบบ | ไป /forgot-password?sent เหมือนกรณีปกติ และไม่ส่งอีเมล | ✅ ผ่าน |
| TC-JUNIT-08-02 | `requestEmailResetAndLoginWithNewPassword` | ขอลิงก์ รีเซ็ตรหัสผ่าน และลองล็อกอิน | รหัสยืนยันไม่ตรงไม่ใช้ token; รีเซ็ตสำเร็จ token ใช้ซ้ำไม่ได้; รหัสเก่าใช้ไม่ได้ รหัสใหม่เข้า /home ได้ | ✅ ผ่าน |
| TC-JUNIT-08-03 | `invalidTokenCannotChangePasswordAndCsrfIsRequired` | ใช้ token ไม่ถูกต้องและส่งคำขอโดยไม่มี CSRF | แสดงลิงก์หมดอายุ/ถูกใช้แล้ว; ไม่มี CSRF ได้ 403 | ✅ ผ่าน |

### 9. PasswordResetSmtpDeliveryTest — ส่งอีเมลผ่าน SMTP ทดสอบ

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/PasswordResetSmtpDeliveryTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.PasswordResetSmtpDeliveryTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.PasswordResetSmtpDeliveryTest.txt)

ผลรวมคลาส: **1 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-09-01 | `realSmtpEmailContainsAUsableLinkAndTheNewPasswordCanLogIn` | ส่งเมลจริงผ่าน SMTP ทดสอบและรีเซ็ตรหัสผ่าน | อีเมลถึงผู้รับ มีลิงก์พอร์ตแอปที่ถูกต้อง; token ใช้ครั้งเดียว; รหัสใหม่ล็อกอินได้ | ✅ ผ่าน |

### 10. ReviewRegressionTest — ป้องกันข้อผิดพลาดเดิมกลับมา

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/ReviewRegressionTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.ReviewRegressionTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.ReviewRegressionTest.txt)

ผลรวมคลาส: **2 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-10-01 | `disabledMailReturnsSameFailureForUnknownAndKnownEmail` | ขอรีเซ็ตเมื่อปิดระบบอีเมล | อีเมลที่มี/ไม่มีบัญชีได้รับ exception ชนิดและข้อความเดียวกัน | ✅ ผ่าน |
| TC-JUNIT-10-02 | `staleProfileCannotRestoreSuspendedAccount` | บันทึกโปรไฟล์เก่าหลังบัญชีถูกระงับ | เกิด OptimisticLockingFailureException และบัญชียังคงถูกระงับ | ✅ ผ่าน |

### 11. SubmissionContractTest — Layer และสัญญาการทำงานของระบบ

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/SubmissionContractTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.SubmissionContractTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.SubmissionContractTest.txt)

ผลรวมคลาส: **4 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-11-01 | `publicMemberProfileContractIsPreserved` | สัญญา API โปรไฟล์สาธารณะ | คืนเฉพาะ id และ name; บัญชีไม่มีอยู่หรือถูกปิดได้ 404 | ✅ ผ่าน |
| TC-JUNIT-11-02 | `servicesDependOnAbstractions` | การพึ่งพา interface ของ Service/Mapper/Storage ในคลาสที่ตรวจ | ไม่พึ่ง collaborator แบบ concrete ในขอบเขตที่กำหนด; PostViewMapper ไม่เข้าถึง Repository | ✅ ผ่าน |
| TC-JUNIT-11-03 | `imageProviderAndLegacyFilesStillWork` | เลือกผู้ให้บริการภาพและอ่าน/ลบภาพ local เดิม | เลือก local/cloudinary ตาม config และทั้งสองแบบยังอ่าน/ลบไฟล์ local เดิมได้ | ✅ ผ่าน |
| TC-JUNIT-11-04 | `controllersRespectServiceLayer` | โครงสร้าง dependency ของ Controller ที่กำหนด | ไม่รับ Repository โดยตรง และ dependency ฝั่ง Service เป็น interface | ✅ ผ่าน |

### 12. WebPagesTest — เรนเดอร์หน้าเว็บและสิทธิ์เข้าถึง

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/WebPagesTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.WebPagesTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.WebPagesTest.txt)

ผลรวมคลาส: **11 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-12-01 | `loggingOutReturnsToPublicHome` | ออกจากระบบผ่าน POST พร้อม CSRF | redirect ไป / | ✅ ผ่าน |
| TC-JUNIT-12-02 | `ownerWorkspaceIncludesQrScannerAndManagementShortcuts` | HTML หน้าโพสต์ของเจ้าของ | มี QR scanner ส่วน preview และทางลัดจัดการโพสต์ | ✅ ผ่าน |
| TC-JUNIT-12-03 | `guestCannotOpenPostEditor` | ผู้ไม่ล็อกอินเปิดหน้าสร้างโพสต์ | redirect ไป /login | ✅ ผ่าน |
| TC-JUNIT-12-04 | `guestRootShowsTheFoodShareLandingPage` | HTML หน้าแรกสำหรับผู้ไม่ล็อกอิน | มีข้อความ/ภาพ Landing Page กล่องความคิดเห็น ตัวดูภาพ และปุ่มสมัครสมาชิก | ✅ ผ่าน |
| TC-JUNIT-12-05 | `csrfIsRequiredForMutations` | ส่งคำขอเปลี่ยนข้อมูลโดยไม่มี CSRF | ได้ 403 | ✅ ผ่าน |
| TC-JUNIT-12-06 | `signedInRootKeepsLandingAndDashboardOpensFoodDiscovery` | หน้าแรก /home และ /explore หลังล็อกอิน | / ยังคง Landing Page; /home และ /explore มีแผนที่ค้นหาอาหาร | ✅ ผ่าน |
| TC-JUNIT-12-07 | `signedInUserCanRenderAccountPage` | เปิดหน้าบัญชีของสมาชิกที่ล็อกอิน | ได้ 200 และมีข้อความบัญชีของฉัน | ✅ ผ่าน |
| TC-JUNIT-12-08 | `signedInHeaderOffersCompactProfileMenuAndProtectedLogout` | ส่วนหัวเว็บหลังล็อกอิน | มีเมนูโปรไฟล์ ทางลัดจอง/โพสต์/แจ้งเตือน และฟอร์ม POST /logout | ✅ ผ่าน |
| TC-JUNIT-12-09 | `publicPagesRenderIncludingSharedFragments` | เรนเดอร์หน้าสาธารณะและ fragments ร่วม | ได้ 200 และ HTML ระบุ lang="th" | ✅ ผ่าน |
| TC-JUNIT-12-10 | `guestOpeningDashboardUrlReturnsToPublicHomeInsteadOfLogin` | ผู้ไม่ล็อกอินเปิด /home | redirect ไป / | ✅ ผ่าน |
| TC-JUNIT-12-11 | `dashboardAndExploreRenderTheSameSharingMapWithSidebarAndPreview` | โครงแผนที่ใน /home และ /explore | มี map shell, sidebar, preview และปุ่มระบุตำแหน่งเอง | ✅ ผ่าน |

### 13. FoodPostApiControllerTest — API แผนที่อาหาร

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/controller/api/FoodPostApiControllerTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.controller.api.FoodPostApiControllerTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.controller.api.FoodPostApiControllerTest.txt)

ผลรวมคลาส: **2 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-13-01 | `shouldReturnActiveFoodPostsFromService` | API Controller ส่งต่อรายการจาก Service | คืนหนึ่งโพสต์ชื่อข้าวกล่องและเรียก getActiveMapPosts() | ✅ ผ่าน |
| TC-JUNIT-13-02 | `shouldExposeMapRestEndpoint` | annotation ของ API แผนที่ | มี @RestController, @RequestMapping /api/food-posts และ @GetMapping /map | ✅ ผ่าน |

### 14. DashboardControllerTest — Controller หน้า Dashboard

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/controller/web/DashboardControllerTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.controller.web.DashboardControllerTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.controller.web.DashboardControllerTest.txt)

ผลรวมคลาส: **3 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-14-01 | `dashboardShouldAddGoogleAccountProfileToModel` | Dashboard ของบัญชี Google | เรียก Service ด้วยอีเมลและป้ายบัญชี Google; ใส่ currentUser และคืน dashboard | ✅ ผ่าน |
| TC-JUNIT-14-02 | `dashboardShouldAddEmailAccountProfileToModel` | Dashboard ของบัญชีอีเมล | เรียก Service ด้วยอีเมลและป้ายบัญชีอีเมล; ใส่ currentUser และคืน dashboard | ✅ ผ่าน |
| TC-JUNIT-14-03 | `anonymousDashboardRequestReturnsToPublicHome` | เรียก Dashboard โดยไม่มี Authentication | คืน redirect:/ | ✅ ผ่าน |

### 15. HomeControllerTest — Controller หน้าแรกและเกี่ยวกับ

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/controller/web/HomeControllerTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.controller.web.HomeControllerTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.controller.web.HomeControllerTest.txt)

ผลรวมคลาส: **2 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-15-01 | `aboutShouldReturnAboutTemplate` | Controller หน้าเกี่ยวกับ | คืนชื่อ template about | ✅ ผ่าน |
| TC-JUNIT-15-02 | `rootShouldReturnLandingPage` | Controller หน้าแรก | คืนชื่อ template home | ✅ ผ่าน |

### 16. LoginControllerTest — Controller หน้าเข้าสู่ระบบ

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/controller/web/LoginControllerTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.controller.web.LoginControllerTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.controller.web.LoginControllerTest.txt)

ผลรวมคลาส: **1 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-16-01 | `loginShouldReturnLoginTemplate` | Controller หน้าเข้าสู่ระบบ | คืนชื่อ template login | ✅ ผ่าน |

### 17. FoodPostTest — ข้อมูลใน FoodPost Entity

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/domain/entity/FoodPostTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.domain.entity.FoodPostTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.domain.entity.FoodPostTest.txt)

ผลรวมคลาส: **1 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-17-01 | `shouldStoreFoodPostInformation` | เก็บข้อมูลใน FoodPost Entity ผ่าน setters/getters | อ่าน id เจ้าของ ชื่อ รายละเอียด จำนวน พิกัด เวลา และสถานะได้ตรงค่าที่ตั้ง | ✅ ผ่าน |

### 18. UserRoleTest — บทบาทเริ่มต้นของสมาชิก

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/domain/entity/UserRoleTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.domain.entity.UserRoleTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.domain.entity.UserRoleTest.txt)

ผลรวมคลาส: **1 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-18-01 | `newUserShouldHaveUserRoleByDefault` | บทบาทเริ่มต้นของ User ใหม่ | เป็น USER | ✅ ผ่าน |

### 19. FoodPostMapperTest — Mapper ข้อมูลโพสต์สำหรับแผนที่

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/mapper/FoodPostMapperTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.mapper.FoodPostMapperTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.mapper.FoodPostMapperTest.txt)

ผลรวมคลาส: **1 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-19-01 | `shouldMapFoodPostForMapResponse` | แปลง FoodPost เป็น DTO สำหรับแผนที่ | ข้อมูลชื่อ จำนวน พิกัด เวลา และสถานะตรง Entity; หมวดอาหารมีไอคอน 🍱 | ✅ ผ่าน |

### 20. UserProfileMapperTest — Mapper โปรไฟล์สมาชิก

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/mapper/UserProfileMapperTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.mapper.UserProfileMapperTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.mapper.UserProfileMapperTest.txt)

ผลรวมคลาส: **2 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-20-01 | `shouldUseRegisteredDisplayNameForEmailAccount` | Mapper โปรไฟล์เมื่อมีชื่อที่สมัครไว้ | DTO ใช้ชื่อที่สมัคร อีเมล และป้ายบัญชีอีเมล | ✅ ผ่าน |
| TC-JUNIT-20-02 | `shouldUseEmailPrefixWhenDisplayNameIsBlank` | Mapper โปรไฟล์เมื่อชื่อว่าง | ใช้ส่วนก่อน @ ของอีเมลเป็นชื่อแสดง | ✅ ผ่าน |

### 21. FoodPostRepositoryTest — Repository ค้นหาอาหารสำหรับแผนที่

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/repository/FoodPostRepositoryTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.repository.FoodPostRepositoryTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.repository.FoodPostRepositoryTest.txt)

ผลรวมคลาส: **1 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-21-01 | `shouldFindOnlyActiveMapPosts` | Repository ค้นหาโพสต์สำหรับแผนที่ | คืนเฉพาะโพสต์ที่ยังรับได้ตาม fixture หนึ่งรายการ | ✅ ผ่าน |

### 22. GoogleOAuth2SuccessHandlerTest — Handler หลัง Google Login

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/security/GoogleOAuth2SuccessHandlerTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.security.GoogleOAuth2SuccessHandlerTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.security.GoogleOAuth2SuccessHandlerTest.txt)

ผลรวมคลาส: **1 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-22-01 | `shouldCreateOrFindUserAndRedirectHomeAfterGoogleLogin` | Success Handler หลัง Google Login | เรียก findOrCreateGoogleUser ด้วยอีเมล/ชื่อ และ redirect ไป /home | ✅ ผ่าน |

### 23. AdminPostListingTest — รายการโพสต์สำหรับแอดมิน

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/service/AdminPostListingTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.service.AdminPostListingTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.service.AdminPostListingTest.txt)

ผลรวมคลาส: **1 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-23-01 | `adminCanFindExpiredAndCancelledPostsWithoutChangingStock` | แอดมินค้นหาโพสต์หมดอายุ/ยกเลิก และตรวจสิทธิ์ | แอดมินอ่านและกรองได้โดยสต็อกไม่เปลี่ยน; สมาชิกทั่วไปได้ 403; filter ไม่ถูกต้องได้ 400 | ✅ ผ่าน |

### 24. BrevoEmailServiceTest — บริการส่งอีเมลผ่าน Brevo adapter

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/service/BrevoEmailServiceTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.service.BrevoEmailServiceTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.service.BrevoEmailServiceTest.txt)

ผลรวมคลาส: **1 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-24-01 | `sendsResetLinkThroughHttpAndRejectsProviderFailure` | Brevo adapter ส่งข้อความบน HTTP ทดสอบ | ส่ง API key ผู้รับ และลิงก์ตามกำหนด; provider ล้มเหลวเกิด MailSendException | ✅ ผ่าน |

### 25. CommentRepliesPersistenceTest — ความสัมพันธ์คำตอบและ persistence

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/service/CommentRepliesPersistenceTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.service.CommentRepliesPersistenceTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.service.CommentRepliesPersistenceTest.txt)

ผลรวมคลาส: **1 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-25-01 | `selfReferencesSurviveReloadAndSoftDeletionKeepsRepliesReadable` | โหลดสายคำตอบใหม่และลบความคิดเห็นหลักแบบ soft delete | parent/replyTo ถูกต้อง; คำตอบสองรายการยังอยู่; parentDeleted เป็น true และซ่อนชื่อที่เกี่ยวกับข้อความถูกลบ | ✅ ผ่าน |

### 26. CommentRepliesTest — ตอบกลับ ลบ และ event แจ้งเตือนความคิดเห็น

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/service/CommentRepliesTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.service.CommentRepliesTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.service.CommentRepliesTest.txt)

ผลรวมคลาส: **7 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-26-01 | `rootDeletionIsSoftAndRepliesRetainContextWithoutDeletedAuthorsName` | ลบความคิดเห็นหลักโดยรักษาคำตอบ | root มี deletedAt; คำตอบไม่ถูกลบ; ซ่อนชื่อผู้เขียนต้นทาง; ไม่เรียก repository.delete() | ✅ ผ่าน |
| TC-JUNIT-26-02 | `deletionPermissionsRemainAuthorOwnerOrAdmin` | สิทธิ์ลบความคิดเห็น | สมาชิกที่ไม่เกี่ยวข้องได้ 403; แอดมินลบได้และบันทึก deletedBy | ✅ ผ่าน |
| TC-JUNIT-26-03 | `replyToReplyKeepsRootAndTracksActualTarget` | ตอบกลับคำตอบอีกชั้น | parentCommentId อ้าง root เดิม และ replyToCommentId อ้างคำตอบที่ตอบจริง | ✅ ผ่าน |
| TC-JUNIT-26-04 | `existingRootCommentPayloadStillWorksAndNotifiesOwnerWithActor` | เพิ่มความคิดเห็นหลักด้วย payload เดิมและส่ง event | parent/replyTo เป็น null; ปรับช่องว่างข้อความ; ส่ง ActivityNotice ให้เจ้าของพร้อม actor และลิงก์โพสต์ | ✅ ผ่าน |
| TC-JUNIT-26-05 | `replyNotifiesOwnerAndTargetWithoutDuplicateRecipients` | ตอบความคิดเห็นของผู้อื่นและตรวจผู้รับ event | แจ้งเจ้าของโพสต์และผู้เขียนเป้าหมายอย่างละหนึ่งครั้ง | ✅ ผ่าน |
| TC-JUNIT-26-06 | `replyToOwnerOnlyCreatesOneNoticeAndSelfCommentsCreateNone` | ตอบเจ้าของโพสต์และแสดงความคิดเห็นกับโพสต์ตนเอง | ตอบเจ้าของส่งหนึ่ง event; self-comment ไม่ส่งแจ้งเตือนให้ตนเอง | ✅ ผ่าน |
| TC-JUNIT-26-07 | `deletedOrForeignTargetsAndOverlongBodiesAreRejectedBeforeSave` | เป้าหมายคำตอบถูกลบ/ต่างโพสต์/ไม่มีอยู่ และข้อความไม่ถูกต้อง | ปฏิเสธด้วย 400/404 ตามกรณี; ไม่บันทึกและไม่ส่ง event | ✅ ผ่าน |

### 27. CustomUserDetailsServiceTest — โหลดสมาชิกและ authority

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/service/CustomUserDetailsServiceTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.service.CustomUserDetailsServiceTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.service.CustomUserDetailsServiceTest.txt)

ผลรวมคลาส: **2 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-27-01 | `shouldLoadAdminAuthorityFromUserRole` | แปลงบทบาทแอดมินเป็น authority | มี ROLE_ADMIN | ✅ ผ่าน |
| TC-JUNIT-27-02 | `shouldLoadUserByEmail` | โหลด UserDetails จากอีเมล | username และ password hash ตรงข้อมูลสมาชิก | ✅ ผ่าน |

### 28. FoodPostServiceTest — Service อ่านรายการแผนที่

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/service/FoodPostServiceTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.service.FoodPostServiceTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.service.FoodPostServiceTest.txt)

ผลรวมคลาส: **1 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-28-01 | `shouldReturnActiveMapPostsInRepositoryOrder` | Service แปลงโพสต์แผนที่ตามลำดับ Repository | รายการข้าวกล่องก่อนน้ำผลไม้ตามลำดับเดิม; รายการผลลัพธ์เพิ่มสมาชิกไม่ได้ | ✅ ผ่าน |

### 29. GoogleUserServiceTest — ค้นหา/สร้างสมาชิกจาก Google Login

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/service/GoogleUserServiceTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.service.GoogleUserServiceTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.service.GoogleUserServiceTest.txt)

ผลรวมคลาส: **2 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-29-01 | `shouldCreateNewUserWhenGoogleEmailDoesNotExist` | Google Login ด้วยอีเมลใหม่ | สร้าง User ที่มีอีเมล/ชื่อถูกต้อง และเรียก save หนึ่งครั้ง | ✅ ผ่าน |
| TC-JUNIT-29-02 | `shouldReturnExistingUserWhenGoogleEmailAlreadyExists` | Google Login ด้วยอีเมลที่มีแล้ว | คืน User เดิมและไม่เรียก save เพื่อสร้างซ้ำ | ✅ ผ่าน |

### 30. PasswordResetProtectionTest — ความปลอดภัยของการรีเซ็ตรหัสผ่าน

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/service/PasswordResetProtectionTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.service.PasswordResetProtectionTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.service.PasswordResetProtectionTest.txt)

ผลรวมคลาส: **5 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-30-01 | `publicRequestsAreLimitedPerAddress` | จำกัดความถี่รีเซ็ตตาม IP | IP ที่เกินกำหนดได้ 429; IP อื่นยังขอได้ | ✅ ผ่าน |
| TC-JUNIT-30-02 | `smtpFailureDoesNotRevealKnownEmail` | บริการ SMTP ล้มเหลวเมื่อขอรีเซ็ต | คำขออีเมลที่มี/ไม่มีบัญชีไม่เผยความต่างผ่าน exception; ลบ token ที่ส่งไม่สำเร็จ | ✅ ผ่าน |
| TC-JUNIT-30-03 | `deliveryFailureAllowsTheNextRequestToRetryWithoutLeakingToken` | ส่งเมลล้มเหลวแล้วขอใหม่ | ลองส่งใหม่ได้ทั้งสองครั้ง; token ที่ส่งไม่สำเร็จถูกลบทั้งสองครั้ง | ✅ ผ่าน |
| TC-JUNIT-30-04 | `cooldownDoesNotInvalidatePreviousTokenOrSendAnotherEmail` | ขอรีเซ็ตซ้ำในช่วง cooldown | ไม่ลบ token เดิมและไม่ส่งเมลเพิ่ม | ✅ ผ่าน |
| TC-JUNIT-30-05 | `expiredUsedOrSuspendedAccountTokensCannotResetPassword` | token หมดอายุ ใช้แล้ว หรือบัญชีถูกระงับ | isTokenValid เป็น false ตามกรณี; รีเซ็ตไม่ได้ และไม่บันทึก token ใหม่ | ✅ ผ่าน |

### 31. PickupReminderIntegrationTest — แจ้งเตือนก่อนหมดเวลารับอาหาร

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/service/PickupReminderIntegrationTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.service.PickupReminderIntegrationTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.service.PickupReminderIntegrationTest.txt)

ผลรวมคลาส: **7 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-31-01 | `cancelledCollectedExpiredAndClosedReservationsAreNotReminded` | รายการยกเลิก รับแล้ว หมดอายุ หรือโพสต์ปิด | ไม่สร้างการเตือนรับอาหาร | ✅ ผ่าน |
| TC-JUNIT-31-02 | `disabledReceiverOrOwnerDoesNotReceiveAPickupReminder` | ผู้รับหรือเจ้าของโพสต์ถูกระงับ | ไม่สร้างการเตือนของการจองนั้น | ✅ ผ่าน |
| TC-JUNIT-31-03 | `inboxAndBadgeCatchUpWhenFreeHostingWasAsleepAndStayScopedToTheSignedInUser` | เปิด inbox/ยอด unread หลังไม่มี scheduler และตรวจแยกบัญชี | สร้างการเตือนค้างให้บัญชีที่ล็อกอิน ไม่ซ้ำ และลิงก์ตรงใบจอง; ผู้ไม่ล็อกอินได้ 401 | ✅ ผ่าน |
| TC-JUNIT-31-04 | `longPickupNamesStayWithinTheExistingNotificationColumnAndNamesRemainEscapable` | ชื่ออาหาร/จุดรับยาวและข้อความที่ต้อง escape | ข้อความไม่เกิน 500 ตัวอักษรและยังมีเวลารับ; คงข้อความต้นฉบับเพื่อให้ UI escape (ไม่ใช่การทดสอบ browser XSS) | ✅ ผ่าน |
| TC-JUNIT-31-05 | `concurrentSchedulerAndInboxChecksCreateOnlyOneReminder` | งานเบื้องหลังและ inbox ตรวจพร้อมกัน | สร้างการเตือนรวมหนึ่งรายการ และการจองยังเป็น RESERVED | ✅ ผ่าน |
| TC-JUNIT-31-06 | `thirtyMinuteBoundaryCreatesOneReadableReminderAndLeavesStockAndQrUntouched` | ขอบเขต 30 นาทีก่อนหมดเวลารับ | สร้างการเตือนครั้งเดียวเมื่อเข้าเกณฑ์ พร้อมข้อมูลรับอาหาร; สต็อกและข้อมูล QR ไม่เปลี่ยน | ✅ ผ่าน |
| TC-JUNIT-31-07 | `reservationsWithZeroPublicStockStillReceiveAReminder` | อาหารว่างเป็น 0 แต่ยังมีผู้จองรอรับ | ผู้จองยังได้รับการเตือน | ✅ ผ่าน |

### 32. QrScanServiceTest — อ่าน QR รับอาหาร

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/service/QrScanServiceTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.service.QrScanServiceTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.service.QrScanServiceTest.txt)

ผลรวมคลาส: **1 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-32-01 | `decodesFoodSharePassFromCameraImage` | ถอด QR จาก bytes ของภาพ | ได้ FS1:42:001234 | ✅ ผ่าน |

### 33. UserProfileServiceTest — Service อ่านโปรไฟล์

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/service/UserProfileServiceTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.service.UserProfileServiceTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.service.UserProfileServiceTest.txt)

ผลรวมคลาส: **2 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-33-01 | `shouldThrowExceptionWhenUserDoesNotExist` | Service อ่านโปรไฟล์อีเมลที่ไม่พบ | เกิด IllegalArgumentException ข้อความ User not found | ✅ ผ่าน |
| TC-JUNIT-33-02 | `shouldReturnProfileOfUserByEmail` | Service อ่านโปรไฟล์ตามอีเมล | DTO มีชื่อ อีเมล และประเภทบัญชีถูกต้อง; เรียก findByEmailIgnoreCase() | ✅ ผ่าน |

### 34. CloudinaryImageStorageTest — จัดเก็บภาพผ่าน Cloudinary adapter

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/service/storage/CloudinaryImageStorageTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.service.storage.CloudinaryImageStorageTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.service.storage.CloudinaryImageStorageTest.txt)

ผลรวมคลาส: **10 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-34-01 | `mediaUrlSurvivesACompletelyNewStorageInstanceWithoutTheLocalUploadDirectory` | เปิดภาพ cloud หลังสร้าง storage ใหม่และไม่มีโฟลเดอร์ local | ได้ redirect 302 ไป CDN; เปิดภาพไม่เรียก provider API เพิ่ม | ✅ ผ่าน |
| TC-JUNIT-34-02 | `missingCredentialsDoNotSilentlySwitchToEphemeralStorage` | เลือก Cloudinary โดยไม่มี credentials | ได้ 503; ไม่ fallback เป็น local และไม่ส่งคำขอ provider | ✅ ผ่าน |
| TC-JUNIT-34-03 | `removingACloudImageUsesSignedDestroyForOnlyThatImage` | ลบภาพ cloud ด้วย signed destroy และตรวจชื่อไฟล์ผิด | ระบุ public_id ของภาพนั้นและลายเซ็นถูกต้อง; ไม่ส่ง secret; ชื่อผิดไม่เรียก provider | ✅ ผ่าน |
| TC-JUNIT-34-04 | `providerErrorsNeverPretendThePhotoIsSavedAndNeverLeakSecrets(int)[1]` | provider ตอบข้อผิดพลาด HTTP 401 (ชุดค่าที่ 1) | ได้ Problem 503; ไม่เผย API key/secret และล้างไฟล์ staging | ✅ ผ่าน |
| TC-JUNIT-34-05 | `providerErrorsNeverPretendThePhotoIsSavedAndNeverLeakSecrets(int)[2]` | provider ตอบข้อผิดพลาด HTTP 429 (ชุดค่าที่ 2) | ได้ Problem 503; ไม่เผย API key/secret และล้างไฟล์ staging | ✅ ผ่าน |
| TC-JUNIT-34-06 | `providerErrorsNeverPretendThePhotoIsSavedAndNeverLeakSecrets(int)[3]` | provider ตอบข้อผิดพลาด HTTP 503 (ชุดค่าที่ 3) | ได้ Problem 503; ไม่เผย API key/secret และล้างไฟล์ staging | ✅ ผ่าน |
| TC-JUNIT-34-07 | `existingLocalImagesKeepTheirOriginalMediaAddressAndDeleteStillWorks` | เปิดและลบภาพ local เดิมขณะใช้ Cloudinary | เปิดได้ 200; ลบไฟล์ local ได้; ไม่เรียก Cloudinary API | ✅ ผ่าน |
| TC-JUNIT-34-08 | `invalidOrOversizedImagesAreRejectedBeforeAnyProviderRequest` | ภาพปลอม ไฟล์เกิน 5 MB หรือขนาดพิกเซลเกินกำหนด | ได้ 400 ก่อนเรียก provider และไม่มีไฟล์ staging ค้าง | ✅ ผ่าน |
| TC-JUNIT-34-09 | `malformedSuccessResponseCannotCreateAnUnusableOrForeignImageReference` | provider ตอบ success แต่ payload ใช้งานไม่ได้ | ได้ 503 และไม่เหลือไฟล์ staging | ✅ ผ่าน |
| TC-JUNIT-34-10 | `signedUploadPreservesValidationAndStoresAResizedJpegInsteadOfOriginalBytes` | ตรวจภาพ ย่อขนาด และอัปโหลดด้วยลายเซ็น | ภาพ 2800×1400 ถูกแปลงเป็น JPEG 1400×700; ลายเซ็นถูกต้อง ไม่ส่ง secret และล้าง staging | ✅ ผ่าน |

### 35. ImageStorageConfigurationTest — เลือกผู้ให้บริการภาพ

ไฟล์ทดสอบ: `code/src/test/java/com/kku/foodshare/service/storage/ImageStorageConfigurationTest.java`

หลักฐาน: [Surefire XML](surefire/TEST-com.kku.foodshare.service.storage.ImageStorageConfigurationTest.xml) · [สรุป TXT](surefire/com.kku.foodshare.service.storage.ImageStorageConfigurationTest.txt)

ผลรวมคลาส: **2 รายการ ผ่านทั้งหมด**

| Test ID | Test Method | สิ่งที่ทดสอบ | ผลที่คาดหวัง | ผล |
|---|---|---|---|---|
| TC-JUNIT-35-01 | `unknownProviderCannotSilentlyUseTemporaryStorage` | ตั้งชื่อ image provider ไม่ถูกต้อง | Application Context เริ่มไม่ได้ แทนการ fallback เป็น local เงียบ ๆ | ✅ ผ่าน |
| TC-JUNIT-35-02 | `localIsStillTheDefaultAndCloudinaryIsOnlySelectedWhenExplicitlyConfigured` | เลือก storage ตาม config | ค่าเริ่มต้นเป็น local; เลือก cloudinary เมื่อระบุ; ยังมี local storage สำหรับภาพเดิม | ✅ ผ่าน |

## หลักฐานและขอบเขตผลทดสอบ

- [Log Maven/Docker แบบ TXT](java-tests-docker.txt)
- [Log Maven/Docker แบบ LOG](java-tests-docker.log)
- [ผลรายคลาสจาก Surefire](surefire/)

ผล 124 รายการยืนยันเฉพาะกรณีและ assertions ในชุดทดสอบนี้ ไม่ใช่จำนวนฟีเจอร์หรือจำนวน assertions ทั้งหมด ไม่ได้รวม Browser automation หรือ JavaScript tests และไม่ได้ยืนยันเว็บ Production ณ ปัจจุบัน

JUnit ในชุดนี้ประกอบด้วย Unit tests, การตรวจโครงสร้าง/config และ Integration tests ผ่าน Spring/MockMvc ฐานข้อมูลบางคลาสใช้ H2 ในโหมด PostgreSQL ตาม annotation ใน source จึงไม่ควรเรียกผลทั้งชุดว่าเป็นการทดสอบ PostgreSQL จริงทุกคลาส

เทส Brevo/Cloudinary ใช้ HTTP provider จำลอง และเทส SMTP ใช้ SMTP server สำหรับทดสอบ ผลผ่านจึงยืนยัน adapter และกระบวนการในสภาพแวดล้อมทดสอบ ไม่ยืนยัน credentials หรือความพร้อมของบริการภายนอกที่ deploy อยู่

รายละเอียดชื่อ Test Method คงตาม XML รวม suffix `(int)[1]`–`(int)[3]` ของ parameterized test เพื่อจับคู่กับหลักฐานได้ตรงรายการ ไม่มีการเพิ่มผลผ่านให้เมธอดที่ไม่ได้อยู่ใน ZIP
