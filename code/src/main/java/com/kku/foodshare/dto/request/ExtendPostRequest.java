package com.kku.foodshare.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/** A deliberately small request: an expired post may only receive a new closing time. */
public record ExtendPostRequest(@NotNull LocalDateTime availableUntil) {}
