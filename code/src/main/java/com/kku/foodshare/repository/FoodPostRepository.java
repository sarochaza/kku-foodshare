package com.kku.foodshare.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kku.foodshare.domain.entity.FoodPost;
import com.kku.foodshare.domain.entity.FoodPostStatus;

public interface FoodPostRepository
        extends JpaRepository<FoodPost, Long> {

    // เพิ่มบรรทัดนี้: ดึงโพสต์ของผู้ใช้ตาม ownerId เรียงจากใหม่ไปเก่า
    List<FoodPost> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    @Query("""
            SELECT post
            FROM FoodPost post
            WHERE post.status IN :statuses
              AND post.availableFrom <= :now
              AND post.availableUntil > :now
            ORDER BY post.availableUntil ASC
            """)
    List<FoodPost> findActiveMapPosts(
            @Param("statuses")
            Collection<FoodPostStatus> statuses,

            @Param("now")
            LocalDateTime now
    );
}