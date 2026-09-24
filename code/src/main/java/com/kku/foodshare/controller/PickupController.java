package com.kku.foodshare.controller;

import com.kku.foodshare.domain.entity.Pickup;
import com.kku.foodshare.dto.PickupRequest;
import com.kku.foodshare.service.PickupService;
import jakarta.validation.Valid;
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
            @Valid @RequestBody PickupRequest request) {

        Pickup pickup = pickupService.createInterest(
                request.getUserId(),
                request.getFoodPostId()
        );

        return ResponseEntity.ok(pickup);
    }

    @PutMapping("/{pickupId}/confirm")
    public ResponseEntity<Pickup> confirmPickup(
            @PathVariable Long pickupId,
            @Valid @RequestBody PickupRequest request) {

        Pickup pickup = pickupService.confirmPickup(
                pickupId,
                request.getQuantity()
        );

        return ResponseEntity.ok(pickup);
    }

    @PutMapping("/{pickupId}/cancel")
    public ResponseEntity<Void> cancelInterest(
            @PathVariable Long pickupId) {

        pickupService.cancelInterest(pickupId);

        return ResponseEntity.noContent().build();
    }
}