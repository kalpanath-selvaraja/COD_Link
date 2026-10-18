package com.ijse.codlink.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogDTO {
    @jakarta.validation.constraints.Positive
    private Long logId;
    @jakarta.validation.constraints.Positive
    @jakarta.validation.constraints.NotNull
    private Long userId;
    @jakarta.validation.constraints.Size(max = 250)
    @jakarta.validation.constraints.NotBlank
    private String action;
    private LocalDateTime logTime;

    public AuditLogDTO(Long userId, String action, LocalDateTime logTime) {
        this.userId = userId;
        this.action = action;
        this.logTime = logTime;
    }
}
