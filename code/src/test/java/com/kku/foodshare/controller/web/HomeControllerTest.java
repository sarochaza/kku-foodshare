package com.kku.foodshare.controller.web;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HomeControllerTest {

    // =========================================================
    // TEST 1: ทดสอบว่า HomeController เปิดหน้า home.html ถูกต้อง
    // ผ่านเมื่อ: controller.home() คืนค่า "home"
    // =========================================================
    @Test
    void homeShouldReturnHomeTemplate() {

        HomeController controller =
                new HomeController();

        String viewName =
                controller.home();

        assertEquals(
                "home",
                viewName
        );
    }


    // =========================================================
    // TEST 2: ทดสอบว่า Home Dashboard มีส่วน "รายการอาหารใกล้คุณ"
    // ผ่านเมื่อ: home.html มีข้อความ "รายการอาหารใกล้คุณ"
    //
    // ตอนนี้คาดว่า FAIL (RED)
    // เพราะเรายังไม่ได้สร้าง Home Dashboard เวอร์ชันใหม่
    // =========================================================
    @Test
    void homeTemplateShouldContainNearbyFoodSection()
            throws Exception {

        String html = Files.readString(
                Path.of(
                    "src/main/resources/templates/home.html"
                )
        );

        assertTrue(
                html.contains("รายการอาหารใกล้คุณ")
        );
    }
}