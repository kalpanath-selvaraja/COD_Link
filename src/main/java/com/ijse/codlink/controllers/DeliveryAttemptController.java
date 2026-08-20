package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.DeliveryAttemptDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.DeliveryAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/delivery-attempts")
@RequiredArgsConstructor
public class DeliveryAttemptController {

    private final DeliveryAttemptService deliveryAttemptService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse create(@RequestBody DeliveryAttemptDTO dto) {
        return new CommonResponse(0, deliveryAttemptService.create(dto), "Delivery attempt recorded");
    }

    @GetMapping(value = "/parcel/{parcelId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getByParcel(@PathVariable Long parcelId) {
        return new CommonResponse(0, deliveryAttemptService.getByParcelId(parcelId), "Delivery attempts fetched");
    }
}