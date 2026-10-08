package com.kku.foodshare.service.impl;

import com.kku.foodshare.domain.entity.*;
import com.kku.foodshare.domain.enums.UserRole;
import com.kku.foodshare.dto.response.PageView;
import com.kku.foodshare.exception.Problem;
import com.kku.foodshare.repository.*;
import com.kku.foodshare.service.*;
import java.time.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {
  private final PostCommentRepository comments; private final FoodPostRepository posts; private final MemberService members; private final Clock clock; private final ApplicationEventPublisher events;
  public CommentServiceImpl(PostCommentRepository comments, FoodPostRepository posts, MemberService members, Clock clock, ApplicationEventPublisher events) { this.comments=comments; this.posts=posts; this.members=members; this.clock=clock; this.events=events; }
  private View view(PostComment c, User actor) {
    boolean can = actor != null && (actor.getId().equals(c.author.getId()) || actor.getId().equals(c.post.getOwner().getId()) || actor.getRole()==UserRole.ADMIN);
    return new View(c.id,c.post.getId(),c.author.getId(),c.author.getDisplayName(),c.body,c.createdAt,can,
        c.parent == null ? null : c.parent.getId(), c.replyTo == null ? null : c.replyTo.getId(),
        c.replyTo == null || c.replyTo.getDeletedAt() != null ? null : c.replyTo.getAuthor().getDisplayName(),
        c.parent != null && c.parent.getDeletedAt() != null,
        actor != null && actor.getId().equals(c.author.getId()));
  }
  @Transactional(readOnly=true) public PageView<View> list(long postId, String email, int page) { User actor=email==null?null:members.require(email); if (!posts.existsById(postId)) throw Problem.missing(); return PageView.of(comments.findByPostIdAndDeletedAtIsNullOrderByCreatedAtAscIdAsc(postId, PageRequest.of(Math.max(0,page),30)).map(c->view(c,actor))); }
  @Transactional(readOnly=true)
  public View get(String email, long id) {
    User actor = members.require(email);
    return view(comments.findByIdAndDeletedAtIsNull(id).orElseThrow(Problem::missing), actor);
  }
  private String text(String body) {
    String text = body == null ? "" : body.trim().replaceAll("\\s+", " ");
    if (text.isBlank() || text.length() > 800) throw new Problem(400, "ความคิดเห็นต้องมีความยาว 1–800 ตัวอักษร");
    return text;
  }
  public View add(String email, long postId, String body) { return add(email, postId, body, null); }
  public View add(String email, long postId, String body, Long parentCommentId) {
    User user = members.require(email);
    FoodPost post = posts.findById(postId).orElseThrow(Problem::missing);
    String text = text(body);
    PostComment target = null, root = null;
    if (parentCommentId != null) {
      if (parentCommentId < 1) throw new Problem(400, "ความคิดเห็นต้นทางไม่ถูกต้อง");
      target = comments.lockActive(parentCommentId).orElseThrow(Problem::missing);
      if (!target.post.getId().equals(postId)) throw new Problem(400, "ตอบกลับได้เฉพาะความคิดเห็นในโพสต์เดียวกัน");
      root = target.parent == null ? target : comments.lockActive(target.parent.getId())
          .orElseThrow(() -> new Problem(400, "ความคิดเห็นต้นทางถูกลบแล้ว"));
    }
    PostComment c = new PostComment();
    c.post = post; c.author = user; c.body = text; c.createdAt = LocalDateTime.now(clock);
    c.parent = root; c.replyTo = target;
    comments.save(c);
    String href = "/posts/" + postId + "#post-comments";
    if (!post.getOwner().getId().equals(user.getId())) {
      events.publishEvent(new com.kku.foodshare.service.event.ActivityNotice(post.getOwner(), user,
          target == null ? "มีความคิดเห็นใหม่ในโพสต์ของคุณ" : "มีการตอบกลับความคิดเห็นในโพสต์ของคุณ",
          "ในโพสต์ “" + post.getTitle() + "”: " + text, href));
    }
    if (target != null && !target.author.getId().equals(user.getId()) && !target.author.getId().equals(post.getOwner().getId())) {
      events.publishEvent(new com.kku.foodshare.service.event.ActivityNotice(target.author, user, "มีคนตอบกลับความคิดเห็นของคุณ",
          "ในโพสต์ “" + post.getTitle() + "”: " + text, href));
    }
    return view(c, user);
  }
  public View update(String email, long id, String body) {
    User user = members.require(email);
    PostComment c = comments.lockActive(id).orElseThrow(Problem::missing);
    if (!user.getId().equals(c.author.getId())) throw Problem.forbidden();
    c.body = text(body);
    return view(c, user);
  }
  public void remove(String email,long id) { User user=members.require(email); PostComment c=comments.lockActive(id).orElseThrow(Problem::missing); if(!user.getId().equals(c.author.getId())&&!user.getId().equals(c.post.getOwner().getId())&&user.getRole()!=UserRole.ADMIN) throw Problem.forbidden(); c.deletedAt=LocalDateTime.now(clock);c.deletedBy=user; }
}
