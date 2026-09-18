package com.kku.foodshare.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MapFoodPostResponse {

    private final Long id;
    private final String title;
    private final String description;
    private final String category;
    private final Integer quantity;
    private final String unit;
    private final String pickupLocationName;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final LocalDateTime availableFrom;
    private final LocalDateTime availableUntil;
    private final String status;
    private final String categoryIcon;

    public MapFoodPostResponse(
            Long id,
            String title,
            String description,
            String category,
            Integer quantity,
            String unit,
            String pickupLocationName,
            BigDecimal latitude,
            BigDecimal longitude,
            LocalDateTime availableFrom,
            LocalDateTime availableUntil,
            String status,
            String categoryIcon
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.pickupLocationName =
                pickupLocationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.availableFrom = availableFrom;
        this.availableUntil = availableUntil;
        this.status = status;
        this.categoryIcon = categoryIcon;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public String getPickupLocationName() {
        return pickupLocationName;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public LocalDateTime getAvailableFrom() {
        return availableFrom;
    }

    public LocalDateTime getAvailableUntil() {
        return availableUntil;
    }

    public String getStatus() {
        return status;
    }

    public String getCategoryIcon() {
        return categoryIcon;
    }
}