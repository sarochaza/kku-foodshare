package com.kku.foodshare.service.impl;

import com.kku.foodshare.domain.entity.FoodPost;
import com.kku.foodshare.domain.entity.PickupStatus;
import com.kku.foodshare.dto.PredictionResponse;
import com.kku.foodshare.repository.FoodPostRepository;
import com.kku.foodshare.repository.PickupRepository;
import com.kku.foodshare.service.PredictionService;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class PredictionServiceImpl implements PredictionService {

    private final FoodPostRepository foodPostRepository;
    private final PickupRepository pickupRepository;

    public PredictionServiceImpl(
            FoodPostRepository foodPostRepository,
            PickupRepository pickupRepository) {
        this.foodPostRepository = foodPostRepository;
        this.pickupRepository = pickupRepository;
    }

    @Override
    public PredictionResponse predict(Long foodPostId) {

        FoodPost foodPost = foodPostRepository.findById(foodPostId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Food post not found"));

        int pickedUpQuantity = pickupRepository
                .findByFoodPostIdAndStatus(
                        foodPostId,
                        PickupStatus.PICKED_UP
                )
                .stream()
                .mapToInt(pickup -> pickup.getQuantity())
                .sum();

        int initialQuantity = foodPost.getQuantity();

        int remainingQuantity =
                initialQuantity - pickedUpQuantity;

        PredictionResponse response = new PredictionResponse();

        response.setFoodPostId(foodPostId);
        response.setInitialQuantity(initialQuantity);
        response.setPickedUpQuantity(pickedUpQuantity);
        response.setRemainingQuantity(
                Math.max(remainingQuantity, 0)
        );

        /*
         * Calculate pickup rate using the time
         * from food post creation until now.
         */
        LocalDateTime createdAt = foodPost.getCreatedAt();
        LocalDateTime now = LocalDateTime.now();

        long minutes = Duration.between(createdAt, now).toMinutes();

        double pickupRatePerHour = 0.0;

        if (pickedUpQuantity > 0 && minutes > 0) {
            pickupRatePerHour =
                    pickedUpQuantity / (minutes / 60.0);
        }

        response.setPickupRatePerHour(pickupRatePerHour);

        /*
         * Estimate when the remaining food will run out.
         */
        if (remainingQuantity <= 0) {

            response.setEstimatedSoldOutTime(
                    now.toString()
            );

        } else if (pickupRatePerHour > 0) {

            double hoursRemaining =
                    remainingQuantity / pickupRatePerHour;

            LocalDateTime estimatedTime =
                    now.plusMinutes(
                            (long) (hoursRemaining * 60)
                    );

            response.setEstimatedSoldOutTime(
                    estimatedTime.toString()
            );

        } else {

            response.setEstimatedSoldOutTime(
                    "Insufficient data"
            );
        }

        return response;
    }
}