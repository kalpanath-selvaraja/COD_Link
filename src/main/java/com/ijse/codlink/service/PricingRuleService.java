package com.ijse.codlink.service;

import com.ijse.codlink.dto.PricingRuleDTO;

import java.util.List;

public interface PricingRuleService {

    PricingRuleDTO create(PricingRuleDTO dto);
    List<PricingRuleDTO> getByZoneId(Long zoneId);

    PricingRuleDTO update(Long ruleId, PricingRuleDTO dto);
    void delete(Long ruleId);
}
