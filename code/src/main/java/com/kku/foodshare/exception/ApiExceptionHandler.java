package com.kku.foodshare.exception;

import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice(basePackages = "com.kku.foodshare.controller.api")
public class ApiExceptionHandler {
  @ExceptionHandler(Problem.class)
  ResponseEntity<?> problem(Problem p) {
    return error(p.status, p.getMessage(), Map.of());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<?> invalid(MethodArgumentNotValidException e) {
    Map<String, String> fields = new LinkedHashMap<>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(f -> fields.putIfAbsent(f.getField(), f.getDefaultMessage()));
    return error(400, "กรุณาตรวจสอบข้อมูลที่กรอก", fields);
  }

  @ExceptionHandler({
    IllegalArgumentException.class,
    org.springframework.http.converter.HttpMessageNotReadableException.class,
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
  })
  ResponseEntity<?> invalidValue(Exception e) {
    return error(400, "ข้อมูลไม่ถูกต้อง กรุณาตรวจสอบอีกครั้ง", Map.of());
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  ResponseEntity<?> large(Exception e) {
    return error(413, "รูปภาพต้องมีขนาดไม่เกิน 5 MB", Map.of());
  }

  @ExceptionHandler({
    DataIntegrityViolationException.class,
    org.springframework.dao.OptimisticLockingFailureException.class,
    org.springframework.dao.PessimisticLockingFailureException.class
  })
  ResponseEntity<?> conflict(Exception e) {
    return error(409, "ข้อมูลเปลี่ยนไปแล้ว กรุณาโหลดใหม่แล้วลองอีกครั้ง", Map.of());
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<?> unexpected(Exception e) {
    org.slf4j.LoggerFactory.getLogger(getClass()).error("Request failed", e);
    return error(500, "ทำรายการไม่สำเร็จ กรุณาลองอีกครั้ง", Map.of());
  }

  private ResponseEntity<?> error(int status, String message, Map<String, String> fields) {
    return ResponseEntity.status(status)
        .body(Map.of("status", status, "message", message, "fields", fields));
  }
}
