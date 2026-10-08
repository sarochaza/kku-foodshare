package com.kku.foodshare.repository;

import com.kku.foodshare.domain.entity.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {}
