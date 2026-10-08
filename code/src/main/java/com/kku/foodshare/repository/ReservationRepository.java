package com.kku.foodshare.repository;

import com.kku.foodshare.domain.entity.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
  @Query("select r.post.id from Reservation r where r.id=:id")
  Optional<Long> findPostId(@Param("id") long id);

  Optional<Reservation> findByMemberIdAndRequestKey(Long member, String key);

  Optional<Reservation> findByPostIdAndMemberIdAndStatus(Long post, Long member, ReservationStatus status);

  boolean existsByPostIdAndMemberIdAndStatus(Long post, Long member, ReservationStatus status);

  List<Reservation> findByPostIdAndStatus(Long id, ReservationStatus status);

  @Query("select coalesce(max(r.quantity), 0) from Reservation r where r.post.id=:post and r.status=:status")
  int maxQuantityByPostIdAndStatus(
      @Param("post") long postId, @Param("status") ReservationStatus status);

  List<Reservation> findByPostIdOrderByCreatedAtDesc(Long id);

  Page<Reservation> findByMemberIdOrderByCreatedAtDesc(Long id, Pageable page);

  @Query("""
      select r.id from Reservation r
      where r.status = com.kku.foodshare.domain.entity.ReservationStatus.RESERVED
        and r.member.active = true and r.post.owner.active = true
        and r.post.status in :states
        and r.post.availableUntil > :now and r.post.availableUntil <= :until
        and (:memberId is null or r.member.id = :memberId)
        and not exists (select n.id from Notification n where n.user.id = r.member.id
          and n.dedupeKey = concat('pickup-deadline:', cast(r.id as string)))
      order by r.id
      """)
  List<Long> findDuePickupReminderIds(
      @Param("now") java.time.LocalDateTime now,
      @Param("until") java.time.LocalDateTime until,
      @Param("states") Collection<FoodPostStatus> states,
      @Param("memberId") Long memberId, Pageable page);

  @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("select r from Reservation r where r.id = :id")
  Optional<Reservation> lockForPickupReminder(@Param("id") long id);
}
