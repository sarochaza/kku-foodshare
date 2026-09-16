package com.kku.foodshare.service;

public interface PasswordResetService {

    // รับคำขอและส่งลิงก์รีเซ็ต
    void requestReset(String email);

    // ตรวจว่า Token ยังใช้ได้หรือไม่
    boolean isTokenValid(String rawToken);

    // เปลี่ยนรหัสผ่านและทำให้ Token ใช้ซ้ำไม่ได้
    void resetPassword(
            String rawToken,
            String newPassword
    );
}