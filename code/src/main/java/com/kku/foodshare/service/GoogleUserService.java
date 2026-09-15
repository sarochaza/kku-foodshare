package com.kku.foodshare.service;

import com.kku.foodshare.domain.entity.User;
//GoogleUserService เป็น Interface กำหนดความสามารถของ Service
public interface GoogleUserService {

    User findOrCreateGoogleUser(String email, String displayName);
}