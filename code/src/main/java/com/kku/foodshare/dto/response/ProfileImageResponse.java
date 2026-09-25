package com.kku.foodshare.dto.response;

public record ProfileImageResponse(
        String contentType,
        byte[] imageData
) {
}