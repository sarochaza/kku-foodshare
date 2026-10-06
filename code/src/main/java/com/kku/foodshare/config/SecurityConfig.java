package com.kku.foodshare.config;

import com.kku.foodshare.security.GoogleOAuth2SuccessHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      GoogleOAuth2SuccessHandler handler,
      ObjectProvider<ClientRegistrationRepository> clients,
      com.kku.foodshare.service.MemberService members)
      throws Exception {
    http.authorizeHttpRequests(
            a ->
                a.requestMatchers("/posts/new")
                    .authenticated()
                    .requestMatchers(
                        "/",
                        "/explore",
                        "/posts/{id}",
                        "/login",
                        "/register",
                        "/forgot-password",
                        "/reset-password",
                        "/fonts/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/videos/**",
                        "/vendor/**",
                        "/media/**",
                        "/error",
                        "/oauth2/**",
                        "/login/oauth2/**",
                        "/actuator/health",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/food-posts",
                        "/api/v1/food-posts/map",
                        "/api/v1/food-posts/{id}",
                        "/api/v1/stats")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .formLogin(
            f ->
                f.loginPage("/login")
                    .usernameParameter("email")
                    .defaultSuccessUrl("/home", true)
                    .permitAll())
        .logout(l -> l.logoutSuccessUrl("/").permitAll())
        .exceptionHandling(
            e ->
                e.defaultAuthenticationEntryPointFor(
                        (req, res, ex) -> {
                          res.setStatus(401);
                          res.setContentType("application/json;charset=UTF-8");
                          res.getWriter()
                              .write("{\"status\":401,\"message\":\"กรุณาเข้าสู่ระบบ\"}");
                        },
                        request -> request.getRequestURI().startsWith("/api/"))
                    .defaultAuthenticationEntryPointFor(
                        new org.springframework.security.web.authentication
                            .LoginUrlAuthenticationEntryPoint("/login"),
                        request -> !request.getRequestURI().startsWith("/api/")));
    if (clients.getIfAvailable() != null)
      http.oauth2Login(o -> o.loginPage("/login").successHandler(handler));
    http.addFilterAfter(
        new com.kku.foodshare.security.ActiveAccountFilter(members),
        org.springframework.security.web.authentication.AnonymousAuthenticationFilter.class);
    return http.build();
  }
}
