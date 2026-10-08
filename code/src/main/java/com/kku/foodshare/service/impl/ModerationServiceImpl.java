package com.kku.foodshare.service.impl;

import com.kku.foodshare.domain.entity.*;
import com.kku.foodshare.dto.response.PageView;
import com.kku.foodshare.exception.Problem;
import com.kku.foodshare.repository.*;
import com.kku.foodshare.service.*;
import com.kku.foodshare.service.event.*;
import java.time.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ModerationServiceImpl implements ModerationService {
  private final ReportRepository reports;
  private final AuditEventRepository audits;
  private final FoodPostRepository posts;
  private final UserRepository users;
  private final PostCommentRepository comments;
  private final MemberService members;
  private final ApplicationEventPublisher events;
  private final Clock clock;

  public ModerationServiceImpl(
      ReportRepository reports,
      AuditEventRepository audits,
      FoodPostRepository posts,
      UserRepository users, PostCommentRepository comments,
      MemberService members,
      ApplicationEventPublisher events,
      Clock clock) {
    this.reports = reports;
    this.audits = audits;
    this.posts = posts;
    this.users = users;
    this.comments = comments;
    this.members = members;
    this.events = events;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public long pendingReports(String email) { members.admin(email); return reports.countByStatus("OPEN"); }

  @Transactional(readOnly = true)
  public PageView<PostView> posts(String email, int page, String query, String status) {
    members.admin(email);
    FoodPostStatus filter = null;
    if (status != null && !status.isBlank()) {
      try { filter = FoodPostStatus.valueOf(status); }
      catch (IllegalArgumentException invalid) { throw new Problem(400, "สถานะโพสต์ไม่ถูกต้อง"); }
    }
    final FoodPostStatus selected = filter;
    String term = query == null ? "" : query.trim().toLowerCase(java.util.Locale.ROOT);
    if (term.length() > 120) throw new Problem(400, "คำค้นหาต้องไม่เกิน 120 ตัวอักษร");
    String like = "%" + term.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    org.springframework.data.jpa.domain.Specification<FoodPost> criteria = (root, q, cb) -> {
      var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
      if (selected != null) predicates.add(cb.equal(root.get("status"), selected));
      if (!term.isEmpty()) predicates.add(cb.or(
          cb.like(cb.lower(root.get("title")), like, '!'),
          cb.like(cb.lower(root.get("owner").get("displayName")), like, '!'),
          cb.like(cb.lower(root.get("pickupLocationName")), like, '!')));
      return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
    };
    return PageView.of(posts.findAll(criteria, PageRequest.of(Math.max(0, page), 20,
        Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))))
        .map(p -> new PostView(p.getId(), p.getTitle(), p.getOwner().getDisplayName(),
            p.getStatus().name(), p.getAvailableQuantity(), p.getReservedQuantity(),
            p.getUnit(), p.getPickupLocationName(), p.getAvailableUntil())));
  }

  private ReportView view(Report r) {
    return new ReportView(
        r.id,
        r.post.getId(),
        r.commentId,
        r.post.getTitle(),
        r.reporter.getDisplayName(),
        r.reason,
        r.status,
        r.resolution,
        r.createdAt);
  }

  private void reason(String s) {
    if (s == null || s.isBlank() || s.length() > 1000)
      throw new Problem(400, "กรุณาระบุเหตุผลไม่เกิน 1,000 ตัวอักษร");
  }

  public ReportView report(String email, long id, Long commentId, String reason) {
    User u = members.require(email);
    reason(reason);
    FoodPost p = posts.lockById(id).orElseThrow(Problem::missing);
    if (p.getStatus() == FoodPostStatus.CANCELLED) throw Problem.missing();
    if (commentId == null && reports.existsByPostIdAndReporterIdAndStatus(id, u.getId(), "OPEN"))
      throw Problem.conflict("คุณรายงานรายการนี้แล้ว ผู้ดูแลกำลังตรวจสอบ");
    if (commentId != null) {
      var comment = comments.findByIdAndDeletedAtIsNull(commentId).orElseThrow(Problem::missing);
      if (!comment.post.getId().equals(id)) throw Problem.missing();
      if (reports.existsByCommentIdAndReporterIdAndStatus(commentId, u.getId(), "OPEN")) throw Problem.conflict("คุณรายงานความคิดเห็นนี้แล้ว ผู้ดูแลกำลังตรวจสอบ");
    }
    Report r = new Report();
    r.post = p;
    r.commentId = commentId;
    r.reporter = u;
    r.reason = reason.trim();
    r.status = "OPEN";
    r.createdAt = LocalDateTime.now(clock);
    reports.save(r);
    return view(r);
  }

  @Transactional(readOnly = true)
  public PageView<ReportView> reports(String email, int page) {
    members.admin(email);
    return PageView.of(
        reports
            .findAllByOrderByCreatedAtDesc(PageRequest.of(Math.max(0, page), 20))
            .map(this::view));
  }

  private void audit(User actor, String action, String type, long id, String reason) {
    AuditEvent e = new AuditEvent();
    e.actor = actor;
    e.action = action;
    e.targetType = type;
    e.targetId = id;
    e.reason = reason;
    e.createdAt = LocalDateTime.now(clock);
    audits.save(e);
  }

  public ReportView resolve(String email, long id, String reason, boolean closePost) {
    User admin = members.admin(email);
    reason(reason);
    Report r = reports.findById(id).orElseThrow(Problem::missing);
    FoodPost p = posts.lockById(r.post.getId()).orElseThrow(Problem::missing);
    if (!r.status.equals("OPEN")) throw Problem.conflict("รายงานนี้ถูกจัดการแล้ว");
    if (closePost) {
      p.setStatus(FoodPostStatus.CANCELLED);
      events.publishEvent(new PostClosed(p, "ผู้ดูแลปิดโพสต์"));
    }
    r.status = "RESOLVED";
    r.resolution = reason;
    r.reviewer = admin;
    r.resolvedAt = LocalDateTime.now(clock);
    audit(admin, closePost ? "CLOSE_POST" : "RESOLVE_REPORT", "REPORT", id, reason);
    return view(r);
  }

  @Transactional(readOnly = true)
  public PageView<UserView> users(String email, int page) {
    members.admin(email);
    return PageView.of(
        users
            .findAll(PageRequest.of(Math.max(0, page), 20, Sort.by("id")))
            .map(
                u ->
                    new UserView(
                        u.getId(),
                        u.getDisplayName(),
                        u.getEmail(),
                        Boolean.TRUE.equals(u.getActive()),
                        u.getRole().name())));
  }

  public void active(String email, long userId, boolean active, String reason) {
    User admin = members.admin(email);
    reason(reason);
    if (admin.getId().equals(userId))
      throw Problem.conflict("ไม่สามารถระงับบัญชีผู้ดูแลที่กำลังใช้งาน");
    User u = users.findById(userId).orElseThrow(Problem::missing);
    u.setActive(active);
    if (!active) {
      var owned = posts.findOwnedIds(userId);
      for (Long id : owned) {
        FoodPost p = posts.lockById(id).orElseThrow();
        if (p.getStatus() == FoodPostStatus.AVAILABLE
            || p.getStatus() == FoodPostStatus.LOW_STOCK) {
          p.setStatus(FoodPostStatus.CANCELLED);
          events.publishEvent(new PostClosed(p, "บัญชีเจ้าของโพสต์ถูกระงับ"));
        }
      }
    }
    audit(admin, active ? "ACTIVATE_USER" : "SUSPEND_USER", "USER", userId, reason);
  }
}
