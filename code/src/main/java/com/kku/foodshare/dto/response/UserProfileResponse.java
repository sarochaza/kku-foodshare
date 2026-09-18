package com.kku.foodshare.dto.response;

public class UserProfileResponse {

    private final String displayName;
    private final String email;
    private final String providerLabel;

    public UserProfileResponse(
            String displayName,
            String email,
            String providerLabel) {

        this.displayName = displayName;
        this.email = email;
        this.providerLabel = providerLabel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getProviderLabel() {
        return providerLabel;
    }
}