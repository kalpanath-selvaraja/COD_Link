package com.ijse.codlink.repository;

import com.ijse.codlink.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog,Long> {
    List<AuditLog> findByUser_UserId(Long id);
}
