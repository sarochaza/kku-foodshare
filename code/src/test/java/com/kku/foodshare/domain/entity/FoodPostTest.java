package com.kku.foodshare.domain.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FoodPostTest {

    @Test
    void shouldStoreFoodPostInformation() {

        User owner = new User();
        owner.setId(1L);
        owner.setEmail("student@kku.ac.th");
        owner.setDisplayName("ผู้แบ่งปันอาหาร");

        LocalDateTime availableFrom =
                LocalDateTime.of(2026, 9, 18, 12, 0);

        LocalDateTime availableUntil =
                LocalDateTime.of(2026, 9, 18, 15, 0);

        LocalDateTime createdAt =
                LocalDateTime.of(2026, 9, 18, 10, 0);

        LocalDateTime updatedAt =
                LocalDateTime.of(2026, 9, 18, 10, 30);

        FoodPost post = new FoodPost();

        post.setId(10L);
        post.setOwner(owner);
        post.setTitle("ข้าวกล่องจากงานสัมมนา");
        post.setDescription("รับได้ที่หน้าอาคาร");
        post.setCategory(FoodCategory.FOOD);
        post.setQuantity(20);
        post.setUnit("กล่อง");
        post.setPickupLocationName("อาคารวิทยวิภาส");
        post.setLatitude(new BigDecimal("16.4745000"));
        post.setLongitude(new BigDecimal("102.8237000"));
        post.setAvailableFrom(availableFrom);
        post.setAvailableUntil(availableUntil);
        post.setStatus(FoodPostStatus.AVAILABLE);
        post.setCreatedAt(createdAt);
        post.setUpdatedAt(updatedAt);

        assertEquals(10L, post.getId());
        assertEquals(owner, post.getOwner());
        assertEquals(
                "ข้าวกล่องจากงานสัมมนา",
                post.getTitle()
        );
        assertEquals(
                "รับได้ที่หน้าอาคาร",
                post.getDescription()
        );
        assertEquals(
                FoodCategory.FOOD,
                post.getCategory()
        );
        assertEquals(20, post.getQuantity());
        assertEquals("กล่อง", post.getUnit());
        assertEquals(
                "อาคารวิทยวิภาส",
                post.getPickupLocationName()
        );
        assertEquals(
                new BigDecimal("16.4745000"),
                post.getLatitude()
        );
        assertEquals(
                new BigDecimal("102.8237000"),
                post.getLongitude()
        );
        assertEquals(
                availableFrom,
                post.getAvailableFrom()
        );
        assertEquals(
                availableUntil,
                post.getAvailableUntil()
        );
        assertEquals(
                FoodPostStatus.AVAILABLE,
                post.getStatus()
        );
        assertEquals(createdAt, post.getCreatedAt());
        assertEquals(updatedAt, post.getUpdatedAt());
    }
}