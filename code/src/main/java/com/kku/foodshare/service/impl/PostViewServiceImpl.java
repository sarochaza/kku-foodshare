package com.kku.foodshare.service.impl;

import com.kku.foodshare.domain.entity.FoodPost;
import com.kku.foodshare.dto.response.*;
import com.kku.foodshare.mapper.PostViewMapping;
import com.kku.foodshare.repository.*;
import com.kku.foodshare.service.PostViewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Enriches post data within the service transaction; the mapper performs no database reads. */
@Service
@Transactional(readOnly = true)
public class PostViewServiceImpl implements PostViewService {
  private final FoodPostImageRepository images;
  private final PostCommentRepository comments;
  private final SavedPostRepository savedPosts;
  private final UserRepository users;
  private final PostViewMapping mapper;

  public PostViewServiceImpl(FoodPostImageRepository images, PostCommentRepository comments,
      SavedPostRepository savedPosts, UserRepository users, PostViewMapping mapper) {
    this.images = images; this.comments = comments; this.savedPosts = savedPosts;
    this.users = users; this.mapper = mapper;
  }

  @Override
  public PostView map(FoodPost post, String email, Double latitude, Double longitude) {
    var gallery = images.findAllByPostIdOrderBySortOrderAscIdAsc(post.getId()).stream()
        .map(image -> new PostImageView(image.id, "/media/" + image.filename, image.sortOrder)).toList();
    boolean saved = email != null && users.findByEmailIgnoreCase(email)
        .map(user -> savedPosts.existsByUserIdAndPostId(user.getId(), post.getId())).orElse(false);
    var context = new PostViewContext(gallery, (int) comments.countByPostIdAndDeletedAtIsNull(post.getId()), saved);
    return mapper.map(post, email, latitude, longitude, context);
  }
}
