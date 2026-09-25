package com.kku.foodshare.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record AccountPageResponse(
        String displayName,
        String email,
        String providerLabel,
        LocalDateTime joinedAt,
        int totalPosts,
        long availablePosts,
        long sharedPosts,
        List<PostItem> posts
) {
    public record PostItem(
            String title,
            String pickupLocation,
            LocalDateTime createdAt,
            String statusLabel,
            boolean available
    ) {
    }
}