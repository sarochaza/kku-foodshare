package com.kku.foodshare.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.kku.foodshare.domain.entity.*;
import com.kku.foodshare.domain.enums.UserRole;
import com.kku.foodshare.exception.Problem;
import com.kku.foodshare.repository.*;
import com.kku.foodshare.service.event.ActivityNotice;
import com.kku.foodshare.service.impl.CommentServiceImpl;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;

class CommentRepliesTest {
  private final PostCommentRepository comments = mock(PostCommentRepository.class);
  private final FoodPostRepository posts = mock(FoodPostRepository.class);
  private final MemberService members = mock(MemberService.class);
  private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
  private final Clock clock = Clock.fixed(Instant.parse("2026-10-07T10:00:00Z"), ZoneOffset.UTC);
  private final CommentService service = new CommentServiceImpl(comments, posts, members, clock, events);
  private final User owner = user(1), writer = user(2), actor = user(3);
  private final FoodPost post = post(10, owner);

  private static User user(long id) {
    User u = new User(); u.setId(id); u.setEmail(id + "@test.local"); u.setDisplayName("สมาชิก " + id); return u;
  }
  private static FoodPost post(long id, User owner) {
    FoodPost p = new FoodPost(); p.setId(id); p.setOwner(owner); p.setTitle("ข้าวแบ่งปัน"); return p;
  }
  private PostComment comment(long id, User author) {
    PostComment c = new PostComment(); c.id = id; c.post = post; c.author = author;
    c.body = "ถามรายละเอียด"; c.createdAt = LocalDateTime.now(clock); return c;
  }
  private void setup(User member) {
    when(members.require(member.getEmail())).thenReturn(member);
    when(posts.findById(10L)).thenReturn(Optional.of(post));
    when(comments.save(any())).thenAnswer(call -> {PostComment c = call.getArgument(0); c.id = 99L; return c;});
  }

  @Test void existingRootCommentPayloadStillWorksAndNotifiesOwnerWithActor() {
    setup(actor);
    var result = service.add(actor.getEmail(), 10, "  ยังมี\nอาหารไหม  ");
    assertNull(result.parentCommentId()); assertNull(result.replyToCommentId());
    assertEquals("ยังมี อาหารไหม", result.body());
    ArgumentCaptor<ActivityNotice> notice = ArgumentCaptor.forClass(ActivityNotice.class);
    verify(events).publishEvent(notice.capture());
    assertEquals(owner, notice.getValue().user()); assertEquals(actor, notice.getValue().actor());
    assertEquals("/posts/10#post-comments", notice.getValue().href());
  }

  @Test void replyNotifiesOwnerAndTargetWithoutDuplicateRecipients() {
    setup(actor); PostComment root = comment(5, writer);
    when(comments.lockActive(5L)).thenReturn(Optional.of(root));
    var result = service.add(actor.getEmail(), 10, "ตอบกลับ", 5L);
    assertEquals(5L, result.parentCommentId()); assertEquals(5L, result.replyToCommentId());
    assertEquals(writer.getDisplayName(), result.replyToAuthorName());
    ArgumentCaptor<ActivityNotice> notices = ArgumentCaptor.forClass(ActivityNotice.class);
    verify(events, times(2)).publishEvent(notices.capture());
    assertEquals(Set.of(owner, writer), new HashSet<>(notices.getAllValues().stream().map(ActivityNotice::user).toList()));
  }

  @Test void replyToOwnerOnlyCreatesOneNoticeAndSelfCommentsCreateNone() {
    setup(actor); when(comments.lockActive(5L)).thenReturn(Optional.of(comment(5, owner)));
    service.add(actor.getEmail(), 10, "ตอบ", 5L);
    verify(events, times(1)).publishEvent(any(ActivityNotice.class));
    reset(events); setup(owner); service.add(owner.getEmail(), 10, "รายละเอียดเพิ่มเติม");
    verifyNoInteractions(events);
  }

  @Test void replyToReplyKeepsRootAndTracksActualTarget() {
    setup(actor); PostComment root = comment(5, writer), child = comment(6, owner); child.parent = root; child.replyTo = root;
    when(comments.lockActive(5L)).thenReturn(Optional.of(root));
    when(comments.lockActive(6L)).thenReturn(Optional.of(child));
    var result = service.add(actor.getEmail(), 10, "ตอบอีกครั้ง", 6L);
    assertEquals(5L, result.parentCommentId()); assertEquals(6L, result.replyToCommentId());
  }

  @Test void deletedOrForeignTargetsAndOverlongBodiesAreRejectedBeforeSave() {
    setup(actor); PostComment foreign = comment(5, writer); foreign.post = post(11, owner);
    when(comments.lockActive(5L)).thenReturn(Optional.of(foreign));
    assertEquals(400, assertThrows(Problem.class, () -> service.add(actor.getEmail(), 10, "ตอบ", 5L)).status);
    PostComment child = comment(6, writer); child.parent = comment(7, owner);
    when(comments.lockActive(6L)).thenReturn(Optional.of(child));
    when(comments.lockActive(7L)).thenReturn(Optional.empty());
    assertEquals(400, assertThrows(Problem.class, () -> service.add(actor.getEmail(), 10, "ตอบ", 6L)).status);
    assertEquals(404, assertThrows(Problem.class, () -> service.add(actor.getEmail(), 10, "ตอบ", 8L)).status);
    assertThrows(Problem.class, () -> service.add(actor.getEmail(), 10, " "));
    assertThrows(Problem.class, () -> service.add(actor.getEmail(), 10, "a".repeat(801)));
    verify(comments, never()).save(any()); verifyNoInteractions(events);
  }

  @Test void rootDeletionIsSoftAndRepliesRetainContextWithoutDeletedAuthorsName() {
    setup(owner); PostComment root = comment(5, writer), child = comment(6, actor); child.parent = root; child.replyTo = root;
    when(comments.lockActive(5L)).thenReturn(Optional.of(root));
    service.remove(owner.getEmail(), 5);
    assertNotNull(root.deletedAt); assertNull(child.deletedAt);
    when(posts.existsById(10L)).thenReturn(true);
    when(comments.findByPostIdAndDeletedAtIsNullOrderByCreatedAtAscIdAsc(eq(10L), any())).thenReturn(new PageImpl<>(List.of(child)));
    var view = service.list(10, owner.getEmail(), 0).items().get(0);
    assertTrue(view.parentDeleted()); assertNull(view.replyToAuthorName()); assertTrue(view.canDelete());
    verify(comments, never()).delete(any());
  }

  @Test void deletionPermissionsRemainAuthorOwnerOrAdmin() {
    setup(actor); PostComment c = comment(5, writer); when(comments.lockActive(5L)).thenReturn(Optional.of(c));
    assertEquals(403, assertThrows(Problem.class, () -> service.remove(actor.getEmail(), 5)).status);
    actor.setRole(UserRole.ADMIN); assertDoesNotThrow(() -> service.remove(actor.getEmail(), 5));
    assertEquals(actor, c.deletedBy);
  }
}
