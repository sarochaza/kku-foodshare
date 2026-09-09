package com.kku.foodshare.controller.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoginControllerTest {

    @Test
    void loginShouldReturnLoginTemplate() {

        LoginController controller = new LoginController();

        String viewName = controller.login();

        assertEquals("login", viewName);
    }
}