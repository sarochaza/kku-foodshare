package com.kku.foodshare.repository;

import com.kku.foodshare.domain.entity.PostComment;
import java.util.Optional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
  Page<PostComment> findByPostIdAndDeletedAtIsNullOrderByCreatedAtAscIdAsc(Long postId, Pageable pageable);

  long countByPostIdAndDeletedAtIsNull(Long postId);

  Optional<PostComment> findByIdAndDeletedAtIsNull(Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select c from PostComment c where c.id = :id and c.deletedAt is null")
  Optional<PostComment> lockActive(@Param("id") Long id);
}
