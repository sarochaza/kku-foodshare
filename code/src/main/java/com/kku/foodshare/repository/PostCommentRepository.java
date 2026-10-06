package com.kku.foodshare.repository;

import com.kku.foodshare.domain.entity.PostComment;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
  Page<PostComment> findByPostIdAndDeletedAtIsNullOrderByCreatedAtAsc(Long postId, Pageable pageable);

  long countByPostIdAndDeletedAtIsNull(Long postId);

  Optional<PostComment> findByIdAndDeletedAtIsNull(Long id);
}
