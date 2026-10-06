package com.kku.foodshare.repository;

import com.kku.foodshare.domain.entity.FoodPost;
import com.kku.foodshare.domain.entity.FoodPostStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FoodPostRepository
    extends JpaRepository<FoodPost, Long>,
        org.springframework.data.jpa.repository.JpaSpecificationExecutor<FoodPost> {

  // เพิ่มบรรทัดนี้: ดึงโพสต์ของผู้ใช้ตาม ownerId เรียงจากใหม่ไปเก่า
  List<FoodPost> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

  @Query(
      """
      SELECT post
      FROM FoodPost post
      WHERE post.status IN :statuses
        AND post.availableFrom <= :now
        AND post.availableUntil > :now
      ORDER BY post.availableUntil ASC
      """)
  List<FoodPost> findActiveMapPosts(
      @Param("statuses") Collection<FoodPostStatus> statuses, @Param("now") LocalDateTime now);

  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from FoodPost p where p.id = :id")
  java.util.Optional<FoodPost> lockById(@Param("id") Long id);

  org.springframework.data.domain.Page<FoodPost> findByOwnerIdOrderByCreatedAtDesc(
      Long id, org.springframework.data.domain.Pageable pageable);

  java.util.List<FoodPost> findByAvailableUntilBeforeAndStatusIn(
      java.time.LocalDateTime now, java.util.Collection<FoodPostStatus> statuses);

  @Query("select p.id from FoodPost p where p.owner.id = :ownerId order by p.id")
  List<Long> findOwnedIds(@Param("ownerId") Long ownerId);

  @Query("select coalesce(sum(p.collectedQuantity + p.offlineQuantity),0) from FoodPost p")
  long totalCollected();
}
