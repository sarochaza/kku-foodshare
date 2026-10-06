package com.kku.foodshare.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PostView(
    Long id,
    String title,
    String description,
    String category,
    int quantity,
    int reservedQuantity,
    int collectedQuantity,
    int availableQuantity,
    String unit,
    String pickupLocationName,
    BigDecimal latitude,
    BigDecimal longitude,
    LocalDateTime availableFrom,
    LocalDateTime availableUntil,
    String status,
    String ownerName,
    Long ownerId,
    String allergens,
    String imageUrl,
    boolean mine,
    Double distanceKm) {}
