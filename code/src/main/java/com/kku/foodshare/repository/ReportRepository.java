package com.kku.foodshare.repository;

import com.kku.foodshare.domain.entity.Report;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
  Page<Report> findAllByOrderByCreatedAtDesc(Pageable page);

  boolean existsByPostIdAndReporterIdAndStatus(Long post, Long user, String status);

  boolean existsByCommentIdAndReporterIdAndStatus(Long comment, Long user, String status);
}
