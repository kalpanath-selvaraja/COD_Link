package com.ijse.codlink.dto;

import com.ijse.codlink.enumeration.ParcelStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PublicTrackingDTO {
    private String trackingNumber;
    private ParcelStatus status;
    private String originBranchName;
    private String currentBranchName;
}
