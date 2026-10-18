package com.ijse.codlink.dto;

import com.ijse.codlink.enumeration.VehicleStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class VehicleDTO {
    @jakarta.validation.constraints.Positive
    private Long vehicleId;
    @jakarta.validation.constraints.Size(max = 250)
    private String type;
    @jakarta.validation.constraints.Size(max = 250)
    private String plateNumber;
    @jakarta.validation.constraints.Positive
    private Long courierId;
    private VehicleStatus status;
}
