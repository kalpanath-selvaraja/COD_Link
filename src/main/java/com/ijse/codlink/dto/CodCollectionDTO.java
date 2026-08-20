package com.ijse.codlink.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CodCollectionDTO {
    private Long collectionId;
    private Long parcelId;
    private Long courierId;
    private BigDecimal amount;
    private Long batchId;
}