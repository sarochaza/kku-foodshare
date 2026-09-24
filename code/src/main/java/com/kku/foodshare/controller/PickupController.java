package com.kku.foodshare.controller;

import com.kku.foodshare.domain.entity.Pickup;
import com.kku.foodshare.service.PickupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pickups")
public class PickupController {

    private final PickupService pickupService;

    public PickupController(PickupService pickupService) {
        this.pickupService = pickupService;
    }

    @PostMapping("/interest")
    public ResponseEntity<Pickup> createInterest(
            @RequestParam Long userId,
            @RequestParam Long foodPostId) {

        Pickup pickup = pickupService.createInterest(userId, foodPostId);

        return ResponseEntity.ok(pickup);
    }

    @PutMapping("/{pickupId}/confirm")
    public ResponseEntity<Pickup> confirmPickup(
            @PathVariable Long pickupId,
            @RequestParam Integer quantity) {

        Pickup pickup = pickupService.confirmPickup(pickupId, quantity);

        return ResponseEntity.ok(pickup);
    }

    @PutMapping("/{pickupId}/cancel")
    public ResponseEntity<Void> cancelInterest(
            @PathVariable Long pickupId) {

        pickupService.cancelInterest(pickupId);

        return ResponseEntity.noContent().build();
    }
}