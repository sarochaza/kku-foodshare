package com.kku.foodshare.service.event;

import com.kku.foodshare.domain.entity.FoodPost;

public record PostClosed(FoodPost post, String reason) {}
