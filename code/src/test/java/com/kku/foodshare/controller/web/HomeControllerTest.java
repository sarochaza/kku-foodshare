package com.kku.foodshare.controller.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HomeControllerTest {

    @Test
    void homeShouldReturnHomeTemplate() {
        HomeController controller = new HomeController();

        String viewName = controller.home();

        assertEquals("home", viewName);
    }
}