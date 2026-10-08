package com.kku.foodshare.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kku.foodshare.domain.entity.*;
import com.kku.foodshare.dto.request.FoodPostRequest;
import com.kku.foodshare.dto.response.ReservationView;
import com.kku.foodshare.repository.*;
import com.kku.foodshare.service.impl.PickupReminderService;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:pickup-reminders;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DEFAULT_NULL_ORDERING=HIGH",
    "app.pickup-reminder.scheduled=false"
})
@Import(PickupReminderIntegrationTest.Time.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PickupReminderIntegrationTest {
  private static final Instant START = Instant.parse("2026-10-08T10:00:00Z");
  @Autowired FoodCatalogService catalog;
  @Autowired ReservationService reservations;
  @Autowired NotificationService notifications;
  @Autowired PickupReminderService reminders;
  @Autowired UserRepository users;
  @Autowired ReservationRepository reservationRepository;
  @Autowired NotificationRepository notificationRepository;
  @Autowired PlatformTransactionManager transactions;
  @Autowired MutableClock clock;
  @Autowired MockMvc mvc;

  @BeforeEach void resetTime() { clock.instant = START; }

  @Test void thirtyMinuteBoundaryCreatesOneReadableReminderAndLeavesStockAndQrUntouched() {
    var fixture = booking(31, 2, 5);
    assertEquals(0, reminders.remindDue(fixture.receiver.getId()));
    clock.instant = START.plusSeconds(60);
    assertEquals(1, reminders.remindDue(fixture.receiver.getId()));
    assertEquals(0, reminders.remindDue(fixture.receiver.getId()));
    var notice = reminder(fixture);
    assertEquals("ใกล้หมดเวลารับอาหารที่จอง", notice.title());
    assertTrue(notice.message().contains("ข้าวกล่องแบ่งปัน"));
    assertTrue(notice.message().contains("2 กล่อง"));
    assertTrue(notice.message().contains("หอสมุด มข."));
    assertTrue(notice.message().contains("17:31"));
    assertEquals("/reservations#reservation-" + fixture.reservation.id(), notice.href());
    assertEquals("RESERVATION", notice.type()); assertFalse(notice.read());
    assertEquals("ผู้แบ่งปัน", notice.actorName());
    var after = reservations.get(fixture.receiver.getEmail(), fixture.reservation.id());
    assertEquals("RESERVED", after.status()); assertEquals(fixture.reservation.pickupCode(), after.pickupCode());
    var post = catalog.get(fixture.reservation.post().id(), fixture.owner.getEmail());
    assertEquals(5, post.quantity()); assertEquals(2, post.reservedQuantity());
    assertEquals(3, post.availableQuantity()); assertEquals(0, post.collectedQuantity());
    notifications.read(fixture.receiver.getEmail(), notice.id());
    assertTrue(reminder(fixture).read());
  }

  @Test void reservationsWithZeroPublicStockStillReceiveAReminder() {
    var fixture = booking(15, 1, 1);
    assertEquals(0, catalog.get(fixture.reservation.post().id(), fixture.owner.getEmail()).availableQuantity());
    assertEquals(1, reminders.remindDue(fixture.receiver.getId()));
    assertEquals("RESERVATION", reminder(fixture).type());
  }

  @Test void cancelledCollectedExpiredAndClosedReservationsAreNotReminded() {
    var cancelled = booking(15, 1, 3);
    reservations.cancel(cancelled.receiver.getEmail(), cancelled.reservation.id());
    assertEquals(0, reminders.remindDue(cancelled.receiver.getId()));
    var collected = booking(15, 1, 3);
    reservations.collect(collected.owner.getEmail(), collected.reservation.id(), collected.reservation.pickupCode());
    assertEquals(0, reminders.remindDue(collected.receiver.getId()));
    var closed = booking(15, 1, 3);
    catalog.delete(closed.owner.getEmail(), closed.reservation.post().id());
    assertEquals(0, reminders.remindDue(closed.receiver.getId()));
    var expired = booking(15, 1, 3);
    clock.instant = START.plusSeconds(15 * 60);
    assertEquals(0, reminders.remindDue(expired.receiver.getId()));
  }

  @Test void disabledReceiverOrOwnerDoesNotReceiveAPickupReminder() {
    var receiver = booking(15, 1, 3);
    receiver.receiver.setActive(false); users.save(receiver.receiver);
    assertEquals(0, reminders.remindDue(receiver.receiver.getId()));
    var owner = booking(15, 1, 3);
    owner.owner.setActive(false); users.save(owner.owner);
    assertEquals(0, reminders.remindDue(owner.receiver.getId()));
  }

  @Test void inboxAndBadgeCatchUpWhenFreeHostingWasAsleepAndStayScopedToTheSignedInUser() throws Exception {
    var first = booking(15, 1, 3);
    var second = booking(15, 1, 3);
    clock.instant = START.plusSeconds(1);
    long before = notificationRepository.countByUserIdAndReadAtIsNull(first.receiver.getId());
    mvc.perform(get("/api/v1/me/notifications/unread").with(user(first.receiver.getEmail())))
        .andExpect(status().isOk()).andExpect(jsonPath("count").value(before + 1));
    assertFalse(notificationRepository.existsByUserIdAndDedupeKey(second.receiver.getId(), "pickup-deadline:" + second.reservation.id()));
    mvc.perform(get("/api/v1/me/notifications").with(user(second.receiver.getEmail())))
        .andExpect(status().isOk()).andExpect(jsonPath("items[0].href").value("/reservations#reservation-" + second.reservation.id()));
    assertEquals(0, reminders.remindDue(first.receiver.getId()));
    assertEquals(0, reminders.remindDue(second.receiver.getId()));
    mvc.perform(get("/api/v1/me/notifications/unread")).andExpect(status().isUnauthorized());
  }

  @Test void concurrentSchedulerAndInboxChecksCreateOnlyOneReminder() throws Exception {
    var fixture = booking(15, 1, 3);
    var pool = Executors.newFixedThreadPool(2); var start = new CountDownLatch(1);
    try {
      Callable<Integer> run = () -> { start.await(); return reminders.remindDue(fixture.receiver.getId()); };
      var first = pool.submit(run); var second = pool.submit(run); start.countDown();
      assertEquals(1, first.get(10, TimeUnit.SECONDS) + second.get(10, TimeUnit.SECONDS));
      var rows = notificationRepository.findByUserIdOrderByCreatedAtDesc(fixture.receiver.getId(), org.springframework.data.domain.PageRequest.of(0, 20));
      assertEquals(1, rows.stream().filter(n -> ("pickup-deadline:" + fixture.reservation.id()).equals(n.dedupeKey)).count());
      assertEquals("RESERVED", reservations.get(fixture.receiver.getEmail(), fixture.reservation.id()).status());
    } finally { pool.shutdownNow(); }
  }

  @Test void longPickupNamesStayWithinTheExistingNotificationColumnAndNamesRemainEscapable() {
    var fixture = booking(15, 1, 3, "<script>ชื่ออาหาร</script>" + "ก".repeat(100), "ข".repeat(255), "ค".repeat(50));
    assertEquals(1, reminders.remindDue(fixture.receiver.getId()));
    var n = reminder(fixture);
    assertTrue(n.message().length() <= 500); assertTrue(n.message().contains("<script>"));
    assertTrue(n.message().contains("17:15"), "Deadline remains visible even with maximum-length title and pickup name");
  }

  private NotificationService.View reminder(Fixture fixture) {
    return notifications.list(fixture.receiver.getEmail(), 0).items().stream()
        .filter(n -> n.title().equals("ใกล้หมดเวลารับอาหารที่จอง")).findFirst().orElseThrow();
  }
  private Fixture booking(int minutes, int quantity, int total) {
    return booking(minutes, quantity, total, "ข้าวกล่องแบ่งปัน", "หอสมุด มข.", "กล่อง");
  }
  private Fixture booking(int minutes, int quantity, int total, String title, String location, String unit) {
    var owner = account("ผู้แบ่งปัน"); var receiver = account("ผู้รับ");
    LocalDateTime now = LocalDateTime.now(clock);
    var p = catalog.create(owner.getEmail(), new FoodPostRequest(title, "อาหารปรุงใหม่", FoodCategory.FOOD,
        total, unit, location, new BigDecimal("16.474"), new BigDecimal("102.823"), now.minusMinutes(5), now.plusMinutes(minutes), "ไข่", null));
    var reservation = reservations.reserve(receiver.getEmail(), p.id(), quantity, UUID.randomUUID().toString());
    return new Fixture(owner, receiver, reservation);
  }
  private User account(String name) {
    User u = new User(); u.setEmail(UUID.randomUUID() + "@reminder.test"); u.setDisplayName(name); u.setPassword("unused");
    return users.saveAndFlush(u);
  }
  private record Fixture(User owner, User receiver, ReservationView reservation) {}
  @TestConfiguration static class Time {
    @Bean @Primary MutableClock reminderClock() { return new MutableClock(); }
  }
  static class MutableClock extends Clock {
    volatile Instant instant = START;
    public ZoneId getZone() { return ZoneId.of("Asia/Bangkok"); }
    public Clock withZone(ZoneId zone) { return Clock.fixed(instant, zone); }
    public Instant instant() { return instant; }
  }
}
