package com.ijse.codlink.service;

import com.ijse.codlink.dto.AuditLogDTO;

import java.util.List;

public interface AuditLogService {
    AuditLogDTO createAuditLog(AuditLogDTO auditLogDTO);
    List<AuditLogDTO> getByUserId(Long userId);
}
