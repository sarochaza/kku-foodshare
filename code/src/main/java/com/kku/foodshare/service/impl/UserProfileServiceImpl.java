package com.kku.foodshare.service.impl;

import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.dto.response.UserProfileResponse;
import com.kku.foodshare.mapper.UserProfileMapper;
import com.kku.foodshare.repository.UserRepository;
import com.kku.foodshare.service.UserProfileService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileServiceImpl
        implements UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileMapper userProfileMapper;

    public UserProfileServiceImpl(
            UserRepository userRepository,
            UserProfileMapper userProfileMapper) {

        this.userRepository =
                userRepository;

        this.userProfileMapper =
                userProfileMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(
            String email,
            String providerLabel) {

        User user =
                userRepository
                        .findByEmailIgnoreCase(email)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "User not found"
                                        )
                        );

        return userProfileMapper.toResponse(
                user,
                providerLabel
        );
    }
}