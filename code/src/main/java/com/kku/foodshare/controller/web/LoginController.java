package com.kku.foodshare.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    //Spring จะเรียก:login()แล้วไปแสดง template login.html
    public String login() {
        return "login";
    }
}