package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.PricingRuleDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.PricingRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/pricing-rules")
@RequiredArgsConstructor
public class PricingRuleController {

    private final PricingRuleService pricingRuleService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> create(@jakarta.validation.Valid @RequestBody PricingRuleDTO dto) {
        pricingRuleService.create(dto);
        CommonResponse response = new CommonResponse(0, "Pricing rule created");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping(value = "/zone/{zoneId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getByZone(@PathVariable Long zoneId) {
        CommonResponse response = new CommonResponse(0, pricingRuleService.getByZoneId(zoneId), "Pricing rules fetched");
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> update(@PathVariable Long id, @jakarta.validation.Valid @RequestBody PricingRuleDTO dto) {
        CommonResponse response = new CommonResponse(0, pricingRuleService.update(id, dto), "Pricing rule updated");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<CommonResponse> delete(@PathVariable Long id) {
        pricingRuleService.delete(id);
        CommonResponse response = new CommonResponse(0, "Pricing rule deleted");
        return ResponseEntity.ok(response);
    }
}