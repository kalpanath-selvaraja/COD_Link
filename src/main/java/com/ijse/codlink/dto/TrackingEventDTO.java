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
    private Long eventId;
    private Long parcelId;
    private String status;
    private LocalDateTime eventTime;
}
