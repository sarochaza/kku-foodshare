package com.kku.foodshare.repository;

import com.kku.foodshare.domain.entity.Pickup;
import com.kku.foodshare.domain.entity.PickupStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PickupRepository extends JpaRepository<Pickup, Long> {

    List<Pickup> findByFoodPostId(Long foodPostId);

    List<Pickup> findByFoodPostIdAndStatus(
            Long foodPostId,
            PickupStatus status
    );

    List<Pickup> findByUserId(Long userId); 

    Optional<Pickup> findByUserIdAndFoodPostIdAndStatus(
        Long userId,
        Long foodPostId,
        PickupStatus status
);
}