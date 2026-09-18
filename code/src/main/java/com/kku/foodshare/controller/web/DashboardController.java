package com.kku.foodshare.controller.web;

import com.kku.foodshare.dto.response.UserProfileResponse;
import com.kku.foodshare.service.UserProfileService;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final UserProfileService userProfileService;

    public DashboardController(
            UserProfileService userProfileService) {

        this.userProfileService =
                userProfileService;
    }

    @GetMapping("/home")
    public String dashboard(
            Authentication authentication,
            Model model) {

        String email =
                resolveEmail(authentication);

        String providerLabel =
                resolveProviderLabel(authentication);

        UserProfileResponse currentUser =
                userProfileService.getProfile(
                        email,
                        providerLabel
                );

        model.addAttribute(
                "currentUser",
                currentUser
        );

        return "dashboard";
    }

    private String resolveEmail(
            Authentication authentication) {

        Object principal =
                authentication.getPrincipal();

        if (principal instanceof OAuth2User oauth2User) {
            return oauth2User.getAttribute(
                    "email"
            );
        }

        return authentication.getName();
    }

    private String resolveProviderLabel(
            Authentication authentication) {

        if (authentication.getPrincipal()
                instanceof OAuth2User) {

            return "บัญชี Google";
        }

        return "บัญชีอีเมล";
    }
}