package com.kku.foodshare.controller.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.kku.foodshare.dto.response.AccountPageResponse;
import com.kku.foodshare.service.AccountService;

@Controller
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping({"/account", "/account/posts"})
    public String account(Authentication authentication, Model model) {
        String email;
        String providerLabel;

        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            email = oauth2User.getAttribute("email");
            providerLabel = "บัญชี Google";
        } else {
            email = authentication.getName();
            providerLabel = "บัญชีอีเมล";
        }

        AccountPageResponse account =
                accountService.getAccount(email, providerLabel);

        model.addAttribute("account", account);

        return "account";
    }
}