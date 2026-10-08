package com.kku.foodshare.service.impl;

import com.kku.foodshare.service.*;

import com.kku.foodshare.domain.StockPolicy;
import com.kku.foodshare.domain.entity.*;
import com.kku.foodshare.exception.Problem;
import com.kku.foodshare.repository.FoodPostRepository;
import java.time.*;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OwnerStockServiceImpl implements OwnerStockService {
  private final FoodPostRepository posts;
  private final MemberService members;
  private final Clock clock;

  public OwnerStockServiceImpl(FoodPostRepository posts, MemberService members, Clock clock) {
    this.posts = posts; this.members = members; this.clock = clock;
  }

  private Snapshot view(FoodPost p) {
    return new Snapshot(p.getId(), p.getQuantity(), p.getReservedQuantity(), p.getCollectedQuantity(),
        p.getOfflineQuantity(), p.getAvailableQuantity(), p.getVersion(), p.getUnit());
  }

  private void owner(FoodPost p, User u) {
    if (!p.getOwner().getId().equals(u.getId())) throw Problem.forbidden();
  }

  @Transactional(readOnly = true)
  public Snapshot get(String email, long id) {
    User u = members.require(email);
    FoodPost p = posts.findById(id).orElseThrow(Problem::missing);
    owner(p, u);
    return view(p);
  }

  public Snapshot change(String email, long id, StockPolicy.Action action, int amount, Long version) {
    User u = members.require(email);
    // Same lock and lock order as reservation reserve/change/cancel/collection.
    FoodPost p = posts.lockById(id).orElseThrow(Problem::missing);
    owner(p, u);
    LocalDateTime now = LocalDateTime.now(clock);
    if (p.getStatus() == FoodPostStatus.CANCELLED || p.getStatus() == FoodPostStatus.EXPIRED
        || !p.getAvailableUntil().isAfter(now)) throw Problem.conflict("โพสต์นี้ปิดรับแล้ว");
    if (version == null || !Objects.equals(version, p.getVersion()))
      throw Problem.conflict("จำนวนเปลี่ยนไปแล้ว กรุณาตรวจยอดล่าสุดก่อนยืนยันอีกครั้ง");
    StockPolicy.Result result;
    try {
      result = StockPolicy.apply(p.getQuantity(), p.getReservedQuantity(), p.getCollectedQuantity(),
          p.getOfflineQuantity(), action, amount);
    } catch (IllegalArgumentException error) {
      throw Problem.conflict(error.getMessage());
    }
    p.setQuantity(result.quantity());
    p.setOfflineQuantity(result.offlineQuantity());
    p.setStatus(p.getCollectedQuantity() + p.getOfflineQuantity() == p.getQuantity()
        ? FoodPostStatus.CLAIMED : FoodPostStatus.AVAILABLE);
    p.setUpdatedAt(now);
    posts.saveAndFlush(p); // Return the new version, so stale/double requests cannot apply again.
    return view(p);
  }
}
