package com.kku.foodshare.service.impl;

import com.kku.foodshare.domain.entity.*;
import com.kku.foodshare.dto.request.*;
import com.kku.foodshare.dto.response.*;
import com.kku.foodshare.exception.Problem;
import com.kku.foodshare.service.PostViewService;
import com.kku.foodshare.repository.*;
import com.kku.foodshare.service.*;
import com.kku.foodshare.service.discovery.*;
import com.kku.foodshare.service.event.PostClosed;
import com.kku.foodshare.service.storage.ImageStorage;
import java.time.*;
import java.util.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class FoodCatalogServiceImpl implements FoodCatalogService {
  private final FoodPostRepository posts;
  private final MemberService members;
  private final PostViewService mapper;
  private final Clock clock;
  private final List<FoodDiscoveryStrategy> strategies;
  private final ApplicationEventPublisher events;
  private final FoodPostImageRepository images;
  private final ImageStorage storage;
  private final ReservationRepository reservations;
  private final ReservationService reservationService;
  private final NotificationService notifications;

  public FoodCatalogServiceImpl(
      FoodPostRepository posts,
      MemberService members,
      PostViewService mapper,
      Clock clock,
      List<FoodDiscoveryStrategy> strategies,
      ApplicationEventPublisher events,
      FoodPostImageRepository images,
      ImageStorage storage,
      ReservationRepository reservations,
      ReservationService reservationService, NotificationService notifications) {
    this.posts = posts;
    this.members = members;
    this.mapper = mapper;
    this.clock = clock;
    this.strategies = strategies;
    this.events = events;
    this.images = images;
    this.storage = storage;
    this.reservations = reservations;
    this.reservationService = reservationService;
    this.notifications = notifications;
  }

  private LocalDateTime now() {
    return LocalDateTime.now(clock);
  }

  private void fields(FoodPost p, FoodPostRequest r) {
    if (!r.availableUntil().isAfter(r.availableFrom()) || !r.availableUntil().isAfter(now()))
      throw new Problem(400, "เวลาสิ้นสุดต้องอยู่หลังเวลาเริ่มและเวลาปัจจุบัน");
    p.setTitle(r.title().trim());
    p.setDescription(r.description().trim());
    p.setCategory(r.category());
    p.setQuantity(r.quantity());
    if (r.maxPerPerson() != null && r.maxPerPerson() > r.quantity())
      throw new Problem(400, "จำกัดต่อคนต้องไม่เกินจำนวนทั้งหมด");
    p.setMaxPerPerson(r.maxPerPerson());
    p.setUnit(r.unit().trim());
    p.setPickupLocationName(r.pickupLocationName().trim());
    p.setLatitude(r.latitude());
    p.setLongitude(r.longitude());
    p.setAvailableFrom(r.availableFrom());
    p.setAvailableUntil(r.availableUntil());
    p.setAllergens(r.allergens() == null ? "" : r.allergens().trim());
    p.setUpdatedAt(now());
  }

  private FoodPost owned(String email, long id) {
    User u = members.require(email);
    FoodPost p = posts.lockById(id).orElseThrow(Problem::missing);
    if (!p.getOwner().getId().equals(u.getId())) throw Problem.forbidden();
    return p;
  }

  public PostView create(String email, FoodPostRequest r) {
    FoodPost p = new FoodPost();
    p.setOwner(members.require(email));
    fields(p, r);
    p.setStatus(FoodPostStatus.AVAILABLE);
    p.setCreatedAt(now());
    posts.saveAndFlush(p);
    notifications.notifyInterested(p);
    return mapper.map(p, email, null, null);
  }

  public PostView update(String email, long id, FoodPostRequest r) {
    FoodPost p = owned(email, id);
    if (p.getStatus() == FoodPostStatus.CANCELLED || !p.getAvailableUntil().isAfter(now()))
      throw Problem.conflict("โพสต์นี้ปิดรับแล้ว");
    if (r.quantity() < p.getReservedQuantity() + p.getCollectedQuantity() + p.getOfflineQuantity())
      throw Problem.conflict("จำนวนต้องไม่น้อยกว่ายอดจอง รับผ่านเว็บ และแจกนอกเว็บรวมกัน");
    if (r.maxPerPerson() != null
        && r.maxPerPerson()
            < reservations.maxQuantityByPostIdAndStatus(id, ReservationStatus.RESERVED))
      throw Problem.conflict("จำกัดต่อคนต่ำกว่าจำนวนที่ผู้จองไว้ไม่ได้");
    if (p.getReservedQuantity() > 0
        && (!p.getAvailableFrom().equals(r.availableFrom())
            || !p.getAvailableUntil().equals(r.availableUntil())
            || p.getLatitude().compareTo(r.latitude()) != 0
            || p.getLongitude().compareTo(r.longitude()) != 0
            || !p.getPickupLocationName().equals(r.pickupLocationName())))
      throw Problem.conflict(
          "มีผู้จองแล้ว หากต้องเปลี่ยนสถานที่หรือเวลา กรุณาปิดโพสต์และสร้างใหม่");
    fields(p, r);
    p.setStatus(p.getQuantity() > p.getCollectedQuantity() + p.getOfflineQuantity()
        ? FoodPostStatus.AVAILABLE : FoodPostStatus.CLAIMED);
    return mapper.map(p, email, null, null);
  }

  public PostView extend(String email, long id, ExtendPostRequest r) {
    User owner = members.require(email);
    FoodPost before = posts.findById(id).orElseThrow(Problem::missing);
    if (!before.getOwner().getId().equals(owner.getId())) throw Problem.forbidden();
    if (before.getStatus() == FoodPostStatus.CANCELLED)
      throw Problem.conflict("โพสต์นี้ถูกปิดแล้ว");
    if (before.getAvailableUntil().isAfter(now()))
      throw Problem.conflict("โพสต์นี้ยังไม่หมดเวลารับ");
    if (!r.availableUntil().isAfter(now()))
      throw new Problem(400, "เวลาใหม่ต้องอยู่หลังเวลาปัจจุบัน");

    // Expire any old reservations first, so they can never be revived with the new time.
    reservationService.expire(id);
    FoodPost p = posts.lockById(id).orElseThrow(Problem::missing);
    if (p.getAvailableQuantity() < 1)
      throw Problem.conflict("ไม่มีอาหารเหลือให้เปิดรับต่อ");
    p.setAvailableUntil(r.availableUntil());
    p.setStatus(FoodPostStatus.AVAILABLE);
    p.setUpdatedAt(now());
    return mapper.map(p, email, null, null);
  }

  @Transactional(readOnly = true)
  public PostView get(long id, String email) {
    FoodPost p = posts.findById(id).orElseThrow(Problem::missing);
    if (p.getStatus() == FoodPostStatus.CANCELLED
        && (email == null || (!p.getOwner().getEmail().equalsIgnoreCase(email)
            && members.require(email).getRole() != com.kku.foodshare.domain.enums.UserRole.ADMIN)))
      throw Problem.missing();
    return mapper.map(p, email, null, null);
  }

  public void delete(String email, long id) {
    FoodPost p = owned(email, id);
    if (p.getStatus() != FoodPostStatus.CANCELLED) {
      p.setStatus(FoodPostStatus.CANCELLED);
      p.setUpdatedAt(now());
      events.publishEvent(new PostClosed(p, "เจ้าของปิดโพสต์"));
    }
  }

  public PostView image(String email, long id, MultipartFile file) {
    FoodPost p = owned(email, id);
    if (p.getStatus() == FoodPostStatus.CANCELLED) throw Problem.conflict("โพสต์นี้ปิดแล้ว");
    if (images.countByPostId(id) >= 5) throw Problem.conflict("เพิ่มรูปได้สูงสุด 5 รูปต่อโพสต์");
    String name = storage.store(file);
    FoodPostImage i = new FoodPostImage();
    i.post = p;
    i.filename = name;
    i.sortOrder = (int) images.countByPostId(id);
    images.save(i);
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          public void afterCompletion(int status) {
            if (status != STATUS_COMMITTED) storage.remove(name);
          }
        });
    return mapper.map(p, email, null, null);
  }

  public void removeImage(String email, long id, long imageId) {
    owned(email, id);
    FoodPostImage image = images.findByIdAndPostId(imageId, id).orElseThrow(Problem::missing);
    String filename = image.filename;
    images.delete(image);
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      public void afterCompletion(int status) { if (status == STATUS_COMMITTED) storage.remove(filename); }
    });
  }

  @Transactional(readOnly = true)
  public PageView<PostView> mine(String email, int page) {
    User u = members.require(email);
    return PageView.of(
        posts
            .findByOwnerIdOrderByCreatedAtDesc(u.getId(), PageRequest.of(Math.max(0, page), 12))
            .map(p -> mapper.map(p, email, null, null)));
  }

  @Transactional(readOnly = true)
  public PageView<PostView> ownerPosts(long ownerId, String email, int page) {
    return PageView.of(posts.findByOwnerIdAndStatusNotOrderByCreatedAtDesc(ownerId, FoodPostStatus.CANCELLED, PageRequest.of(Math.max(0, page), 12))
        .map(p -> mapper.map(p, email, null, null)));
  }

  @Transactional(readOnly = true)
  public Map<String, Long> managementSummary(String email) {
    User user = members.require(email);
    var owned = posts.findByOwnerIdOrderByCreatedAtDesc(user.getId());
    long open =
        owned.stream()
            .map(post -> mapper.map(post, email, null, null).status())
            .filter(state -> Set.of("AVAILABLE", "LOW_STOCK", "SCHEDULED", "FULL").contains(state))
            .count();
    long waiting = owned.stream().mapToLong(FoodPost::getReservedQuantity).sum();
    long collected = owned.stream().mapToLong(FoodPost::getCollectedQuantity).sum();
    return Map.of(
        "totalPosts", (long) owned.size(),
        "openPosts", open,
        "waitingCount", waiting,
        "collectedCount", collected,
        "offlineCount", owned.stream().mapToLong(FoodPost::getOfflineQuantity).sum());
  }

  @Transactional(readOnly = true)
  public PageView<PostView> search(
      String text,
      String category,
      String sort,
      Double lat,
      Double lng,
      boolean availableNow,
      int page,
      int size,
      String email,
      String ownership) {
    if (page < 0 || page > 10000 || size < 1 || size > 200)
      throw new Problem(400, "ขนาดหน้าข้อมูลไม่ถูกต้อง");
    if ((lat == null) != (lng == null)
        || lat != null
            && (!Double.isFinite(lat)
                || !Double.isFinite(lng)
                || Math.abs(lat) > 90
                || Math.abs(lng) > 180)) throw new Problem(400, "พิกัดไม่ถูกต้อง");
    if (text != null && text.length() > 100) throw new Problem(400, "คำค้นยาวเกินไป");
    FoodCategory cat =
        category == null || category.isBlank() ? null : FoodCategory.valueOf(category);
    FoodDiscoveryStrategy strategy =
        strategies.stream()
            .filter(s -> s.key().equals(sort))
            .findFirst()
            .orElseThrow(() -> new Problem(400, "รูปแบบการเรียงไม่ถูกต้อง"));
    if (ownership == null) ownership = "";
    if (!Set.of("", "mine", "others").contains(ownership)) throw new Problem(400, "ตัวกรองเจ้าของโพสต์ไม่ถูกต้อง");
    User ownerFilter = ownership.equals("mine") ? members.require(email) : email == null ? null : members.require(email);
    String ownerScope = ownership;
    Specification<FoodPost> spec =
        (r, q, b) -> {
          var list = new ArrayList<jakarta.persistence.criteria.Predicate>();
          list.add(r.get("status").in(FoodPostStatus.AVAILABLE, FoodPostStatus.LOW_STOCK));
          list.add(b.greaterThan(r.get("availableUntil"), now()));
          list.add(
              b.gt(
                  r.<Integer>get("quantity"),
                  b.sum(b.sum(r.<Integer>get("reservedQuantity"), r.<Integer>get("collectedQuantity")),
                      r.<Integer>get("offlineQuantity"))));
          list.add(b.isTrue(r.get("owner").get("active")));
          if (availableNow) list.add(b.lessThanOrEqualTo(r.get("availableFrom"), now()));
          if (cat != null) list.add(b.equal(r.get("category"), cat));
          if (ownerScope.equals("mine")) list.add(b.equal(r.get("owner").get("id"), ownerFilter.getId()));
          if (ownerScope.equals("others") && ownerFilter != null) list.add(b.notEqual(r.get("owner").get("id"), ownerFilter.getId()));
          if (text != null && !text.isBlank()) {
            String pattern =
                "%"
                    + text.toLowerCase(Locale.ROOT)
                        .replace("\\", "\\\\")
                        .replace("%", "\\%")
                        .replace("_", "\\_")
                    + "%";
            list.add(
                b.or(
                    b.like(b.lower(r.get("title")), pattern, '\\'),
                    b.like(b.lower(r.get("pickupLocationName")), pattern, '\\')));
          }
          if (q.getResultType() != Long.class)
            q.orderBy(strategy.order(b, r, lat, lng), b.asc(r.get("id")));
          return b.and(list.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    return PageView.of(
        posts.findAll(spec, PageRequest.of(page, size)).map(p -> mapper.map(p, email, lat, lng)));
  }

  @Transactional(readOnly = true)
  public Map<String, Long> stats() {
    return Map.of("shared", posts.totalCollected(), "posts", posts.count());
  }
}
