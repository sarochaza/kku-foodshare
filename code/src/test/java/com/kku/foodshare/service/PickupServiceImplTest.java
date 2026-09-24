package com.kku.foodshare.service;

import com.kku.foodshare.domain.entity.FoodPost;
import com.kku.foodshare.domain.entity.Pickup;
import com.kku.foodshare.domain.entity.PickupStatus;
import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.repository.FoodPostRepository;
import com.kku.foodshare.repository.PickupRepository;
import com.kku.foodshare.repository.UserRepository;
import com.kku.foodshare.service.impl.PickupServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PickupServiceImplTest {

    @Mock
    private PickupRepository pickupRepository;

    @Mock
    private FoodPostRepository foodPostRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PickupServiceImpl pickupService;

    private User user;
    private FoodPost foodPost;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        foodPost = new FoodPost();
        foodPost.setId(1L);
        foodPost.setQuantity(10);
    }

    @Test
    void createInterest_shouldCreatePickupSuccessfully() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(foodPostRepository.findById(1L))
                .thenReturn(Optional.of(foodPost));

        when(pickupRepository.findByUserIdAndFoodPostIdAndStatus(
                1L, 1L, PickupStatus.INTERESTED))
                .thenReturn(Optional.empty());

        when(pickupRepository.save(any(Pickup.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Pickup result = pickupService.createInterest(1L, 1L);

        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals(foodPost, result.getFoodPost());
        assertEquals(PickupStatus.INTERESTED, result.getStatus());
        assertEquals(0, result.getQuantity());

        verify(pickupRepository).save(any(Pickup.class));
    }

    @Test
    void createInterest_shouldThrowWhenUserNotFound() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> pickupService.createInterest(1L, 1L)
        );
    }

    @Test
    void createInterest_shouldThrowWhenFoodPostNotFound() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(foodPostRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> pickupService.createInterest(1L, 1L)
        );
    }

    @Test
    void createInterest_shouldThrowWhenAlreadyInterested() {
        Pickup existingPickup = new Pickup();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(foodPostRepository.findById(1L))
                .thenReturn(Optional.of(foodPost));

        when(pickupRepository.findByUserIdAndFoodPostIdAndStatus(
                1L, 1L, PickupStatus.INTERESTED))
                .thenReturn(Optional.of(existingPickup));

        assertThrows(
                IllegalStateException.class,
                () -> pickupService.createInterest(1L, 1L)
        );

        verify(pickupRepository, never()).save(any(Pickup.class));
    }

    @Test
    void cancelInterest_shouldCancelSuccessfully() {
        Pickup pickup = new Pickup();
        pickup.setStatus(PickupStatus.INTERESTED);

        when(pickupRepository.findById(1L))
                .thenReturn(Optional.of(pickup));

        pickupService.cancelInterest(1L);

        assertEquals(PickupStatus.CANCELLED, pickup.getStatus());
        verify(pickupRepository).save(pickup);
    }

    @Test
    void cancelInterest_shouldThrowWhenPickupNotFound() {
        when(pickupRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> pickupService.cancelInterest(1L)
        );
    }

    @Test
    void cancelInterest_shouldThrowWhenStatusIsNotInterested() {
        Pickup pickup = new Pickup();
        pickup.setStatus(PickupStatus.PICKED_UP);

        when(pickupRepository.findById(1L))
                .thenReturn(Optional.of(pickup));

        assertThrows(
                IllegalStateException.class,
                () -> pickupService.cancelInterest(1L)
        );

        verify(pickupRepository, never()).save(any(Pickup.class));
    }

    @Test
    void confirmPickup_shouldConfirmSuccessfully() {
        Pickup pickup = new Pickup();
        pickup.setFoodPost(foodPost);
        pickup.setStatus(PickupStatus.INTERESTED);

        when(pickupRepository.findById(1L))
                .thenReturn(Optional.of(pickup));

        when(pickupRepository.findByFoodPostIdAndStatus(
                1L, PickupStatus.PICKED_UP))
                .thenReturn(List.of());

        when(pickupRepository.save(any(Pickup.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Pickup result = pickupService.confirmPickup(1L, 3);

        assertEquals(PickupStatus.PICKED_UP, result.getStatus());
        assertEquals(3, result.getQuantity());

        verify(pickupRepository).save(pickup);
    }

    @Test
    void confirmPickup_shouldThrowWhenQuantityIsInvalid() {
        Pickup pickup = new Pickup();
        pickup.setFoodPost(foodPost);
        pickup.setStatus(PickupStatus.INTERESTED);

        when(pickupRepository.findById(1L))
                .thenReturn(Optional.of(pickup));

        assertThrows(
                IllegalArgumentException.class,
                () -> pickupService.confirmPickup(1L, 0)
        );

        verify(pickupRepository, never()).save(any(Pickup.class));
    }

    @Test
    void confirmPickup_shouldThrowWhenQuantityExceedsRemaining() {
        Pickup existingPickup = new Pickup();
        existingPickup.setQuantity(8);

        Pickup pickup = new Pickup();
        pickup.setFoodPost(foodPost);
        pickup.setStatus(PickupStatus.INTERESTED);

        when(pickupRepository.findById(1L))
                .thenReturn(Optional.of(pickup));

        when(pickupRepository.findByFoodPostIdAndStatus(
                1L, PickupStatus.PICKED_UP))
                .thenReturn(List.of(existingPickup));

        assertThrows(
                IllegalArgumentException.class,
                () -> pickupService.confirmPickup(1L, 3)
        );

        verify(pickupRepository, never()).save(any(Pickup.class));
    }

    @Test
    void confirmPickup_shouldThrowWhenPickupAlreadyPickedUp() {
        Pickup pickup = new Pickup();
        pickup.setFoodPost(foodPost);
        pickup.setStatus(PickupStatus.PICKED_UP);

        when(pickupRepository.findById(1L))
                .thenReturn(Optional.of(pickup));

        assertThrows(
                IllegalStateException.class,
                () -> pickupService.confirmPickup(1L, 2)
        );

        verify(pickupRepository, never()).save(any(Pickup.class));
    }
}