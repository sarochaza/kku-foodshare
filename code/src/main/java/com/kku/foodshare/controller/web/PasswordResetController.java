package com.kku.foodshare.controller.web;

import com.kku.foodshare.dto.request.ForgotPasswordRequest;
import com.kku.foodshare.dto.request.ResetPasswordRequest;
import com.kku.foodshare.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PasswordResetController {

  private final PasswordResetService passwordResetService;

  private final com.kku.foodshare.service.RequestRateLimiter limiter;

  public PasswordResetController(
      PasswordResetService passwordResetService,
      com.kku.foodshare.service.RequestRateLimiter limiter) {
    this.limiter = limiter;

    this.passwordResetService = passwordResetService;
  }

  /*
   * แสดงหน้ากรอกอีเมล
   */
  @GetMapping("/forgot-password")
  public String showForgotPassword(Model model) {

    model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());

    return "forgot-password";
  }

  /*
   * รับอีเมลและส่งลิงก์รีเซ็ต
   */
  @PostMapping("/forgot-password")
  public String submitForgotPassword(
      @Valid @ModelAttribute ForgotPasswordRequest request,
      BindingResult bindingResult,
      jakarta.servlet.http.HttpServletRequest http,
      jakarta.servlet.http.HttpServletResponse response,
      Model model) {

    if (bindingResult.hasErrors()) {
      return "forgot-password";
    }

    try {
      limiter.passwordReset(http.getRemoteAddr());
      passwordResetService.requestReset(request.getEmail());
    } catch (com.kku.foodshare.exception.Problem failure) {
      if (failure.status != 429 && failure.status != 503) throw failure;
      response.setStatus(failure.status);
      model.addAttribute("resetError", failure.status == 429
          ? "ขอลิงก์บ่อยเกินไป กรุณารอสักครู่แล้วลองใหม่"
          : "ระบบส่งอีเมลยังไม่พร้อม กรุณาติดต่อผู้ดูแล");
      return "forgot-password";
    }

    /*
     * แสดงข้อความเดียวกันเสมอ
     * ไม่ว่าอีเมลจะมีในระบบหรือไม่
     */
    return "redirect:/forgot-password?sent";
  }

  /*
   * เปิดหน้าตั้งรหัสผ่านใหม่
   */
  @GetMapping("/reset-password")
  public String showResetPassword(@RequestParam(required = false) String token, Model model) {

    boolean validToken = passwordResetService.isTokenValid(token);

    ResetPasswordRequest request = new ResetPasswordRequest();

    request.setToken(token);

    model.addAttribute("resetPasswordRequest", request);

    model.addAttribute("validToken", validToken);

    return "reset-password";
  }

  /*
   * บันทึกรหัสผ่านใหม่
   */
  @PostMapping("/reset-password")
  public String submitResetPassword(
      @Valid @ModelAttribute ResetPasswordRequest request,
      BindingResult bindingResult,
      Model model) {

    boolean validToken = passwordResetService.isTokenValid(request.getToken());

    model.addAttribute("validToken", validToken);

    if (!bindingResult.hasErrors() && !validToken) {

      bindingResult.reject("token.invalid", "ลิงก์หมดอายุหรือถูกใช้งานแล้ว");
    }

    if (bindingResult.hasErrors()) {
      return "reset-password";
    }

    try {
      passwordResetService.resetPassword(request.getToken(), request.getPassword());
    } catch (IllegalArgumentException | org.springframework.dao.OptimisticLockingFailureException failure) {
      model.addAttribute("validToken", false);
      return "reset-password";
    }

    return "redirect:/login?resetSuccess";
  }
}
