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
    @jakarta.validation.constraints.Positive
    private Long transferId;

    @NotNull(message = "parcelId is required")
    @jakarta.validation.constraints.Positive
    private Long parcelId;

    @NotNull(message = "fromBranchId is required")
    @jakarta.validation.constraints.Positive
    private Long fromBranchId;

    @NotNull(message = "toBranchId is required")
    @jakarta.validation.constraints.Positive
    private Long toBranchId;

    private LocalDateTime dispatchedAt;
}