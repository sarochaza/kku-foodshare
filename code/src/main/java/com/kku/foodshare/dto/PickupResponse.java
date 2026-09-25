package com.kku.foodshare.dto;

import com.kku.foodshare.domain.entity.PickupStatus;

import java.time.LocalDateTime;

public class PickupResponse {

    private Long id;
    private Long userId;
    private Long foodPostId;
    private Integer quantity;
    private PickupStatus status;
    private LocalDateTime createdAt;

    public PickupResponse() {
    }

    public PickupResponse(
            Long id,
            Long userId,
            Long foodPostId,
            Integer quantity,
            PickupStatus status,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.foodPostId = foodPostId;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getFoodPostId() {
        return foodPostId;
    }

    public void setFoodPostId(Long foodPostId) {
        this.foodPostId = foodPostId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public PickupStatus getStatus() {
        return status;
    }

    public void setStatus(PickupStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}