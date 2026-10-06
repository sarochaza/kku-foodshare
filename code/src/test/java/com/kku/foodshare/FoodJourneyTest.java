package com.kku.foodshare;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class FoodJourneyTest {
  @Autowired MockMvc mvc;
  @Autowired UserRepository users;

  @BeforeEach
  void accounts() {
    for (String email : new String[] {"owner@test.local", "receiver@test.local"}) {
      if (users.findByEmailIgnoreCase(email).isEmpty()) {
        var u = new User();
        u.setEmail(email);
        u.setPassword("unused");
        u.setDisplayName(email);
        users.save(u);
      }
    }
  }

  String validPost() {
    return """
{"title":"ข้าวกล่องแบ่งปัน","description":"อาหารปรุงใหม่","category":"FOOD","quantity":5,"unit":"กล่อง",
 "pickupLocationName":"หอสมุด มข.","latitude":16.474,"longitude":102.823,
 "availableFrom":"%s","availableUntil":"%s","allergens":"ไข่"}
"""
        .formatted(
            java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Bangkok")).minusMinutes(5),
            java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Bangkok")).plusHours(2));
  }

  @Test
  void createPersistsCoordinatesAndAvailableQuantity() throws Exception {
    mvc.perform(
            post("/api/v1/food-posts")
                .with(user("owner@test.local"))
                .with(csrf())
                .contentType("application/json")
                .content(validPost()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("availableQuantity").value(5))
        .andExpect(jsonPath("latitude").value(16.474));
  }

  @Test
  void invalidQuantityIsRejected() throws Exception {
    mvc.perform(
            post("/api/v1/food-posts")
                .with(user("owner@test.local"))
                .with(csrf())
                .contentType("application/json")
                .content(validPost().replace("\"quantity\":5", "\"quantity\":0")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void unauthenticatedMutationIsRejected() throws Exception {
    mvc.perform(
            post("/api/v1/food-posts")
                .with(csrf())
                .contentType("application/json")
                .content(validPost()))
        .andExpect(status().isUnauthorized());
  }

  long createPost() throws Exception {
    String json =
        mvc.perform(
                post("/api/v1/food-posts")
                    .with(user("owner@test.local"))
                    .with(csrf())
                    .contentType("application/json")
                    .content(validPost()))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return ((Number) com.jayway.jsonpath.JsonPath.read(json, "$.id")).longValue();
  }

  String reserve(long id, String email, int quantity, String key) throws Exception {
    return mvc.perform(
            post("/api/v1/food-posts/" + id + "/reservations")
                .with(user(email))
                .with(csrf())
                .header("Idempotency-Key", key)
                .contentType("application/json")
                .content("{\"quantity\":" + quantity + "}"))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();
  }

  @Test
  void cancellingTwiceRestoresStockOnlyOnce() throws Exception {
    long id = createPost();
    String reservation =
        reserve(id, "receiver@test.local", 2, java.util.UUID.randomUUID().toString());
    long rid = ((Number) com.jayway.jsonpath.JsonPath.read(reservation, "$.id")).longValue();
    mvc.perform(get("/api/v1/food-posts/" + id)).andExpect(jsonPath("availableQuantity").value(3));
    for (int i = 0; i < 2; i++)
      mvc.perform(
              delete("/api/v1/reservations/" + rid).with(user("receiver@test.local")).with(csrf()))
          .andExpect(status().isNoContent());
    mvc.perform(get("/api/v1/food-posts/" + id)).andExpect(jsonPath("availableQuantity").value(5));
  }

  @Test
  void ownReservationAndForeignMutationAreForbidden() throws Exception {
    long id = createPost();
    mvc.perform(
            post("/api/v1/food-posts/" + id + "/reservations")
                .with(user("owner@test.local"))
                .with(csrf())
                .header("Idempotency-Key", java.util.UUID.randomUUID().toString())
                .contentType("application/json")
                .content("{\"quantity\":1}"))
        .andExpect(status().isForbidden());
    mvc.perform(
            put("/api/v1/food-posts/" + id)
                .with(user("receiver@test.local"))
                .with(csrf())
                .contentType("application/json")
                .content(validPost()))
        .andExpect(status().isForbidden());
  }

  @Test
  void collectIsIdempotentAndPrivate() throws Exception {
    long id = createPost();
    String json = reserve(id, "receiver@test.local", 2, java.util.UUID.randomUUID().toString());
    long rid = ((Number) com.jayway.jsonpath.JsonPath.read(json, "$.id")).longValue();
    String code = com.jayway.jsonpath.JsonPath.read(json, "$.pickupCode");
    mvc.perform(get("/api/v1/reservations/" + rid).with(user("owner@test.local")))
        .andExpect(jsonPath("pickupCode").isEmpty());
    for (int i = 0; i < 2; i++)
      mvc.perform(
              post("/api/v1/reservations/" + rid + "/collection")
                  .with(user("owner@test.local"))
                  .with(csrf())
                  .contentType("application/json")
                  .content("{\"code\":\"" + code + "\"}"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("status").value("COLLECTED"));
    mvc.perform(get("/api/v1/food-posts/" + id))
        .andExpect(jsonPath("collectedQuantity").value(2))
        .andExpect(jsonPath("availableQuantity").value(3));
  }

  @Test
  void retryUsesSameReservation() throws Exception {
    long id = createPost();
    String key = java.util.UUID.randomUUID().toString();
    String a = reserve(id, "receiver@test.local", 2, key),
        b = reserve(id, "receiver@test.local", 2, key);
    org.junit.jupiter.api.Assertions.assertEquals(
        (Object) com.jayway.jsonpath.JsonPath.read(a, "$.id"),
        com.jayway.jsonpath.JsonPath.read(b, "$.id"));
    mvc.perform(get("/api/v1/food-posts/" + id)).andExpect(jsonPath("availableQuantity").value(3));
  }

  @Test
  void memberCannotModerate() throws Exception {
    mvc.perform(get("/api/v1/admin/reports").with(user("receiver@test.local")))
        .andExpect(status().isForbidden());
  }

  @Test
  void foreignReservationCannotBeRead() throws Exception {
    long id = createPost();
    String json = reserve(id, "receiver@test.local", 1, java.util.UUID.randomUUID().toString());
    long rid = ((Number) com.jayway.jsonpath.JsonPath.read(json, "$.id")).longValue();
    var stranger = new User();
    stranger.setEmail(java.util.UUID.randomUUID() + "@test.local");
    stranger.setPassword("unused");
    stranger.setDisplayName("stranger");
    users.save(stranger);
    mvc.perform(get("/api/v1/reservations/" + rid).with(user(stranger.getEmail())))
        .andExpect(status().isForbidden());
  }

  @Test
  void lastItemCanOnlyBeReservedOnce() throws Exception {
    long id = createPost();
    String other = java.util.UUID.randomUUID() + "@test.local";
    var u = new User();
    u.setEmail(other);
    u.setPassword("unused");
    u.setDisplayName("other");
    users.save(u);
    var pool = java.util.concurrent.Executors.newFixedThreadPool(2);
    var latch = new java.util.concurrent.CountDownLatch(1);
    try {
      java.util.List<java.util.concurrent.Future<Integer>> results = new java.util.ArrayList<>();
      for (String email : java.util.List.of("receiver@test.local", other))
        results.add(
            pool.submit(
                () -> {
                  latch.await();
                  return mvc.perform(
                          post("/api/v1/food-posts/" + id + "/reservations")
                              .with(user(email))
                              .with(csrf())
                              .header("Idempotency-Key", java.util.UUID.randomUUID().toString())
                              .contentType("application/json")
                              .content("{\"quantity\":5}"))
                      .andReturn()
                      .getResponse()
                      .getStatus();
                }));
      latch.countDown();
      var statuses = new java.util.ArrayList<Integer>();
      for (var r : results) statuses.add(r.get(20, java.util.concurrent.TimeUnit.SECONDS));
      java.util.Collections.sort(statuses);
      org.junit.jupiter.api.Assertions.assertEquals(java.util.List.of(201, 409), statuses);
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void wrongPickupCodeLocksAfterFiveAttempts() throws Exception {
    long id = createPost();
    String json = reserve(id, "receiver@test.local", 1, java.util.UUID.randomUUID().toString());
    long rid = ((Number) com.jayway.jsonpath.JsonPath.read(json, "$.id")).longValue();
    String code = com.jayway.jsonpath.JsonPath.read(json, "$.pickupCode");
    String wrong = code.equals("000000") ? "111111" : "000000";
    for (int n = 0; n < 5; n++)
      mvc.perform(
              post("/api/v1/reservations/" + rid + "/collection")
                  .with(user("owner@test.local"))
                  .with(csrf())
                  .contentType("application/json")
                  .content("{\"code\":\"" + wrong + "\"}"))
          .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/v1/reservations/" + rid + "/collection")
                .with(user("owner@test.local"))
                .with(csrf())
                .contentType("application/json")
                .content("{\"code\":\"" + code + "\"}"))
        .andExpect(status().isTooManyRequests());
    mvc.perform(get("/api/v1/food-posts/" + id))
        .andExpect(jsonPath("reservedQuantity").value(1))
        .andExpect(jsonPath("collectedQuantity").value(0));
  }

  @Test
  void closePostCancelsPendingReservation() throws Exception {
    long id = createPost();
    String json = reserve(id, "receiver@test.local", 2, java.util.UUID.randomUUID().toString());
    long rid = ((Number) com.jayway.jsonpath.JsonPath.read(json, "$.id")).longValue();
    mvc.perform(delete("/api/v1/food-posts/" + id).with(user("owner@test.local")).with(csrf()))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/v1/reservations/" + rid).with(user("receiver@test.local")))
        .andExpect(jsonPath("status").value("CANCELLED"));
    mvc.perform(get("/api/v1/food-posts/" + id)).andExpect(status().isNotFound());
  }

  @Test
  void quantityCannotDropBelowExistingReservations() throws Exception {
    long id = createPost();
    reserve(id, "receiver@test.local", 4, java.util.UUID.randomUUID().toString());
    mvc.perform(
            put("/api/v1/food-posts/" + id)
                .with(user("owner@test.local"))
                .with(csrf())
                .contentType("application/json")
                .content(validPost().replace("\"quantity\":5", "\"quantity\":3")))
        .andExpect(status().isConflict());
    mvc.perform(get("/api/v1/food-posts/" + id)).andExpect(jsonPath("availableQuantity").value(1));
  }

  @Test
  void imageUploadValidatesPixelsAndServesPersistedImage() throws Exception {
    long id = createPost();
    var bad =
        new org.springframework.mock.web.MockMultipartFile(
            "file", "bad.png", "image/png", "not an image".getBytes());
    mvc.perform(
            multipart("/api/v1/food-posts/" + id + "/images")
                .file(bad)
                .with(user("owner@test.local"))
                .with(csrf()))
        .andExpect(status().isBadRequest());
    var bytes = new java.io.ByteArrayOutputStream();
    javax.imageio.ImageIO.write(
        new java.awt.image.BufferedImage(12, 12, java.awt.image.BufferedImage.TYPE_INT_RGB),
        "png",
        bytes);
    var good =
        new org.springframework.mock.web.MockMultipartFile(
            "file", "food.png", "image/png", bytes.toByteArray());
    String json =
        mvc.perform(
                multipart("/api/v1/food-posts/" + id + "/images")
                    .file(good)
                    .with(user("owner@test.local"))
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String url = com.jayway.jsonpath.JsonPath.read(json, "$.imageUrl");
    mvc.perform(get(url)).andExpect(status().isOk()).andExpect(content().contentType("image/jpeg"));
  }

  @Test
  void administratorCanModerateButSuspendedSessionCannotContinue() throws Exception {
    var admin = new User();
    admin.setEmail(java.util.UUID.randomUUID() + "@test.local");
    admin.setDisplayName("admin");
    admin.setPassword("unused");
    admin.setRole(com.kku.foodshare.domain.enums.UserRole.ADMIN);
    users.save(admin);
    var target = new User();
    target.setEmail(java.util.UUID.randomUUID() + "@test.local");
    target.setDisplayName("member");
    target.setPassword("unused");
    users.save(target);
    mvc.perform(get("/api/v1/admin/users").with(user(admin.getEmail()))).andExpect(status().isOk());
    mvc.perform(
            patch("/api/v1/admin/users/" + target.getId())
                .with(user(admin.getEmail()))
                .with(csrf())
                .contentType("application/json")
                .content("{\"active\":false,\"reason\":\"ทดสอบระงับ\"}"))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/v1/me/reservations").with(user(target.getEmail())))
        .andExpect(status().isForbidden());
  }
}
