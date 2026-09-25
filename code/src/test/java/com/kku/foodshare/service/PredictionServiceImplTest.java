package com.kku.foodshare.service;

import com.kku.foodshare.domain.entity.FoodPost;
import com.kku.foodshare.domain.entity.Pickup;
import com.kku.foodshare.domain.entity.PickupStatus;
import com.kku.foodshare.dto.PredictionResponse;
import com.kku.foodshare.repository.FoodPostRepository;
import com.kku.foodshare.repository.PickupRepository;
import com.kku.foodshare.service.impl.PredictionServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PredictionServiceImplTest {

    @Mock
    private FoodPostRepository foodPostRepository;

    @Mock
    private PickupRepository pickupRepository;

    @InjectMocks
    private PredictionServiceImpl predictionService;

    private FoodPost foodPost;

    @BeforeEach
    void setUp() {
        foodPost = new FoodPost();

        foodPost.setId(1L);
        foodPost.setQuantity(10);
        foodPost.setCreatedAt(
                LocalDateTime.now().minusMinutes(30)
        );
    }

    @Test
    void shouldCalculateRemainingQuantity() {

        Pickup pickup = new Pickup();
        pickup.setQuantity(3);
        pickup.setStatus(PickupStatus.PICKED_UP);

        when(foodPostRepository.findById(1L))
                .thenReturn(Optional.of(foodPost));

        when(pickupRepository.findByFoodPostIdAndStatus(
                1L,
                PickupStatus.PICKED_UP
        )).thenReturn(List.of(pickup));

        PredictionResponse response =
                predictionService.predict(1L);

        assertEquals(10, response.getInitialQuantity());
        assertEquals(3, response.getPickedUpQuantity());
        assertEquals(7, response.getRemainingQuantity());
    }

    @Test
    void shouldReturnZeroRemainingWhenAllFoodPickedUp() {

        Pickup pickup = new Pickup();
        pickup.setQuantity(10);
        pickup.setStatus(PickupStatus.PICKED_UP);

        when(foodPostRepository.findById(1L))
                .thenReturn(Optional.of(foodPost));

        when(pickupRepository.findByFoodPostIdAndStatus(
                1L,
                PickupStatus.PICKED_UP
        )).thenReturn(List.of(pickup));

        PredictionResponse response =
                predictionService.predict(1L);

        assertEquals(0, response.getRemainingQuantity());
        assertNotNull(response.getEstimatedSoldOutTime());
    }

    @Test
    void shouldThrowExceptionWhenFoodPostNotFound() {

        when(foodPostRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> predictionService.predict(999L)
        );
    }

    @Test
    void shouldReturnInsufficientDataWhenNoPickup() {

        when(foodPostRepository.findById(1L))
                .thenReturn(Optional.of(foodPost));

        when(pickupRepository.findByFoodPostIdAndStatus(
                1L,
                PickupStatus.PICKED_UP
        )).thenReturn(List.of());

        PredictionResponse response =
                predictionService.predict(1L);

        assertEquals(10, response.getInitialQuantity());
        assertEquals(0, response.getPickedUpQuantity());
        assertEquals(10, response.getRemainingQuantity());
        assertEquals(
                "Insufficient data",
                response.getEstimatedSoldOutTime()
        );
    }
}