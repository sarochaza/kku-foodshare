package com.kku.foodshare.domain;

/** Pure quantity rules. The service must apply these under the post's write lock. */
public final class StockPolicy {
  private StockPolicy() {}

  public enum Action { ADD, REMOVE, OFFLINE, UNDO_OFFLINE }

  public record Result(int quantity, int offlineQuantity) {}

  public static Result apply(int total, int reserved, int collected, int offline,
      Action action, int amount) {
    if (action == null || amount < 1 || amount > 10000)
      throw new IllegalArgumentException("จำนวนต้องอยู่ระหว่าง 1 ถึง 10,000");
    int available = total - reserved - collected - offline;
    return switch (action) {
      case ADD -> {
        if (total + amount > 10000)
          throw new IllegalArgumentException("จำนวนทั้งหมดต้องไม่เกิน 10,000");
        yield new Result(total + amount, offline);
      }
      case REMOVE -> {
        if (amount > available)
          throw new IllegalArgumentException("ลดได้เฉพาะจำนวนที่ยังว่าง ต้องกันของให้ผู้จองไว้ก่อน");
        if (total - amount < 1)
          throw new IllegalArgumentException("หากนำของทั้งหมดออก กรุณาใช้ปุ่มปิดโพสต์แทน");
        yield new Result(total - amount, offline);
      }
      case OFFLINE -> {
        if (amount > available)
          throw new IllegalArgumentException("แจกนอกเว็บได้เฉพาะจำนวนที่ยังว่าง ต้องกันของให้ผู้จองไว้ก่อน");
        yield new Result(total, offline + amount);
      }
      case UNDO_OFFLINE -> {
        if (amount > offline)
          throw new IllegalArgumentException("แก้ยอดได้ไม่เกินจำนวนที่บันทึกว่าแจกนอกเว็บ");
        yield new Result(total, offline - amount);
      }
    };
  }
}
