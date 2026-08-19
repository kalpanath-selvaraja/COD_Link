package com.ijse.codlink.controllers;


import com.ijse.codlink.dto.PricingRuleDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.PricingRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/pricing-rules")
@RequiredArgsConstructor
public class PricingRuleController {

    private final PricingRuleService  pricingRuleService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse create(@RequestBody PricingRuleDTO dto) {
        pricingRuleService.create(dto);
        return new CommonResponse(0,  "Pricing rule created");
    }

    @GetMapping("/zone/{zoneId}")
    public CommonResponse getByZone(@PathVariable Long zoneId) {
        return new CommonResponse(0, pricingRuleService.getByZoneId(zoneId), "Pricing rules fetched");
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse update(@PathVariable Long id, @RequestBody PricingRuleDTO dto) {
        return new CommonResponse(0, pricingRuleService.update(id, dto), "Pricing rule updated");
    }

    @DeleteMapping(value = "/{id}")
    public CommonResponse delete(@PathVariable Long id) {
        pricingRuleService.delete(id);
        return new CommonResponse(0, "Pricing rule deleted");
    }
}
