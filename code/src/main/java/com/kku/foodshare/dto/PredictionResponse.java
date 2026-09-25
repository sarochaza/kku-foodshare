package com.kku.foodshare.dto;

public class PredictionResponse {

    private Long foodPostId;
    private Integer initialQuantity;
    private Integer pickedUpQuantity;
    private Integer remainingQuantity;
    private Double pickupRatePerHour;
    private String estimatedSoldOutTime;

    public PredictionResponse() {
    }

    public Long getFoodPostId() {
        return foodPostId;
    }

    public void setFoodPostId(Long foodPostId) {
        this.foodPostId = foodPostId;
    }

    public Integer getInitialQuantity() {
        return initialQuantity;
    }

    public void setInitialQuantity(Integer initialQuantity) {
        this.initialQuantity = initialQuantity;
    }

    public Integer getPickedUpQuantity() {
        return pickedUpQuantity;
    }

    public void setPickedUpQuantity(Integer pickedUpQuantity) {
        this.pickedUpQuantity = pickedUpQuantity;
    }

    public Integer getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(Integer remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public Double getPickupRatePerHour() {
        return pickupRatePerHour;
    }

    public void setPickupRatePerHour(Double pickupRatePerHour) {
        this.pickupRatePerHour = pickupRatePerHour;
    }

    public String getEstimatedSoldOutTime() {
        return estimatedSoldOutTime;
    }

    public void setEstimatedSoldOutTime(String estimatedSoldOutTime) {
        this.estimatedSoldOutTime = estimatedSoldOutTime;
    }
}