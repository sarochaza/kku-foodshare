package com.kku.foodshare.service.impl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kku.foodshare.domain.entity.FoodPostStatus;
import com.kku.foodshare.dto.response.MapFoodPostResponse;
import com.kku.foodshare.mapper.FoodPostMapper;
import com.kku.foodshare.repository.FoodPostRepository;
import com.kku.foodshare.service.FoodPostService;

@Service
public class FoodPostServiceImpl
        implements FoodPostService {

    private static final
    List<FoodPostStatus> ACTIVE_STATUSES =
            List.of(
                    FoodPostStatus.AVAILABLE,
                    FoodPostStatus.LOW_STOCK
            );

    private final FoodPostRepository repository;
    private final FoodPostMapper mapper;
    private final Clock clock;

    public FoodPostServiceImpl(
            FoodPostRepository repository,
            FoodPostMapper mapper,
            Clock clock
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MapFoodPostResponse>
            getActiveMapPosts() {

        LocalDateTime now =
                LocalDateTime.now(clock);

        return repository
                .findActiveMapPosts(
                        ACTIVE_STATUSES,
                        now
                )
                .stream()
                .map(mapper::toMapResponse)
                .toList();
    }
}