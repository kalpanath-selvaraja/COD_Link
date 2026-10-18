package com.ijse.codlink.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceZoneDTO {
    @jakarta.validation.constraints.Positive
    private Long zoneId;
    @jakarta.validation.constraints.Size(max = 250)
    private String name;
}