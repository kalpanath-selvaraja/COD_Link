package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.PricingRuleDTO;
import com.ijse.codlink.entity.PricingRule;
import com.ijse.codlink.entity.ServiceZone;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.PricingRuleRepository;
import com.ijse.codlink.repository.ServiceZoneRepository;
import com.ijse.codlink.service.PricingRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PricingRuleServiceImpl implements PricingRuleService {


    private final PricingRuleRepository pricingRuleRepository;
    private final ServiceZoneRepository serviceZoneRepository;

    @Override
    public PricingRuleDTO create(PricingRuleDTO dto) {
        Optional<ServiceZone> optionalZone = serviceZoneRepository.findById(dto.getZoneId());
        if (optionalZone.isEmpty()) {
            throw new CustomException(404,"Service zone not found");
        }

        PricingRule rule = new PricingRule();
        rule.setRate(dto.getRate());
        rule.setWeightBracket(dto.getWeightBracket());
        rule.setIsCod(dto.isCod());

        rule.setServiceZone(optionalZone.get());
        PricingRule saved = pricingRuleRepository.save(rule);

        return new PricingRuleDTO(saved.getRuleId(), saved.getServiceZone().getZoneId(),
                saved.getRate(), saved.getWeightBracket(), saved.getIsCod());
    }

    @Override
    public List<PricingRuleDTO> getByZoneId(Long zoneId) {

        Optional<ServiceZone> optionalZone = serviceZoneRepository.findById(zoneId);
        if (optionalZone.isEmpty()) {
            throw new CustomException(501,"Service zone not found");
        }


        List<PricingRule> pricingRuleList = pricingRuleRepository.findByServiceZone_ZoneId(zoneId);

        List<PricingRuleDTO> list = new ArrayList<>();

        for (PricingRule pricingRule : pricingRuleList) {
            PricingRuleDTO dto = new PricingRuleDTO();
            dto.setRuleId(pricingRule.getRuleId());
            dto.setZoneId(pricingRule.getServiceZone().getZoneId());
            dto.setRate(pricingRule.getRate());
            dto.setWeightBracket(pricingRule.getWeightBracket());
            dto.setCod(pricingRule.getIsCod());
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

        rule.setIsCod(dto.isCod());

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
}
