package com.kku.foodshare.service;

import com.kku.foodshare.domain.entity.Pickup;

public interface PickupService {

    Pickup createInterest(
            Long userId,
            Long foodPostId
    );

    Pickup confirmPickup(
            Long pickupId,
            Integer quantity
    );

    void cancelInterest(Long pickupId);
}