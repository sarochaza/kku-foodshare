package com.kku.foodshare.service;

import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.dto.request.RegisterRequest;

public interface UserService {

    User register(RegisterRequest request);
}