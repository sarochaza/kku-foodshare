package com.kku.foodshare.service;

public interface EmailService {

    void sendPasswordResetEmail(
            String recipient,
            String resetUrl
    );
}