package com.kku.foodshare.dto.request;

import com.kku.foodshare.domain.entity.FoodCategory;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FoodPostRequest(
    @NotBlank @Size(max = 150) String title,
    @NotBlank @Size(max = 1000) String description,
    @NotNull FoodCategory category,
    @NotNull @Min(1) @Max(10000) Integer quantity,
    @NotBlank @Size(max = 50) String unit,
    @NotBlank @Size(max = 255) String pickupLocationName,
    @NotNull @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,
    @NotNull @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude,
    @NotNull LocalDateTime availableFrom,
    @NotNull LocalDateTime availableUntil,
    @Size(max = 500) String allergens,
    @Min(1) @Max(10000) Integer maxPerPerson) {}
