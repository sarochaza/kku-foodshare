package com.kku.foodshare.service;

import com.kku.foodshare.domain.entity.FoodCategory;
import com.kku.foodshare.domain.entity.FoodPost;
import com.kku.foodshare.domain.entity.FoodPostStatus;
import com.kku.foodshare.dto.response.MapFoodPostResponse;
import com.kku.foodshare.mapper.FoodPostMapper;
import com.kku.foodshare.repository.FoodPostRepository;
import com.kku.foodshare.service.impl.FoodPostServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FoodPostServiceTest {

    @Test
    void shouldReturnActiveMapPostsInRepositoryOrder() {

        FoodPostRepository repository =
                mock(FoodPostRepository.class);

        FoodPostMapper mapper =
                new FoodPostMapper();

        Clock clock = Clock.fixed(
                Instant.parse(
                        "2026-09-18T05:00:00Z"
                ),
                ZoneId.of("Asia/Bangkok")
        );

        LocalDateTime now =
                LocalDateTime.of(
                        2026, 9, 18, 12, 0
                );

        FoodPost firstPost = createPost(
                1L,
                "ข้าวกล่อง",
                FoodCategory.FOOD
        );

        FoodPost secondPost = createPost(
                2L,
                "น้ำผลไม้",
                FoodCategory.DRINK
        );

        when(
                repository.findActiveMapPosts(
                        List.of(
                                FoodPostStatus.AVAILABLE,
                                FoodPostStatus.LOW_STOCK
                        ),
                        now
                )
        ).thenReturn(
                List.of(firstPost, secondPost)
        );

        FoodPostService service =
                new FoodPostServiceImpl(
                        repository,
                        mapper,
                        clock
                );

        List<MapFoodPostResponse> result =
                service.getActiveMapPosts();

        assertEquals(2, result.size());
        assertEquals(
                "ข้าวกล่อง",
                result.get(0).getTitle()
        );
        assertEquals(
                "น้ำผลไม้",
                result.get(1).getTitle()
        );

        verify(repository).findActiveMapPosts(
                List.of(
                        FoodPostStatus.AVAILABLE,
                        FoodPostStatus.LOW_STOCK
                ),
                now
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> result.add(result.get(0))
        );
    }

    private FoodPost createPost(
            Long id,
            String title,
            FoodCategory category
    ) {

        FoodPost post = new FoodPost();

        post.setId(id);
        post.setTitle(title);
        post.setDescription(
                "อาหารสำหรับทดสอบ Service"
        );
        post.setCategory(category);
        post.setQuantity(10);
        post.setUnit("ชุด");
        post.setPickupLocationName(
                "มหาวิทยาลัยขอนแก่น"
        );
        post.setLatitude(
                new BigDecimal("16.4745000")
        );
        post.setLongitude(
                new BigDecimal("102.8237000")
        );
        post.setAvailableFrom(
                LocalDateTime.of(
                        2026, 9, 18, 11, 0
                )
        );
        post.setAvailableUntil(
                LocalDateTime.of(
                        2026, 9, 18, 15, 0
                )
        );
        post.setStatus(
                FoodPostStatus.AVAILABLE
        );

        return post;
    }
}