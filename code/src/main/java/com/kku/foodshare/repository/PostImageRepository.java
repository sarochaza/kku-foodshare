package com.kku.foodshare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.kku.foodshare.domain.PostImage;
import java.util.List;


public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    List<PostImage> findByFoodPostPostIdOrderByDisplayOrderAsc(Long postId);
}
