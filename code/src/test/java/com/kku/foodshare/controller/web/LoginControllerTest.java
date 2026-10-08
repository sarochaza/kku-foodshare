package com.kku.foodshare.controller.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LoginControllerTest {

  @Test
  void loginShouldReturnLoginTemplate() {

    LoginController controller = new LoginController();

    String viewName = controller.login();

    assertEquals("login", viewName);
  }
}
