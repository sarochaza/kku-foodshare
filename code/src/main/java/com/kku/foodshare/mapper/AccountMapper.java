package com.kku.foodshare.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.kku.foodshare.domain.entity.FoodPost;
import com.kku.foodshare.domain.entity.FoodPostStatus;
import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.dto.response.AccountPageResponse;

@Component
public class AccountMapper {

    public AccountPageResponse toResponse(
            User user,
            String providerLabel,
            List<FoodPost> posts,
            long availablePosts,
            long sharedPosts
    ) {
        List<AccountPageResponse.PostItem> postItems = posts.stream()
                .map(this::toPostItem)
                .toList();

        return new AccountPageResponse(
                user.getDisplayName(),
                user.getEmail(),
                providerLabel,
                user.getCreatedAt(),
                posts.size(),
                availablePosts,
                sharedPosts,
                postItems
        );
    }

    private AccountPageResponse.PostItem toPostItem(FoodPost post) {
        FoodPostStatus status = post.getStatus();

        String statusLabel = switch (status) {
            case AVAILABLE -> "รับได้";
            case LOW_STOCK -> "เหลือน้อย";
            case CLAIMED -> "ส่งต่อแล้ว";
            case EXPIRED -> "หมดเวลา";
            case CANCELLED -> "ยกเลิก";
        };

        boolean available = status == FoodPostStatus.AVAILABLE
                || status == FoodPostStatus.LOW_STOCK;

        return new AccountPageResponse.PostItem(
                post.getTitle(),
                post.getPickupLocationName(),
                post.getCreatedAt(),
                statusLabel,
                available
        );
    }
}