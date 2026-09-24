package com.kku.foodshare.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PickupRequest {

    private Long userId;
    private Long foodPostId;
    private Integer quantity;

    public PickupRequest() {
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
}