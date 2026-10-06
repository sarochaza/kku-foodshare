package com.kku.foodshare.controller.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HomeControllerTest {

  // ทดสอบว่าเปิดหน้า Landing Page ถูกต้อง
  @Test
  void homeShouldReturnHomeTemplate() {

    HomeController controller = new HomeController();

    String viewName = controller.home();

    assertEquals("home", viewName);
  }
}
