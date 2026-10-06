package com.kku.foodshare.exception;

public class Problem extends RuntimeException {
  public final int status;

  public Problem(int status, String message) {
    super(message);
    this.status = status;
  }

  public static Problem missing() {
    return new Problem(404, "ไม่พบรายการที่ต้องการ");
  }

  public static Problem forbidden() {
    return new Problem(403, "คุณไม่มีสิทธิ์ทำรายการนี้");
  }

  public static Problem conflict(String message) {
    return new Problem(409, message);
  }
}
