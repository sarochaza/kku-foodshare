package com.kku.foodshare.service;

import com.kku.foodshare.domain.entity.Pickup;
import java.util.List;

public interface PickupService {

    Pickup createInterest(Long userId, Long foodPostId);

    Pickup confirmPickup(Long pickupId, Integer quantity);

    void cancelInterest(Long pickupId);

    List<Pickup> getPickupsByUser(Long userId);

    List<Pickup> getPickupsByFoodPost(Long foodPostId);
}