package com.kku.foodshare.security;

import com.kku.foodshare.exception.Problem;
import com.kku.foodshare.service.MemberService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class ActiveAccountFilter extends OncePerRequestFilter {
  private final MemberService members;

  public ActiveAccountFilter(MemberService members) {
    this.members = members;
  }

  protected void doFilterInternal(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    String email = CurrentIdentity.email(SecurityContextHolder.getContext().getAuthentication());
    if (email != null)
      try {
        members.require(email);
      } catch (Problem e) {
        SecurityContextHolder.clearContext();
        if (req.getSession(false) != null) req.getSession(false).invalidate();
        if (req.getRequestURI().startsWith("/api/")) {
          res.setStatus(403);
          res.setContentType("application/json;charset=UTF-8");
          res.getWriter().write("{\"status\":403,\"message\":\"บัญชีนี้ไม่สามารถใช้งานได้\"}");
        } else res.sendRedirect("/login?disabled");
        return;
      }
    chain.doFilter(req, res);
  }
}
