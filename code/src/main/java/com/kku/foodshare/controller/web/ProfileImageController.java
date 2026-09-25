package com.kku.foodshare.controller.web;

import com.kku.foodshare.dto.response.ProfileImageResponse;
import com.kku.foodshare.service.ProfileImageService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

@Controller
public class ProfileImageController {

    private final ProfileImageService profileImageService;

    public ProfileImageController(ProfileImageService profileImageService) {
        this.profileImageService = profileImageService;
    }

    @PostMapping("/account/photo")
    public String upload(
            Authentication authentication,
            @RequestParam("photo") MultipartFile photo,
            RedirectAttributes redirectAttributes
    ) {
        try {
            profileImageService.updateImage(
                    resolveEmail(authentication),
                    photo.getBytes()
            );

            redirectAttributes.addFlashAttribute(
                    "photoSuccess",
                    "เปลี่ยนรูปโปรไฟล์เรียบร้อยแล้ว"
            );
        } catch (IllegalArgumentException | IOException exception) {
            redirectAttributes.addFlashAttribute(
                    "photoError",
                    exception.getMessage()
            );
        }

        return "redirect:/account";
    }

    @GetMapping("/account/photo")
    public ResponseEntity<?> show(Authentication authentication) {
        Optional<ProfileImageResponse> image =
                profileImageService.getImage(
                        resolveEmail(authentication)
                );

        if (image.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/images/default-profile.png"))
                    .build();
        }

        ProfileImageResponse result = image.get();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.parseMediaType(result.contentType()))
                .body(result.imageData());
    }

    private String resolveEmail(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            return oauth2User.getAttribute("email");
        }

        return authentication.getName();
    }
}