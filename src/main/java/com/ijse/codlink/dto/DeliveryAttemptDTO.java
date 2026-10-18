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
    @jakarta.validation.constraints.Positive
    private Long attemptId;

    @NotNull(message = "parcelId is required")
    @jakarta.validation.constraints.Positive
    private Long parcelId;

    @jakarta.validation.constraints.Positive
    private Long courierId;

    @NotNull(message = "outcomeCode is required (DELIVERED, FAILED, or RESCHEDULED)")
    private AttemptOutcome outcomeCode;

    @jakarta.validation.constraints.Size(max = 250)
    private String outcome;
    private LocalDateTime attemptedAt;
}