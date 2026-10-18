package com.ijse.codlink.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {
    private String requestReference;
    private Long collectionId;
    private Long paymentId;
    private Long invoiceId;
    private String method;
    private BigDecimal amount;

    private LocalDateTime paidAt;

}