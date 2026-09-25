package com.kku.foodshare.service;

import com.kku.foodshare.dto.response.AccountPageResponse;

public interface AccountService {

    AccountPageResponse getAccount(
            String email,
            String providerLabel
    );
}