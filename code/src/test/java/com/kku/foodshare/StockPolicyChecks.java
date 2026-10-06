package com.kku.foodshare;

import com.kku.foodshare.domain.StockPolicy;

/** Dependency-free executable checks for the production stock rules. */
public class StockPolicyChecks {
  private static void equal(int expected, int actual) {
    if (expected != actual) throw new AssertionError(expected + " != " + actual);
  }

  private static void rejects(Runnable action) {
    try { action.run(); } catch (IllegalArgumentException expected) { return; }
    throw new AssertionError("Expected rejected stock change");
  }

  public static void main(String[] args) {
    var offline = StockPolicy.apply(10, 4, 2, 0, StockPolicy.Action.OFFLINE, 4);
    equal(10, offline.quantity()); equal(4, offline.offlineQuantity());
    rejects(() -> StockPolicy.apply(10, 4, 2, 0, StockPolicy.Action.OFFLINE, 5));
    rejects(() -> StockPolicy.apply(10, 4, 2, 1, StockPolicy.Action.REMOVE, 4));
    var remove = StockPolicy.apply(10, 4, 2, 1, StockPolicy.Action.REMOVE, 3);
    equal(7, remove.quantity()); equal(1, remove.offlineQuantity());
    rejects(() -> StockPolicy.apply(2, 0, 0, 0, StockPolicy.Action.REMOVE, 2));
    var add = StockPolicy.apply(5, 0, 2, 3, StockPolicy.Action.ADD, 2);
    equal(7, add.quantity()); equal(3, add.offlineQuantity());
    var undo = StockPolicy.apply(10, 4, 2, 4, StockPolicy.Action.UNDO_OFFLINE, 3);
    equal(1, undo.offlineQuantity());
    rejects(() -> StockPolicy.apply(10, 4, 2, 1, StockPolicy.Action.UNDO_OFFLINE, 2));
    rejects(() -> StockPolicy.apply(10000, 0, 0, 0, StockPolicy.Action.ADD, 1));
    rejects(() -> StockPolicy.apply(2, 0, 0, 0, StockPolicy.Action.ADD, 0));
    rejects(() -> StockPolicy.apply(2, 0, 0, 0, StockPolicy.Action.ADD, -1));
    rejects(() -> StockPolicy.apply(2, 0, 0, 0, StockPolicy.Action.ADD, Integer.MAX_VALUE));
    for (int total = 1; total <= 20; total++) {
      for (int reserved = 0; reserved <= total; reserved++) {
        for (int collected = 0; collected <= total - reserved; collected++) {
          int available = total - reserved - collected;
          if (available > 0) {
            var all = StockPolicy.apply(total, reserved, collected, 0, StockPolicy.Action.OFFLINE, available);
            equal(0, all.quantity() - reserved - collected - all.offlineQuantity());
          }
          final int t=total, r=reserved, c=collected, a=available;
          rejects(() -> StockPolicy.apply(t, r, c, 0, StockPolicy.Action.OFFLINE, a + 1));
        }
      }
    }
    System.out.println("StockPolicy: boundaries and exhaustive reserved-stock checks passed");
  }
}
