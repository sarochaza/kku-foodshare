package com.kku.foodshare.service;

import static org.junit.jupiter.api.Assertions.*;

import com.kku.foodshare.domain.entity.*;
import com.kku.foodshare.repository.*;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class CommentRepliesPersistenceTest {
  @Autowired CommentService service;
  @Autowired UserRepository users;
  @Autowired FoodPostRepository posts;
  @Autowired EntityManager entities;

  @Test void selfReferencesSurviveReloadAndSoftDeletionKeepsRepliesReadable() {
    User owner = user("reply-owner@test.local"), member = user("reply-member@test.local");
    LocalDateTime now = LocalDateTime.now();
    FoodPost post = new FoodPost();
    post.setOwner(owner); post.setTitle("อาหารทดสอบตอบกลับ"); post.setDescription("รายละเอียด");
    post.setCategory(FoodCategory.FOOD); post.setQuantity(5); post.setUnit("กล่อง");
    post.setPickupLocationName("มข."); post.setLatitude(new BigDecimal("16.47")); post.setLongitude(new BigDecimal("102.82"));
    post.setAvailableFrom(now); post.setAvailableUntil(now.plusHours(2));
    post.setStatus(FoodPostStatus.AVAILABLE); post.setCreatedAt(now); post.setUpdatedAt(now);
    post = posts.saveAndFlush(post); long postId = post.getId();
    var root = service.add(member.getEmail(), postId, "ถามรายละเอียด");
    var child = service.add(owner.getEmail(), postId, "ตอบผู้รับ", root.id());
    entities.flush(); entities.clear();
    var nested = service.add(member.getEmail(), postId, "ขอบคุณ", child.id());
    assertEquals(root.id(), nested.parentCommentId()); assertEquals(child.id(), nested.replyToCommentId());
    entities.flush(); entities.clear();
    service.remove(owner.getEmail(), root.id());
    entities.flush(); entities.clear();
    var remaining = service.list(postId, member.getEmail(), 0);
    assertEquals(2, remaining.totalElements());
    assertTrue(remaining.items().stream().allMatch(CommentService.View::parentDeleted));
    assertEquals(root.id(), remaining.items().get(0).parentCommentId());
    assertNull(remaining.items().get(0).replyToAuthorName());
    assertNotNull(remaining.items().get(1).replyToAuthorName());
  }

  private User user(String email) {
    User user = new User(); user.setEmail(email); user.setDisplayName(email); user.setPassword("test-only-unused");
    return users.saveAndFlush(user);
  }
}
