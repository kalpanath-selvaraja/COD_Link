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
public class BranchTransferDTO {
    private Long transferId;
    private Long parcelId;
    private Long fromBranchId;
    private Long toBranchId;
    private LocalDateTime dispatchedAt;
}