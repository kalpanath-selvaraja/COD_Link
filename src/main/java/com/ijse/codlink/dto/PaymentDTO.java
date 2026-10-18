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
    @jakarta.validation.constraints.Size(max = 250)
    private String requestReference;
    @jakarta.validation.constraints.Positive
    private Long collectionId;
    @jakarta.validation.constraints.Positive
    private Long paymentId;
    @jakarta.validation.constraints.Positive
    private Long invoiceId;
    @jakarta.validation.constraints.Size(max = 250)
    private String method;
    private BigDecimal amount;

    private LocalDateTime paidAt;

}