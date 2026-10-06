package com.kku.foodshare.controller.api;

import com.kku.foodshare.domain.StockPolicy;
import com.kku.foodshare.security.CurrentIdentity;
import com.kku.foodshare.service.OwnerStockService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/food-posts/{id}/stock")
public class OwnerStockController {
  public record Change(@NotNull StockPolicy.Action action, @Min(1) @Max(10000) int amount,
      @NotNull @PositiveOrZero Long expectedVersion) {}

  private final OwnerStockService service;

  public OwnerStockController(OwnerStockService service) { this.service = service; }

  @GetMapping
  public OwnerStockService.Snapshot get(@PathVariable long id, Authentication a) {
    return service.get(CurrentIdentity.email(a), id);
  }

  @PostMapping
  public OwnerStockService.Snapshot change(@PathVariable long id, @Valid @RequestBody Change r,
      Authentication a) {
    return service.change(CurrentIdentity.email(a), id, r.action(), r.amount(), r.expectedVersion());
  }
}
