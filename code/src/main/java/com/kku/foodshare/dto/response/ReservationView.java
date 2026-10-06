package com.kku.foodshare.dto.response;

import java.time.LocalDateTime;

public record ReservationView(
    Long id,
    PostView post,
    int quantity,
    String status,
    String memberName,
    String pickupCode,
    boolean owner,
    LocalDateTime createdAt) {}
