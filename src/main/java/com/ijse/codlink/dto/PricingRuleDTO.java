package com.ijse.codlink.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PricingRuleDTO {
    @jakarta.validation.constraints.Positive
    private Long ruleId;
    @jakarta.validation.constraints.Positive
    private Long zoneId;
    private BigDecimal rate;
    @jakarta.validation.constraints.Size(max = 250)
    private String weightBracket;
    private Boolean isCod;

    public PricingRuleDTO(Long zoneId, BigDecimal rate, String weightBracket, Boolean isCod) {
        this.zoneId = zoneId;
        this.rate = rate;
        this.weightBracket = weightBracket;
        this.isCod = isCod;
    }
}
