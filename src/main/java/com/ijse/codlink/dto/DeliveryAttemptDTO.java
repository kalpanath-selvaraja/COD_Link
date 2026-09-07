package com.ijse.codlink.dto;

import com.ijse.codlink.enumeration.AttemptOutcome;
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
    private Long parcelId;
    private Long courierId;
    private AttemptOutcome outcomeCode;
    private String outcome;
    private LocalDateTime attemptedAt;
}