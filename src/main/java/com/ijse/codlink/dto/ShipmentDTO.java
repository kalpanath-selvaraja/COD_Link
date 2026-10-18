package com.ijse.codlink.dto;

import com.ijse.codlink.enumeration.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentDTO {

    @jakarta.validation.constraints.Positive
    private Long shipmentId;
    @jakarta.validation.constraints.Positive
    @jakarta.validation.constraints.NotNull
    private Long customerId;
    private LocalDateTime createdAt;
    private ShipmentStatus status;

    public ShipmentDTO(Long customerId) {
        this.customerId = customerId;
    }
}
