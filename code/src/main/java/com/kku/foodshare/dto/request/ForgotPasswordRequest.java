package com.kku.foodshare.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordRequest {

    @NotBlank(
        message = "กรุณากรอกอีเมล"
    )
    @Email(
        message = "รูปแบบอีเมลไม่ถูกต้อง"
    )
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(
            String email) {
        this.email = email;
    }
}