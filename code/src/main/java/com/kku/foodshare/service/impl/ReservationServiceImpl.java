package com.kku.foodshare.service.impl;

import com.kku.foodshare.domain.entity.*;
import com.kku.foodshare.dto.response.*;
import com.kku.foodshare.exception.*;
import com.kku.foodshare.mapper.PostViewMapper;
import com.kku.foodshare.repository.*;
import com.kku.foodshare.service.*;
import com.kku.foodshare.service.event.*;
import java.time.*;
import java.util.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {
  private final ReservationRepository repo;
  private final FoodPostRepository posts;
  private final MemberService members;
  private final PickupCodeService codes;
  private final PostViewMapper mapper;
  private final Clock clock;
  private final ApplicationEventPublisher events;

  public ReservationServiceImpl(
      ReservationRepository repo,
      FoodPostRepository posts,
      MemberService members,
      PickupCodeService codes,
      PostViewMapper mapper,
      Clock clock,
      ApplicationEventPublisher events) {
    this.repo = repo;
    this.posts = posts;
    this.members = members;
    this.codes = codes;
    this.mapper = mapper;
    this.clock = clock;
    this.events = events;
  }

  private LocalDateTime now() {
    return LocalDateTime.now(clock);
  }

  private ReservationView view(Reservation r, User actor) {
    boolean owner = r.post.getOwner().getId().equals(actor.getId());
    String state = r.status.name();
    if (r.status == ReservationStatus.RESERVED && !r.post.getAvailableUntil().isAfter(now()))
      state = "EXPIRED";
    String code = !owner && state.equals("RESERVED") ? codes.decrypt(r.pickupEncrypted) : null;
    return new ReservationView(
        r.id,
        mapper.map(r.post, actor.getEmail(), null, null),
        r.quantity,
        state,
        r.member.getDisplayName(),
        r.member.getId(),
        code,
        owner,
        r.createdAt);
  }

  private Reservation locked(long id) {
    long pid = repo.findPostId(id).orElseThrow(Problem::missing);
    posts.lockById(pid).orElseThrow(Problem::missing);
    return repo.findById(id).orElseThrow(Problem::missing);
  }

  private void participant(Reservation r, User u) {
    if (!r.member.getId().equals(u.getId()) && !r.post.getOwner().getId().equals(u.getId()))
      throw Problem.forbidden();
  }

  private void open(FoodPost p) {
    if ((p.getStatus() != FoodPostStatus.AVAILABLE && p.getStatus() != FoodPostStatus.LOW_STOCK)
        || !p.getAvailableUntil().isAfter(now())
        || !Boolean.TRUE.equals(p.getOwner().getActive()))
      throw Problem.conflict("อาหารรายการนี้ปิดรับจองแล้ว");
  }

  private void quantity(int q) {
    if (q < 1 || q > 10000) throw new Problem(400, "จำนวนต้องอยู่ระหว่าง 1 ถึง 10,000");
  }

  private void withinPerPersonLimit(FoodPost post, int quantity) {
    Integer limit = post.getMaxPerPerson();
    if (limit != null && quantity > limit)
      throw Problem.conflict("โพสต์นี้จำกัดการจองไม่เกิน " + limit + " " + post.getUnit() + " ต่อคน");
  }

  private void notice(User u, String title, String message, String href) {
    events.publishEvent(new ActivityNotice(u, title, message, href));
  }

  public ReservationView reserve(String email, long postId, int quantity, String key) {
    User u = members.require(email);
    quantity(quantity);
    if (key == null || !key.matches("[a-zA-Z0-9-]{16,64}"))
      throw new Problem(400, "รหัสคำขอไม่ถูกต้อง กรุณาโหลดหน้าใหม่");
    FoodPost p = posts.lockById(postId).orElseThrow(Problem::missing);
    var previous = repo.findByMemberIdAndRequestKey(u.getId(), key);
    if (previous.isPresent()) {
      Reservation r = previous.get();
      if (!r.post.getId().equals(postId) || r.quantity != quantity)
        throw Problem.conflict("รหัสคำขอนี้ถูกใช้กับข้อมูลอื่นแล้ว");
      return view(r, u);
    }
    if (p.getOwner().getId().equals(u.getId())) throw Problem.forbidden();
    open(p);
    withinPerPersonLimit(p, quantity);
    if (repo.existsByPostIdAndMemberIdAndStatus(postId, u.getId(), ReservationStatus.RESERVED))
      throw Problem.conflict("คุณมีการจองรายการนี้แล้ว กรุณาแก้จำนวนในการจองเดิม");
    if (quantity > p.getAvailableQuantity())
      throw Problem.conflict("จำนวนอาหารไม่พอ กรุณาเลือกจำนวนใหม่");
    Reservation r = new Reservation();
    r.post = p;
    r.member = u;
    r.quantity = quantity;
    r.status = ReservationStatus.RESERVED;
    r.requestKey = key;
    r.createdAt = now();
    r.updatedAt = now();
    String code = codes.generate();
    r.pickupHash = codes.hash(code);
    r.pickupEncrypted = codes.encrypt(code);
    p.setReservedQuantity(p.getReservedQuantity() + quantity);
    repo.saveAndFlush(r);
    notice(
        p.getOwner(),
        "มีเพื่อนจองอาหารแล้ว",
        u.getDisplayName() + " จอง " + quantity + " " + p.getUnit(),
        "/account/posts");
    notice(
        u, "จองอาหารสำเร็จ", p.getTitle() + " • " + quantity + " " + p.getUnit(), "/reservations");
    return view(r, u);
  }

  @Transactional(readOnly = true)
  public ReservationView get(String email, long id) {
    User u = members.require(email);
    Reservation r = repo.findById(id).orElseThrow(Problem::missing);
    participant(r, u);
    return view(r, u);
  }

  @Transactional(readOnly = true)
  public ReservationView mineForPost(String email, long postId) {
    User u = members.require(email);
    return repo.findByPostIdAndMemberIdAndStatus(postId, u.getId(), ReservationStatus.RESERVED)
        .map(r -> view(r, u)).orElse(null);
  }

  public ReservationView changeQuantity(String email, long id, int quantity) {
    User u = members.require(email);
    Reservation r = locked(id);
    if (!r.member.getId().equals(u.getId())) throw Problem.forbidden();
    r.status.requireMutable();
    open(r.post);
    quantity(quantity);
    withinPerPersonLimit(r.post, quantity);
    int delta = quantity - r.quantity;
    if (delta > r.post.getAvailableQuantity()) throw Problem.conflict("จำนวนอาหารไม่พอ");
    r.post.setReservedQuantity(r.post.getReservedQuantity() + delta);
    r.quantity = quantity;
    r.updatedAt = now();
    notice(
        r.post.getOwner(),
        "มีการแก้ไขจำนวนจอง",
        r.post.getTitle() + " • " + quantity + " " + r.post.getUnit(),
        "/account/posts");
    return view(r, u);
  }

  public void cancel(String email, long id) {
    User u = members.require(email);
    Reservation r = locked(id);
    participant(r, u);
    if (r.status == ReservationStatus.CANCELLED || r.status == ReservationStatus.EXPIRED) return;
    r.status.requireMutable();
    finish(
        r,
        r.post.getAvailableUntil().isAfter(now())
            ? ReservationStatus.CANCELLED
            : ReservationStatus.EXPIRED,
        "การจองถูกยกเลิก");
  }

  private void finish(Reservation r, ReservationStatus target, String reason) {
    if (r.status != ReservationStatus.RESERVED) return;
    r.post.setReservedQuantity(r.post.getReservedQuantity() - r.quantity);
    r.status = target;
    r.updatedAt = now();
    notice(r.member, reason, r.post.getTitle(), "/reservations");
    notice(r.post.getOwner(), reason, r.post.getTitle(), "/account/posts");
  }

  @Transactional(noRollbackFor = InvalidPickupCode.class)
  public ReservationView collect(String email, long id, String code) {
    User u = members.require(email);
    Reservation r = locked(id);
    if (!r.post.getOwner().getId().equals(u.getId())) throw Problem.forbidden();
    if (r.status == ReservationStatus.COLLECTED) return view(r, u);
    r.status.requireMutable();
    open(r.post);
    if (r.post.getAvailableFrom().isAfter(now())) throw Problem.conflict("ยังไม่ถึงเวลารับอาหาร");
    if (r.lockedUntil != null && r.lockedUntil.isAfter(now()))
      throw new InvalidPickupCode(429, "ลองรหัสผิดหลายครั้ง กรุณารอ 15 นาที");
    if (r.lockedUntil != null) {
      r.failedAttempts = 0;
      r.lockedUntil = null;
    }
    if (code == null || !code.matches("[0-9]{6}") || !codes.matches(code, r.pickupHash)) {
      r.failedAttempts++;
      if (r.failedAttempts >= 5) r.lockedUntil = now().plusMinutes(15);
      throw new InvalidPickupCode(400, "รหัสรับอาหารไม่ถูกต้อง");
    }
    r.post.setReservedQuantity(r.post.getReservedQuantity() - r.quantity);
    r.post.setCollectedQuantity(r.post.getCollectedQuantity() + r.quantity);
    if (r.post.getCollectedQuantity() + r.post.getOfflineQuantity() == r.post.getQuantity())
      r.post.setStatus(FoodPostStatus.CLAIMED);
    r.status = ReservationStatus.COLLECTED;
    r.updatedAt = now();
    notice(
        r.member,
        "รับอาหารเรียบร้อยแล้ว",
        "ขอบคุณที่เป็นส่วนหนึ่งของการแบ่งปัน • " + r.post.getTitle(),
        "/reservations");
    return view(r, u);
  }

  @Transactional(readOnly = true)
  public PageView<ReservationView> mine(String email, int page) {
    User u = members.require(email);
    return PageView.of(
        repo.findByMemberIdOrderByCreatedAtDesc(u.getId(), PageRequest.of(Math.max(0, page), 12))
            .map(r -> view(r, u)));
  }

  @Transactional(readOnly = true)
  public List<ReservationView> forPost(String email, long postId) {
    User u = members.require(email);
    FoodPost p = posts.findById(postId).orElseThrow(Problem::missing);
    if (!p.getOwner().getId().equals(u.getId())) throw Problem.forbidden();
    return repo.findByPostIdOrderByCreatedAtDesc(postId).stream().map(r -> view(r, u)).toList();
  }

  @EventListener
  public void closed(PostClosed event) {
    for (Reservation r :
        repo.findByPostIdAndStatus(event.post().getId(), ReservationStatus.RESERVED))
      finish(r, ReservationStatus.CANCELLED, event.reason());
  }

  public void expire(long postId) {
    FoodPost p = posts.lockById(postId).orElseThrow(Problem::missing);
    if (p.getAvailableUntil().isAfter(now())
        || p.getStatus() == FoodPostStatus.CANCELLED
        || p.getStatus() == FoodPostStatus.CLAIMED) return;
    for (Reservation r : repo.findByPostIdAndStatus(postId, ReservationStatus.RESERVED))
      finish(r, ReservationStatus.EXPIRED, "หมดเวลารับอาหารแล้ว");
    p.setStatus(FoodPostStatus.EXPIRED);
  }
}
