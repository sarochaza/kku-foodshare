package com.kku.foodshare.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kku.foodshare.domain.entity.FoodPost;
import com.kku.foodshare.domain.entity.FoodPostStatus;
import com.kku.foodshare.domain.entity.User;
import com.kku.foodshare.dto.response.AccountPageResponse;
import com.kku.foodshare.mapper.AccountMapper;
import com.kku.foodshare.repository.FoodPostRepository;
import com.kku.foodshare.repository.UserRepository;
import com.kku.foodshare.service.AccountService;

@Service
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final FoodPostRepository foodPostRepository;
    private final AccountMapper accountMapper;

    public AccountServiceImpl(
            UserRepository userRepository,
            FoodPostRepository foodPostRepository,
            AccountMapper accountMapper
    ) {
        this.userRepository = userRepository;
        this.foodPostRepository = foodPostRepository;
        this.accountMapper = accountMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public AccountPageResponse getAccount(
            String email,
            String providerLabel
    ) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("ไม่พบบัญชีผู้ใช้")
                );

        List<FoodPost> posts =
                foodPostRepository.findByOwnerIdOrderByCreatedAtDesc(
                        user.getId()
                );

        long availablePosts = posts.stream()
                .filter(post ->
                        post.getStatus() == FoodPostStatus.AVAILABLE
                        || post.getStatus() == FoodPostStatus.LOW_STOCK
                )
                .count();

        long sharedPosts = posts.stream()
                .filter(post ->
                        post.getStatus() == FoodPostStatus.CLAIMED
                )
                .count();

        return accountMapper.toResponse(
                user,
                providerLabel,
                posts,
                availablePosts,
                sharedPosts
        );
    }
}