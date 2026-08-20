package com.ijse.codlink.dto;

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
    private String outcome;
    private LocalDateTime attemptedAt;
}