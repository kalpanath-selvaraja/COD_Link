package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.PricingRuleDTO;
import com.ijse.codlink.entity.PricingRule;
import com.ijse.codlink.entity.ServiceZone;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.PricingRuleRepository;
import com.ijse.codlink.repository.ServiceZoneRepository;
import com.ijse.codlink.service.PricingRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PricingRuleServiceImpl implements PricingRuleService {


    private final PricingRuleRepository pricingRuleRepository;
    private final ServiceZoneRepository serviceZoneRepository;

    // PricingRuleServiceImpl.java

    @Override
    public PricingRuleDTO create(PricingRuleDTO dto) {
        Optional<ServiceZone> optionalZone = serviceZoneRepository.findById(dto.getZoneId());
        if (optionalZone.isEmpty()) {
            throw new CustomException(404, "Service zone not found");
        }
        if (dto.getIsCod() == null) {
            throw new CustomException(400, "isCod is required");
        }

        PricingRule rule = new PricingRule();
        rule.setRate(dto.getRate());
        rule.setWeightBracket(dto.getWeightBracket());
        rule.setIsCod(dto.getIsCod());

        rule.setServiceZone(optionalZone.get());
        PricingRule saved = pricingRuleRepository.save(rule);

        return new PricingRuleDTO(saved.getRuleId(), saved.getServiceZone().getZoneId(),
                saved.getRate(), saved.getWeightBracket(), saved.getIsCod());
    }

    @Override
    public List<PricingRuleDTO> getByZoneId(Long zoneId) {
        Optional<ServiceZone> optionalZone = serviceZoneRepository.findById(zoneId);
        if (optionalZone.isEmpty()) {
            throw new CustomException(404, "Service zone not found");
        }

        List<PricingRule> pricingRuleList = pricingRuleRepository.findByServiceZone_ZoneId(zoneId);
        List<PricingRuleDTO> list = new ArrayList<>();

        for (PricingRule pricingRule : pricingRuleList) {
            PricingRuleDTO dto = new PricingRuleDTO();
            dto.setRuleId(pricingRule.getRuleId());
            dto.setZoneId(pricingRule.getServiceZone().getZoneId());
            dto.setRate(pricingRule.getRate());
            dto.setWeightBracket(pricingRule.getWeightBracket());
            dto.setIsCod(pricingRule.getIsCod());
            list.add(dto);
        }
        return list;
    }

    @Override
    public PricingRuleDTO update(Long ruleId, PricingRuleDTO dto) {
        Optional<PricingRule> optionalRule = pricingRuleRepository.findById(ruleId);
        if (optionalRule.isEmpty()) {
            throw new CustomException(404, "Pricing rule not found");
        }
        PricingRule rule = optionalRule.get();

        if (dto.getRate() != null) rule.setRate(dto.getRate());
        if (dto.getWeightBracket() != null) rule.setWeightBracket(dto.getWeightBracket());
        if (dto.getIsCod() != null) rule.setIsCod(dto.getIsCod());

        PricingRule saved = pricingRuleRepository.save(rule);

        return new PricingRuleDTO(saved.getRuleId(), saved.getServiceZone().getZoneId(),
                saved.getRate(), saved.getWeightBracket(), saved.getIsCod());
    }

    @Override
    public void delete(Long ruleId) {
        Optional<PricingRule> optionalRule = pricingRuleRepository.findById(ruleId);
        if (optionalRule.isEmpty()) {
            throw new CustomException(404, "Pricing rule not found");
        }
        pricingRuleRepository.delete(optionalRule.get());
    }



    @Override
    public BigDecimal calculateShippingFee(Long zoneId, Double weight, boolean isCod) {
        Optional<ServiceZone> optionalZone = serviceZoneRepository.findById(zoneId);
        if (optionalZone.isEmpty()) {
            throw new CustomException(404, "Service zone not found");
        }
        if (weight == null || weight <= 0) {
            throw new CustomException(400, "Weight must be greater than zero");
        }

        List<PricingRule> candidates = pricingRuleRepository.findByServiceZone_ZoneIdAndIsCod(zoneId, isCod);

        for (PricingRule rule : candidates) {
            if (weightInBracket(weight, rule.getWeightBracket())) {
                log.info("Pricing match: zone={}, weight={}kg, isCod={} -> rate={}", zoneId, weight, isCod, rule.getRate());
                return rule.getRate();
            }
        }

        log.warn("No pricing rule matched: zone={}, weight={}kg, isCod={}", zoneId, weight, isCod);
        throw new CustomException(404, "No pricing rule matches zone " + zoneId
                + ", weight " + weight + "kg, isCod=" + isCod
                + " — add a PricingRule row covering this combination");
    }


    private boolean weightInBracket(double weight, String bracket) {
        if (bracket == null || bracket.isBlank()) {
            return false;
        }
        if (bracket.endsWith("+")) {
            try {
                double min = Double.parseDouble(bracket.substring(0, bracket.length() - 1));
                return weight >= min;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        String[] parts = bracket.split("-");
        if (parts.length != 2) {
            return false;
        }
        try {
            double min = Double.parseDouble(parts[0]);
            double max = Double.parseDouble(parts[1]);
            return weight >= min && weight < max;
        } catch (NumberFormatException e) {
            return false;
        }
    }



}
