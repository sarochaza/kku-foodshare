package com.kku.foodshare.service;

import static org.junit.jupiter.api.Assertions.*;
import com.kku.foodshare.domain.entity.*;
import com.kku.foodshare.domain.enums.UserRole;
import com.kku.foodshare.repository.*;
import com.kku.foodshare.exception.Problem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest @Transactional
class AdminPostListingTest {
  @Autowired ModerationService service;
  @Autowired FoodCatalogService catalog;
  @Autowired UserRepository users;
  @Autowired FoodPostRepository posts;
  @Test void adminCanFindExpiredAndCancelledPostsWithoutChangingStock() {
    User admin = user("listing-admin@test.local", UserRole.ADMIN);
    User owner = user("listing-owner@test.local", UserRole.USER);
    FoodPost expired = post(owner, "listing-test อาหารเก่า", FoodPostStatus.EXPIRED);
    FoodPost cancelled = post(owner, "listing-test ปิดแล้ว", FoodPostStatus.CANCELLED);
    assertEquals(cancelled.getId(), catalog.get(cancelled.getId(), admin.getEmail()).id());
    assertEquals(404, assertThrows(Problem.class, () -> catalog.get(cancelled.getId(), null)).status);
    assertEquals(2, service.posts(admin.getEmail(), 0, "listing-test", "").totalElements());
    var result = service.posts(admin.getEmail(), 0, "listing-test", "EXPIRED");
    assertEquals(1, result.totalElements());
    assertEquals(expired.getId(), result.items().get(0).id());
    assertEquals(owner.getDisplayName(), result.items().get(0).owner());
    assertEquals(5, result.items().get(0).availableQuantity());
    service.report(owner.getEmail(), expired.getId(), null, "ตรวจสอบโพสต์");
    assertTrue(service.pendingReports(admin.getEmail()) >= 1);
    assertEquals(403, assertThrows(Problem.class, () -> service.pendingReports(owner.getEmail())).status);
    assertEquals(5, posts.findById(expired.getId()).orElseThrow().getAvailableQuantity());
    assertEquals(403, assertThrows(Problem.class, () -> service.posts(owner.getEmail(), 0, "", "")).status);
    assertEquals(400, assertThrows(Problem.class, () -> service.posts(admin.getEmail(), 0, "", "BAD")).status);
  }
  User user(String email, UserRole role) {
    User u = new User(); u.setEmail(email); u.setDisplayName("ผู้แบ่งปัน"); u.setPassword("unused"); u.setRole(role);
    return users.saveAndFlush(u);
  }
  FoodPost post(User owner, String title, FoodPostStatus status) {
    var p = new FoodPost(); var now = LocalDateTime.now();
    p.setOwner(owner); p.setTitle(title); p.setDescription("รายละเอียด"); p.setCategory(FoodCategory.FOOD);
    p.setQuantity(5); p.setUnit("กล่อง"); p.setPickupLocationName("มข.");
    p.setLatitude(new BigDecimal("16.47")); p.setLongitude(new BigDecimal("102.82"));
    p.setAvailableFrom(now.minusDays(1)); p.setAvailableUntil(now.minusHours(1));
    p.setStatus(status); p.setCreatedAt(now); p.setUpdatedAt(now); return posts.saveAndFlush(p);
  }
}
