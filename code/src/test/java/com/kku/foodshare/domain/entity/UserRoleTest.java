package com.kku.foodshare.domain.entity;

import com.kku.foodshare.domain.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserRoleTest {

    // ทดสอบว่าผู้ใช้ใหม่ได้รับสิทธิ์ USER เป็นค่าเริ่มต้น
    @Test
    void newUserShouldHaveUserRoleByDefault() {

        User user = new User();

        assertEquals(
                UserRole.USER,
                user.getRole()
        );
    }
}