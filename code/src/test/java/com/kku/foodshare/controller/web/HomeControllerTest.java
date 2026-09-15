package com.kku.foodshare.controller.web;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HomeControllerTest {

    // ทดสอบว่าเปิดหน้า Landing Page ถูกต้อง
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

    // ทดสอบว่า Landing Page มี Hero หลัก
    @Test
    void homeTemplateShouldContainHeroSection()
            throws Exception {

        String html = Files.readString(
                Path.of(
                    "src/main/resources/templates/home.html"
                )
        );

        assertTrue(
                html.contains("แบ่งปันอาหาร")
        );
    }
    // ทดสอบว่า Landing Page มี Hero สำหรับ interaction
@Test
void homeTemplateShouldContainInteractiveHero()
        throws Exception {

    String html = Files.readString(
            Path.of(
                "src/main/resources/templates/home.html"
            )
    );

    assertTrue(
            html.contains("interactive-hero")
    );
}
// ทดสอบว่า Landing Page มีส่วนปัญหาอาหารเหลือ
@Test
void homeTemplateShouldContainProblemSection()
        throws Exception {

    String html = Files.readString(
            Path.of(
                "src/main/resources/templates/home.html"
            )
    );

    assertTrue(
            html.contains("problem-section")
    );
}

// ทดสอบว่า Problem มีภาพมาสคอต
@Test
void problemSectionShouldContainMascotImage()
        throws Exception {

    String html = Files.readString(
            Path.of(
                    "src/main/resources/templates/home.html"
            )
    );

    assertTrue(
            html.contains("mascot_landing.png")
    );
}
}