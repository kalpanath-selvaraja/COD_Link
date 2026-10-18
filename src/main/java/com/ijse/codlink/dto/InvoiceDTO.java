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
public class InvoiceDTO {
    private Long invoiceId;
    private Long shipmentId;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private BigDecimal balance;
}