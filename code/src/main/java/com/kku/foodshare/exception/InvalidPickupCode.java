package com.kku.foodshare.exception;

public class InvalidPickupCode extends Problem {
  public InvalidPickupCode(int status, String message) {
    super(status, message);
  }
}
