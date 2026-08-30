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
    private Long ruleId;
    private Long zoneId;
    private BigDecimal rate;
    private String weightBracket;
    private Boolean isCod;

    public PricingRuleDTO(Long zoneId, BigDecimal rate, String weightBracket, Boolean isCod) {
        this.zoneId = zoneId;
        this.rate = rate;
        this.weightBracket = weightBracket;
        this.isCod = isCod;
    }
}
