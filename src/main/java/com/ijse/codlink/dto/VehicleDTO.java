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
    private Long vehicleId;
    private String type;
    private String plateNumber;
    private Long courierId;
    private VehicleStatus status;
}
