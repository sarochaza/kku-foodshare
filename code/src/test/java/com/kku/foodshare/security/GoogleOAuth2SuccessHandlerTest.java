package com.kku.foodshare.security;

import com.kku.foodshare.service.GoogleUserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static org.mockito.Mockito.*;

class GoogleOAuth2SuccessHandlerTest {

    private GoogleUserService googleUserService;
    private GoogleOAuth2SuccessHandler successHandler;

    @BeforeEach
    void setUp() {
        googleUserService = mock(GoogleUserService.class);

        successHandler =
                new GoogleOAuth2SuccessHandler(
                        googleUserService
                );
    }

    @Test
    void shouldCreateOrFindUserAndRedirectHomeAfterGoogleLogin()
            throws Exception {

        OAuth2User oauth2User = mock(OAuth2User.class);
        OAuth2AuthenticationToken authentication =
                mock(OAuth2AuthenticationToken.class);

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        HttpServletResponse response =
                mock(HttpServletResponse.class);

        when(authentication.getPrincipal())
                .thenReturn(oauth2User);

        when(oauth2User.getAttribute("email"))
                .thenReturn("google@gmail.com");

        when(oauth2User.getAttribute("name"))
                .thenReturn("Google User");

        successHandler.onAuthenticationSuccess(
                request,
                response,
                authentication
        );

        verify(googleUserService)
                .findOrCreateGoogleUser(
                        "google@gmail.com",
                        "Google User"
                );

        verify(response)
                .sendRedirect("/");
    }
}