package com.kku.foodshare.config;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class SecurityConfigTest {

    // ทดสอบว่าเส้นทาง Admin อนุญาตเฉพาะผู้มี ROLE_ADMIN
    @Test
    void adminRoutesShouldRequireAdminRole()
            throws Exception {

        String source = Files.readString(
                Path.of(
                        "src/main/java/com/kku/foodshare/config/"
                                + "SecurityConfig.java"
                )
        );

        String compactSource =
                source.replaceAll(
                        "\\s+",
                        ""
                );

        assertTrue(
                compactSource.contains(
                        ".requestMatchers(\"/admin/**\")"
                                + ".hasRole(\"ADMIN\")"
                )
        );
    }
}