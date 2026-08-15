package com.ijse.codlink.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogDTO {
    private Long logId;
    private Long userId;
    private String action;
    private LocalDateTime logTime;

    public AuditLogDTO(Long userId, String action, LocalDateTime logTime) {
        this.userId = userId;
        this.action = action;
        this.logTime = logTime;
    }
}
