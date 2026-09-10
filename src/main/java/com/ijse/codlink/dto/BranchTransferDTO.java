package com.ijse.codlink.dto;

import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "parcelId is required")
    private Long parcelId;

    @NotNull(message = "fromBranchId is required")
    private Long fromBranchId;

    @NotNull(message = "toBranchId is required")
    private Long toBranchId;

    private LocalDateTime dispatchedAt;
}