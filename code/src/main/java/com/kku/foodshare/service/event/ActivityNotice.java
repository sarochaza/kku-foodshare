package com.kku.foodshare.service.event;

import com.kku.foodshare.domain.entity.User;

public record ActivityNotice(User user, String title, String message, String href) {}
