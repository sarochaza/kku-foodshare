package com.kku.foodshare.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCommentRequest(
    @NotBlank(message = "กรุณาเขียนความคิดเห็น")
    @Size(max = 800, message = "ความคิดเห็นต้องไม่เกิน 800 ตัวอักษร") String body) {}
