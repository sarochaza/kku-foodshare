package com.kku.foodshare.controller.web;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardControllerTest {

    // ทดสอบว่าเปิดหน้า Dashboard ถูกต้อง
    @Test
    void dashboardShouldReturnDashboardTemplate() {

        DashboardController controller =
                new DashboardController();

        String viewName =
                controller.dashboard();

        assertEquals("dashboard", viewName);
    }

    // ทดสอบว่า Dashboard มีรายการอาหารใกล้คุณ
    @Test
    void dashboardShouldContainNearbyFoodSection() throws Exception {

        String html = Files.readString(
                Path.of("src/main/resources/templates/dashboard.html")
        );

        assertTrue(
                html.contains("รายการอาหารใกล้คุณ")
        );
    }
}