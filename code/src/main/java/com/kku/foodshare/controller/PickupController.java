package com.kku.foodshare.controller;

import com.kku.foodshare.domain.entity.Pickup;
import com.kku.foodshare.dto.PickupRequest;
import com.kku.foodshare.dto.PickupResponse;
import com.kku.foodshare.service.PickupService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pickups")
public class PickupController {

    private final PickupService pickupService;

    public PickupController(PickupService pickupService) {
        this.pickupService = pickupService;
    }

    @PostMapping("/interest")
    public ResponseEntity<PickupResponse> createInterest(
            @Valid @RequestBody PickupRequest request) {

        Pickup pickup = pickupService.createInterest(
                request.getUserId(),
                request.getFoodPostId()
        );

        return ResponseEntity.ok(toResponse(pickup));
    }

    @PutMapping("/{pickupId}/confirm")
    public ResponseEntity<PickupResponse> confirmPickup(
            @PathVariable Long pickupId,
            @Valid @RequestBody PickupRequest request) {

        Pickup pickup = pickupService.confirmPickup(
                pickupId,
                request.getQuantity()
        );

        return ResponseEntity.ok(toResponse(pickup));
    }

    @PutMapping("/{pickupId}/cancel")
    public ResponseEntity<Void> cancelInterest(
            @PathVariable Long pickupId) {

        pickupService.cancelInterest(pickupId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PickupResponse>> getByUser(
            @PathVariable Long userId) {

        List<PickupResponse> responses = pickupService
                .getPickupsByUser(userId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/food-post/{foodPostId}")
    public ResponseEntity<List<PickupResponse>> getByFoodPost(
            @PathVariable Long foodPostId) {

        List<PickupResponse> responses = pickupService
                .getPickupsByFoodPost(foodPostId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    private PickupResponse toResponse(Pickup pickup) {
        return new PickupResponse(
                pickup.getId(),
                pickup.getUser().getId(),
                pickup.getFoodPost().getId(),
                pickup.getQuantity(),
                pickup.getStatus(),
                pickup.getCreatedAt()
        );
    }
}