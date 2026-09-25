package com.kku.foodshare.mapper;

import com.kku.foodshare.domain.entity.FoodCategory;
import com.kku.foodshare.domain.entity.FoodPost;
import com.kku.foodshare.domain.entity.FoodPostStatus;
import com.kku.foodshare.dto.response.MapFoodPostResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FoodPostMapperTest {

    @Test
    void shouldMapFoodPostForMapResponse() {

        LocalDateTime availableFrom =
                LocalDateTime.of(
                        2026, 9, 18, 12, 0
                );

        LocalDateTime availableUntil =
                LocalDateTime.of(
                        2026, 9, 18, 15, 0
                );

        FoodPost post = new FoodPost();

        post.setId(10L);
        post.setTitle(
                "ข้าวกล่องจากงานสัมมนา"
        );
        post.setDescription(
                "รับได้ที่หน้าอาคาร"
        );
        post.setCategory(FoodCategory.FOOD);
        post.setQuantity(20);
        post.setUnit("กล่อง");
        post.setPickupLocationName(
                "อาคารวิทยวิภาส"
        );
        post.setLatitude(
                new BigDecimal("16.4745000")
        );
        post.setLongitude(
                new BigDecimal("102.8237000")
        );
        post.setAvailableFrom(availableFrom);
        post.setAvailableUntil(availableUntil);
        post.setStatus(
                FoodPostStatus.AVAILABLE
        );

        FoodPostMapper mapper =
                new FoodPostMapper();

        MapFoodPostResponse response =
                mapper.toMapResponse(post);

        assertEquals(
                10L,
                response.getId()
        );
        assertEquals(
                "ข้าวกล่องจากงานสัมมนา",
                response.getTitle()
        );
        assertEquals(
                "รับได้ที่หน้าอาคาร",
                response.getDescription()
        );
        assertEquals(
                "FOOD",
                response.getCategory()
        );
        assertEquals(
                20,
                response.getQuantity()
        );
        assertEquals(
                "กล่อง",
                response.getUnit()
        );
        assertEquals(
                "อาคารวิทยวิภาส",
                response.getPickupLocationName()
        );
        assertEquals(
                new BigDecimal("16.4745000"),
                response.getLatitude()
        );
        assertEquals(
                new BigDecimal("102.8237000"),
                response.getLongitude()
        );
        assertEquals(
                availableFrom,
                response.getAvailableFrom()
        );
        assertEquals(
                availableUntil,
                response.getAvailableUntil()
        );
        assertEquals(
                "AVAILABLE",
                response.getStatus()
        );
        assertEquals(
                "🍱",
                response.getCategoryIcon()
        );
    }
}
