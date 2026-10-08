package com.kku.foodshare.service;

import com.kku.foodshare.domain.StockPolicy;

public interface OwnerStockService {
  record Snapshot(long postId, int quantity, int reservedQuantity, int collectedQuantity,
      int offlineQuantity, int availableQuantity, Long version, String unit) {}
  Snapshot get(String email, long id);
  Snapshot change(String email, long id, StockPolicy.Action action, int amount, Long expectedVersion);
}
