package com.kku.foodshare.mapper;

import com.kku.foodshare.domain.entity.*;
import com.kku.foodshare.dto.response.PostView;
import com.kku.foodshare.repository.FoodPostImageRepository;
import com.kku.foodshare.repository.PostCommentRepository;
import java.time.*;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PostViewMapper {
  private final FoodPostImageRepository images;
  private final PostCommentRepository comments;
  private final Clock clock;

  public PostViewMapper(FoodPostImageRepository images, PostCommentRepository comments, Clock clock) {
    this.images = images;
    this.comments = comments;
    this.clock = clock;
  }

  public PostView map(FoodPost p, String email, Double lat, Double lng) {
    String state = p.getStatus().name();
    if (!p.getAvailableUntil().isAfter(LocalDateTime.now(clock))
        && !state.equals("CANCELLED")
        && !state.equals("CLAIMED")) state = "EXPIRED";
    else if (state.equals("AVAILABLE") || state.equals("LOW_STOCK")) {
      if (p.getAvailableQuantity() == 0)
        state = p.getCollectedQuantity() + p.getOfflineQuantity() == p.getQuantity() ? "CLAIMED" : "FULL";
      else if (p.getAvailableFrom().isAfter(LocalDateTime.now(clock))) state = "SCHEDULED";
    }
    List<com.kku.foodshare.dto.response.PostImageView> gallery = images.findAllByPostIdOrderBySortOrderAscIdAsc(p.getId()).stream()
        .map(i -> new com.kku.foodshare.dto.response.PostImageView(i.id, "/media/" + i.filename, i.sortOrder)).toList();
    String image = gallery.isEmpty() ? null : gallery.get(0).url();
    Double distance = null;
    if (lat != null && lng != null) {
      double a =
          Math.pow(Math.sin(Math.toRadians(lat - p.getLatitude().doubleValue()) / 2), 2)
              + Math.cos(Math.toRadians(lat))
                  * Math.cos(Math.toRadians(p.getLatitude().doubleValue()))
                  * Math.pow(Math.sin(Math.toRadians(lng - p.getLongitude().doubleValue()) / 2), 2);
      distance = Math.round(6371 * 2 * Math.asin(Math.sqrt(Math.min(1, a))) * 10) / 10.0;
    }
    return new PostView(
        p.getId(),
        p.getTitle(),
        p.getDescription(),
        p.getCategory().name(),
        p.getQuantity(),
        p.getReservedQuantity(),
        p.getCollectedQuantity(),
        p.getAvailableQuantity(),
        p.getUnit(),
        p.getPickupLocationName(),
        p.getLatitude(),
        p.getLongitude(),
        p.getAvailableFrom(),
        p.getAvailableUntil(),
        state,
        p.getOwner().getDisplayName(),
        p.getOwner().getId(),
        p.getAllergens(),
        image,
        gallery,
        (int) comments.countByPostIdAndDeletedAtIsNull(p.getId()),
        p.getCreatedAt(),
        email != null && p.getOwner().getEmail().equalsIgnoreCase(email),
        distance,
        p.getOfflineQuantity(),
        p.getMaxPerPerson());
  }
}
