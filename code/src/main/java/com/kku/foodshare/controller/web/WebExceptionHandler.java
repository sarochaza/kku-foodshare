package com.kku.foodshare.controller.web;

import com.kku.foodshare.exception.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(basePackages = "com.kku.foodshare.controller.web")
public class WebExceptionHandler {
  @ExceptionHandler(Problem.class)
  public ModelAndView problem(Problem p) {
    ModelAndView m = new ModelAndView("error");
    m.setStatus(HttpStatus.valueOf(p.status));
    m.addObject("message", p.getMessage());
    m.addObject("status", p.status);
    return m;
  }

  @ExceptionHandler(org.springframework.dao.OptimisticLockingFailureException.class)
  public ModelAndView concurrent(Exception e) {
    return problem(new Problem(409, "ข้อมูลเปลี่ยนไปแล้ว กรุณาโหลดหน้าใหม่แล้วลองอีกครั้ง"));
  }

  @ExceptionHandler(IllegalStateException.class)
  public ModelAndView unavailable(IllegalStateException e) {
    ModelAndView m = new ModelAndView("error");
    m.setStatus(HttpStatus.SERVICE_UNAVAILABLE);
    m.addObject("message", "บริการนี้ยังไม่พร้อม กรุณาลองใหม่หรือติดต่อผู้ดูแล");
    m.addObject("status", 503);
    return m;
  }
}
