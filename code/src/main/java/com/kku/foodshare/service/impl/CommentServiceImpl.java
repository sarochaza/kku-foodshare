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
  private View view(PostComment c, User actor) { boolean can = actor != null && (actor.getId().equals(c.author.getId()) || actor.getId().equals(c.post.getOwner().getId()) || actor.getRole()==UserRole.ADMIN); return new View(c.id,c.post.getId(),c.author.getId(),c.author.getDisplayName(),c.body,c.createdAt,can); }
  @Transactional(readOnly=true) public PageView<View> list(long postId, String email, int page) { User actor=email==null?null:members.require(email); if (!posts.existsById(postId)) throw Problem.missing(); return PageView.of(comments.findByPostIdAndDeletedAtIsNullOrderByCreatedAtAsc(postId, PageRequest.of(Math.max(0,page),30)).map(c->view(c,actor))); }
  public View add(String email,long postId,String body) { User user=members.require(email); FoodPost post=posts.findById(postId).orElseThrow(Problem::missing); String text=body==null?"":body.trim().replaceAll("\\s+", " "); if(text.isBlank()||text.length()>800) throw new Problem(400,"ความคิดเห็นต้องมีความยาว 1–800 ตัวอักษร"); PostComment c=new PostComment(); c.post=post;c.author=user;c.body=text;c.createdAt=LocalDateTime.now(clock);comments.save(c); if (!post.getOwner().getId().equals(user.getId())) events.publishEvent(new com.kku.foodshare.service.event.ActivityNotice(post.getOwner(), user, "มีความคิดเห็นใหม่ในโพสต์ของคุณ", "แสดงความคิดเห็นว่า: "+text, "/posts/"+postId)); return view(c,user); }
  public void remove(String email,long id) { User user=members.require(email); PostComment c=comments.findByIdAndDeletedAtIsNull(id).orElseThrow(Problem::missing); if(!user.getId().equals(c.author.getId())&&!user.getId().equals(c.post.getOwner().getId())&&user.getRole()!=UserRole.ADMIN) throw Problem.forbidden(); c.deletedAt=LocalDateTime.now(clock);c.deletedBy=user; }
}
