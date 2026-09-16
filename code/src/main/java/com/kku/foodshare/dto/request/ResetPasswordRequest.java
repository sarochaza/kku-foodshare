package com.kku.foodshare.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {

    @NotBlank(
        message =
            "ไม่พบ Token สำหรับเปลี่ยนรหัสผ่าน"
    )
    private String token;

    @NotBlank(
        message =
            "กรุณากรอกรหัสผ่านใหม่"
    )
    @Size(
        min = 8,
        message =
            "รหัสผ่านต้องมีอย่างน้อย 8 ตัวอักษร"
    )
    private String password;

    @NotBlank(
        message =
            "กรุณายืนยันรหัสผ่านใหม่"
    )
    private String confirmPassword;

    /*
     * ตรวจว่ารหัสผ่านสองช่องตรงกัน
     */
    @AssertTrue(
        message =
            "รหัสผ่านทั้งสองช่องไม่ตรงกัน"
    )
    public boolean isPasswordMatching() {

        return password != null
                && password.equals(
                        confirmPassword
                );
    }

    public String getToken() {
        return token;
    }

    public void setToken(
            String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(
            String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(
            String confirmPassword) {
        this.confirmPassword =
                confirmPassword;
    }
}