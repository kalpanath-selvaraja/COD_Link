package com.ijse.codlink.repository;

import com.ijse.codlink.entity.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
    List<PricingRule> findByServiceZone_ZoneId(Long zoneId);
    List<PricingRule> findByServiceZone_ZoneIdAndIsCod(Long zoneId, Boolean isCod);
}
