package com.kku.foodshare.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Email is required")//หมายถึงห้ามปล่อยว่าง
    @Email(message = "Email format is invalid")//ช็กว่า email อยู่ในรูปแบบ email
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")//กำหนด password ขั้นต่ำ 6 ตัว
    private String password;

    @NotBlank(message = "Display name is required")
    private String displayName;

    public RegisterRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}