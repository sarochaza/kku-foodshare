package com.kku.foodshare.controller.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HomeControllerTest {

  // Root stays on the public landing page regardless of authentication state.
  @Test
  void rootShouldReturnLandingPage() {

    HomeController controller = new HomeController();

    String viewName = controller.home();

    assertEquals("home", viewName);
  }

  @Test
  void aboutShouldReturnAboutTemplate() {
    HomeController controller = new HomeController();

    String viewName = controller.about();

    assertEquals("about", viewName);
  }
}
