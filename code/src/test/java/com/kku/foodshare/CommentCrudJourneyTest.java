package com.kku.foodshare;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import com.kku.foodshare.domain.entity.*;
import com.kku.foodshare.domain.enums.UserRole;
import com.kku.foodshare.repository.*;
import com.kku.foodshare.service.CommentService;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentCrudJourneyTest {
  @Autowired MockMvc mvc;
  @Autowired CommentService service;
  @Autowired UserRepository users;
  @Autowired FoodPostRepository posts;
  @Autowired PostCommentRepository comments;
  @Autowired EntityManager entities;
  private User owner, writer, stranger, admin;
  private long postId;

  @BeforeEach void setup() {
    owner = member(UserRole.USER); writer = member(UserRole.USER);
    stranger = member(UserRole.USER); admin = member(UserRole.ADMIN);
    LocalDateTime now = LocalDateTime.now();
    FoodPost food = new FoodPost();
    food.setOwner(owner); food.setTitle("อาหารทดสอบ CRUD"); food.setDescription("รายละเอียด");
    food.setCategory(FoodCategory.FOOD); food.setQuantity(5); food.setUnit("กล่อง");
    food.setPickupLocationName("มข."); food.setLatitude(new BigDecimal("16.47")); food.setLongitude(new BigDecimal("102.82"));
    food.setAvailableFrom(now); food.setAvailableUntil(now.plusHours(2));
    food.setStatus(FoodPostStatus.AVAILABLE); food.setCreatedAt(now); food.setUpdatedAt(now);
    postId = posts.saveAndFlush(food).getId();
  }

  @Test void completeCrudUsesCorrectStatusesAndPersistsTheEditedBody() throws Exception {
    var response = mvc.perform(post("/api/v1/food-posts/{id}/comments", postId)
            .with(user(writer.getEmail())).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"body\":\"ยังมีอาหารไหม\"}"))
        .andExpect(status().isCreated()).andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.canEdit").value(true)).andReturn();
    Number value = JsonPath.read(response.getResponse().getContentAsString(), "$.id");
    long id = value.longValue();
    assertEquals("/api/v1/comments/" + id, response.getResponse().getHeader("Location"));
    mvc.perform(get("/api/v1/comments/{id}", id).with(user(writer.getEmail())))
        .andExpect(status().isOk()).andExpect(jsonPath("$.body").value("ยังมีอาหารไหม"));
    mvc.perform(put("/api/v1/comments/{id}", id).with(user(writer.getEmail())).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content("{\"body\":\"  ขอรับ 1 กล่อง  \"}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.body").value("ขอรับ 1 กล่อง"));
    entities.flush(); entities.clear();
    assertEquals("ขอรับ 1 กล่อง", comments.findById(id).orElseThrow().body);
    mvc.perform(get("/api/v1/food-posts/{id}/comments", postId).with(user(writer.getEmail())))
        .andExpect(status().isOk()).andExpect(jsonPath("$.items[0].body").value("ขอรับ 1 กล่อง"))
        .andExpect(jsonPath("$.totalElements").value(1));
    mvc.perform(delete("/api/v1/comments/{id}", id).with(user(writer.getEmail())).with(csrf()))
        .andExpect(status().isNoContent()).andExpect(content().string(""));
    mvc.perform(get("/api/v1/comments/{id}", id).with(user(writer.getEmail())))
        .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
    assertEquals(0, service.list(postId, writer.getEmail(), 0).totalElements());
  }

  @Test void onlyTheAuthorCanEditEvenWhenOthersHaveModerationDeletionRights() throws Exception {
    long id = service.add(writer.getEmail(), postId, "ข้อความของผู้เขียน").id();
    for (User actor : new User[] {stranger, owner, admin}) {
      mvc.perform(get("/api/v1/comments/{id}", id).with(user(actor.getEmail())))
          .andExpect(status().isOk()).andExpect(jsonPath("$.canEdit").value(false))
          .andExpect(jsonPath("$.canDelete").value(actor != stranger));
      mvc.perform(put("/api/v1/comments/{id}", id).with(user(actor.getEmail())).with(csrf())
              .contentType(MediaType.APPLICATION_JSON).content("{\"body\":\"แก้แทนผู้เขียน\"}"))
          .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));
    }
    assertEquals("ข้อความของผู้เขียน", comments.findById(id).orElseThrow().body);
  }

  @Test void blankOverlongAndMalformedUpdatesLeaveTheOriginalBodyUntouched() throws Exception {
    long id = service.add(writer.getEmail(), postId, "ต้นฉบับ").id();
    String[] bodies = {"{\"body\":\"   \"}", "{\"body\":\"" + "a".repeat(801) + "\"}", "{}", "{"};
    for (String body : bodies) {
      mvc.perform(put("/api/v1/comments/{id}", id).with(user(writer.getEmail())).with(csrf())
              .contentType(MediaType.APPLICATION_JSON).content(body))
          .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }
    assertEquals("ต้นฉบับ", comments.findById(id).orElseThrow().body);
    mvc.perform(put("/api/v1/comments/{id}", id).with(user(writer.getEmail())).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content("{\"body\":\"" + "a".repeat(800) + "\"}"))
        .andExpect(status().isOk());
  }

  @Test void updateCannotMoveAReplyChangeItsAuthorOrCreationTime() throws Exception {
    var root = service.add(owner.getEmail(), postId, "ข้อมูลอาหาร");
    var reply = service.add(writer.getEmail(), postId, "ขอรับ", root.id());
    mvc.perform(put("/api/v1/comments/{id}", reply.id()).with(user(writer.getEmail())).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content("{\"body\":\"ขอรับ 2 กล่อง\",\"parentCommentId\":999,\"authorId\":999}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.parentCommentId").value(root.id()))
        .andExpect(jsonPath("$.replyToCommentId").value(root.id()))
        .andExpect(jsonPath("$.authorId").value(writer.getId()));
    entities.flush(); entities.clear();
    var saved = comments.findById(reply.id()).orElseThrow();
    assertEquals(root.id(), saved.parent.getId()); assertEquals(root.id(), saved.replyTo.getId());
    assertEquals(reply.createdAt().withNano(0), saved.createdAt.withNano(0));
  }

  @Test void missingAndDeletedCommentsCannotBeReadOrUpdated() throws Exception {
    long id = service.add(writer.getEmail(), postId, "ลบแล้ว").id();
    service.remove(writer.getEmail(), id);
    for (long target : new long[] {id, Long.MAX_VALUE}) {
      mvc.perform(get("/api/v1/comments/{id}", target).with(user(writer.getEmail())))
          .andExpect(status().isNotFound());
      mvc.perform(put("/api/v1/comments/{id}", target).with(user(writer.getEmail())).with(csrf())
              .contentType(MediaType.APPLICATION_JSON).content("{\"body\":\"แก้ไข\"}"))
          .andExpect(status().isNotFound());
    }
  }

  @Test void editingAReplyAfterRootDeletionKeepsTheRemainingThread() throws Exception {
    var root = service.add(owner.getEmail(), postId, "ต้นทาง");
    var reply = service.add(writer.getEmail(), postId, "คำตอบ", root.id());
    service.remove(owner.getEmail(), root.id());
    mvc.perform(put("/api/v1/comments/{id}", reply.id()).with(user(writer.getEmail())).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content("{\"body\":\"แก้คำตอบ\"}"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.parentDeleted").value(true))
        .andExpect(jsonPath("$.replyToAuthorName").isEmpty());
    assertEquals(1, service.list(postId, writer.getEmail(), 0).totalElements());
  }

  @Test void authenticationAndCsrfAreStillRequired() throws Exception {
    long id = service.add(writer.getEmail(), postId, "ต้นฉบับ").id();
    mvc.perform(get("/api/v1/comments/{id}", id)).andExpect(status().isUnauthorized());
    mvc.perform(put("/api/v1/comments/{id}", id).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"body\":\"แก้ไข\"}"))
        .andExpect(status().isUnauthorized());
    mvc.perform(put("/api/v1/comments/{id}", id).with(user(writer.getEmail()))
            .contentType(MediaType.APPLICATION_JSON).content("{\"body\":\"แก้ไข\"}"))
        .andExpect(status().isForbidden());
    assertEquals("ต้นฉบับ", comments.findById(id).orElseThrow().body);
  }

  @Test void swaggerPublishesCommentReadAndUpdateContracts() throws Exception {
    mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
        .andExpect(jsonPath("$.paths['/api/v1/comments/{id}'].get").exists())
        .andExpect(jsonPath("$.paths['/api/v1/comments/{id}'].put").exists())
        .andExpect(jsonPath("$.paths['/api/v1/comments/{id}'].delete").exists());
  }

  private User member(UserRole role) {
    User member = new User(); member.setEmail(UUID.randomUUID() + "@comment.test");
    member.setPassword("unused"); member.setDisplayName("สมาชิก " + role); member.setRole(role);
    return users.saveAndFlush(member);
  }
}
