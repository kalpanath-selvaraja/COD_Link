package com.ijse.codlink.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class TrackingEventDTO {
    @jakarta.validation.constraints.Positive
    private Long eventId;
    @jakarta.validation.constraints.Positive
    @jakarta.validation.constraints.NotNull
    private Long parcelId;
    @jakarta.validation.constraints.Size(max = 240)
    @jakarta.validation.constraints.NotBlank
    private String status;
    private LocalDateTime eventTime;
}
