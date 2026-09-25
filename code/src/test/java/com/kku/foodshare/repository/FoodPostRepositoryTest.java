package com.kku.foodshare.repository;

import com.kku.foodshare.domain.entity.FoodCategory;
import com.kku.foodshare.domain.entity.FoodPost;
import com.kku.foodshare.domain.entity.FoodPostStatus;
import com.kku.foodshare.domain.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class FoodPostRepositoryTest {

    @Autowired
    private FoodPostRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldFindOnlyActiveMapPosts() {

        LocalDateTime now =
                LocalDateTime.of(
                        2026, 9, 18, 12, 0
                );

        User owner = createOwner();
        entityManager.persist(owner);

        FoodPost activePost = createPost(
                owner,
                "ข้าวกล่องที่ยังรับได้",
                FoodPostStatus.AVAILABLE,
                now.minusHours(1),
                now.plusHours(2)
        );

        FoodPost expiredPost = createPost(
                owner,
                "ขนมที่หมดเวลารับแล้ว",
                FoodPostStatus.AVAILABLE,
                now.minusHours(3),
                now.minusMinutes(1)
        );

        FoodPost cancelledPost = createPost(
                owner,
                "เครื่องดื่มที่ยกเลิกแล้ว",
                FoodPostStatus.CANCELLED,
                now.minusHours(1),
                now.plusHours(2)
        );

        entityManager.persist(activePost);
        entityManager.persist(expiredPost);
        entityManager.persist(cancelledPost);
        entityManager.flush();

        List<FoodPost> result =
                repository.findActiveMapPosts(
                        List.of(
                                FoodPostStatus.AVAILABLE,
                                FoodPostStatus.LOW_STOCK
                        ),
                        now
                );

        assertEquals(1, result.size());
        assertEquals(
                "ข้าวกล่องที่ยังรับได้",
                result.get(0).getTitle()
        );
    }

    private User createOwner() {

        User owner = new User();

        owner.setEmail(
                "food-map-test@kku.ac.th"
        );
        owner.setPassword("encoded-password");
        owner.setDisplayName("ผู้ทดสอบแผนที่");
        owner.setActive(true);
        owner.setCreatedAt(
                LocalDateTime.of(
                        2026, 9, 18, 9, 0
                )
        );

        return owner;
    }

    private FoodPost createPost(
            User owner,
            String title,
            FoodPostStatus status,
            LocalDateTime availableFrom,
            LocalDateTime availableUntil
    ) {

        FoodPost post = new FoodPost();

        post.setOwner(owner);
        post.setTitle(title);
        post.setDescription(
                "ข้อมูลสำหรับทดสอบ Repository"
        );
        post.setCategory(FoodCategory.FOOD);
        post.setQuantity(10);
        post.setUnit("กล่อง");
        post.setPickupLocationName(
                "มหาวิทยาลัยขอนแก่น"
        );
        post.setLatitude(
                new BigDecimal("16.4745000")
        );
        post.setLongitude(
                new BigDecimal("102.8237000")
        );
        post.setAvailableFrom(availableFrom);
        post.setAvailableUntil(availableUntil);
        post.setStatus(status);
        post.setCreatedAt(
                LocalDateTime.of(
                        2026, 9, 18, 9, 0
                )
        );
        post.setUpdatedAt(
                LocalDateTime.of(
                        2026, 9, 18, 9, 0
                )
        );

        return post;
    }
}