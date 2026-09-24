package com.kku.foodshare.repository;

import com.kku.foodshare.domain.entity.Pickup;
import com.kku.foodshare.domain.entity.PickupStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PickupRepository extends JpaRepository<Pickup, Long> {

    List<Pickup> findByFoodPostId(Long foodPostId);

    List<Pickup> findByFoodPostIdAndStatus(
            Long foodPostId,
            PickupStatus status
    );

    List<Pickup> findByUserId(Long userId);
}