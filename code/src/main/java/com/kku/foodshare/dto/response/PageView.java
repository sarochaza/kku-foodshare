package com.kku.foodshare.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageView<T>(List<T> items, int page, int totalPages, long totalElements) {
  public static <T> PageView<T> of(Page<T> page) {
    return new PageView<>(
        page.getContent(), page.getNumber(), page.getTotalPages(), page.getTotalElements());
  }
}
