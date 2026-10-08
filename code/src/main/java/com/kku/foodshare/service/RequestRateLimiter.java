package com.kku.foodshare.service;

import com.kku.foodshare.exception.Problem;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;

/** Bounded, per-instance throttle; the deployment runs one application instance. */
@Service
public class RequestRateLimiter {
  private final Clock clock;
  private final Map<String, ArrayDeque<Instant>> requests = new HashMap<>();

  public RequestRateLimiter(Clock clock) {
    this.clock = clock;
  }

  public synchronized void passwordReset(String address) {
    Instant cutoff = clock.instant().minus(Duration.ofMinutes(15));
    requests
        .values()
        .forEach(
            q -> {
              while (!q.isEmpty() && !q.peekFirst().isAfter(cutoff)) q.removeFirst();
            });
    requests.entrySet().removeIf(e -> e.getValue().isEmpty());
    if (!requests.containsKey(address) && requests.size() >= 10000) throw limited();
    var q = requests.computeIfAbsent(address, k -> new ArrayDeque<>());
    if (q.size() >= 5) throw limited();
    q.addLast(clock.instant());
  }

  private Problem limited() {
    return new Problem(429, "ขอรีเซ็ตรหัสผ่านบ่อยเกินไป กรุณารอ 15 นาทีแล้วลองใหม่");
  }
}
