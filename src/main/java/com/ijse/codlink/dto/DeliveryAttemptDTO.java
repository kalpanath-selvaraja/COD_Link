package com.ijse.codlink.dto;

import com.ijse.codlink.enumeration.AttemptOutcome;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryAttemptDTO {
    private Long attemptId;

    @NotNull(message = "parcelId is required")
    private Long parcelId;

    private Long courierId;

    @NotNull(message = "outcomeCode is required (DELIVERED, FAILED, or RESCHEDULED)")
    private AttemptOutcome outcomeCode;

    private String outcome;
    private LocalDateTime attemptedAt;
}