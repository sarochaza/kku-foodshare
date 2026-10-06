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
}
