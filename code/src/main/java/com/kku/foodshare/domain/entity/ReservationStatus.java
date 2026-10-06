package com.kku.foodshare.domain.entity;

import com.kku.foodshare.domain.state.ReservationState;
import com.kku.foodshare.exception.Problem;

public enum ReservationStatus implements ReservationState {
  RESERVED {
    public void requireMutable() {}
  },
  COLLECTED,
  CANCELLED,
  EXPIRED;

  public void requireMutable() {
    throw Problem.conflict("การจองนี้สิ้นสุดแล้ว ไม่สามารถเปลี่ยนสถานะได้");
  }
}
