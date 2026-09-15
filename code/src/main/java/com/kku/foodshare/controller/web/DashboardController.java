package com.kku.foodshare.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    // เปิดหน้า Dashboard
    @GetMapping("/home")
    public String dashboard() {
        return "dashboard";
    }
}