package com.kku.foodshare.dto.response;

import java.util.List;

/** Data needed by the mapper, loaded by the service layer. */
public record PostViewContext(List<PostImageView> gallery, int commentCount, boolean saved) {
  public PostViewContext { gallery = List.copyOf(gallery); }
}
