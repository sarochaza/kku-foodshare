package com.kku.foodshare.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kku.foodshare.domain.entity.Pickup;
import com.kku.foodshare.domain.entity.PickupStatus;
import com.kku.foodshare.repository.FoodPostRepository;
import com.kku.foodshare.repository.PickupRepository;
import com.kku.foodshare.repository.UserRepository;
import com.kku.foodshare.service.PickupService;

@Service
public class PickupServiceImpl implements PickupService {

    private final PickupRepository pickupRepository;
    private final FoodPostRepository foodPostRepository;
    private final UserRepository userRepository;

    public PickupServiceImpl(
            PickupRepository pickupRepository,
            FoodPostRepository foodPostRepository,
            UserRepository userRepository
    ) {
        this.pickupRepository = pickupRepository;
        this.foodPostRepository = foodPostRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Pickup createInterest(Long userId, Long foodPostId) {

        var user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        var foodPost = foodPostRepository.findById(foodPostId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Food post not found"));

        var existingPickup = pickupRepository
                .findByUserIdAndFoodPostIdAndStatus(
                        userId,
                        foodPostId,
                        PickupStatus.INTERESTED
                );

        if (existingPickup.isPresent()) {
            throw new IllegalStateException(
                    "User already interested in this food post"
            );
        }

        var pickup = new Pickup();

        pickup.setUser(user);
        pickup.setFoodPost(foodPost);
        pickup.setStatus(PickupStatus.INTERESTED);
        pickup.setQuantity(0);

        return pickupRepository.save(pickup);
    }

    @Override
    public void cancelInterest(Long pickupId) {

        var pickup = pickupRepository.findById(pickupId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Pickup not found"));

        if (pickup.getStatus() != PickupStatus.INTERESTED) {
            throw new IllegalStateException(
                    "Only interested pickup can be cancelled"
            );
        }

        pickup.setStatus(PickupStatus.CANCELLED);

        pickupRepository.save(pickup);
    }

    @Override
    @Transactional
    public Pickup confirmPickup(Long pickupId, Integer quantity) {

        var pickup = pickupRepository.findById(pickupId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Pickup not found"));

        if (pickup.getStatus() != PickupStatus.INTERESTED) {
            throw new IllegalStateException(
                    "Pickup is not in interested status"
            );
        }

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than 0"
            );
        }

        var foodPost = pickup.getFoodPost();

        int pickedUpQuantity = pickupRepository
                .findByFoodPostIdAndStatus(
                        foodPost.getId(),
                        PickupStatus.PICKED_UP
                )
                .stream()
                .mapToInt(Pickup::getQuantity)
                .sum();

        int remainingQuantity =
                foodPost.getQuantity() - pickedUpQuantity;

        if (quantity > remainingQuantity) {
            throw new IllegalArgumentException(
                    "Quantity exceeds remaining food"
            );
        }

        pickup.setQuantity(quantity);
        pickup.setStatus(PickupStatus.PICKED_UP);

        return pickupRepository.save(pickup);
    }
}