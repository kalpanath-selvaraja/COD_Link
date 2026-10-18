package com.ijse.codlink.dto;

import com.ijse.codlink.enumeration.ParcelStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PublicTrackingDTO {
    @jakarta.validation.constraints.Size(max = 250)
    private String trackingNumber;
    private ParcelStatus status;
    @jakarta.validation.constraints.Size(max = 250)
    private String originBranchName;
    @jakarta.validation.constraints.Size(max = 250)
    private String currentBranchName;
}
