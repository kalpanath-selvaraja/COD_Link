package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.AuditLogDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse createAuditLog(@RequestBody AuditLogDTO auditLogDTO) {
        auditLogService.createAuditLog(auditLogDTO);
        return new CommonResponse(0,"audit log created successfully");
    }

    @GetMapping(value = "/user/{userId}")
    public CommonResponse getLogByUser(@PathVariable Long userId) {
        List<AuditLogDTO> auditLogs= auditLogService.getByUserId(userId);
        return new CommonResponse(0,auditLogs ,"Audit logs");
    }

}
