package com.kku.foodshare.service;

import com.kku.foodshare.domain.entity.User;

public interface GoogleUserService {

    User findOrCreateGoogleUser(String email, String displayName);
}