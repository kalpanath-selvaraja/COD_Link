package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.DeliveryAttemptDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.DeliveryAttemptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/delivery-attempts")
@RequiredArgsConstructor
public class DeliveryAttemptController {

    private final DeliveryAttemptService deliveryAttemptService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> create(@Valid @RequestBody DeliveryAttemptDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        CommonResponse response = new CommonResponse(0, deliveryAttemptService.create(principal.getUsername(), dto), "Delivery attempt recorded");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping(value = "/parcel/{parcelId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getByParcel(@PathVariable Long parcelId) {
        CommonResponse response = new CommonResponse(0, deliveryAttemptService.getByParcelId(parcelId), "Delivery attempts fetched");
        return ResponseEntity.ok(response);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getAll() {
        CommonResponse response = new CommonResponse(0, deliveryAttemptService.getAll(), "Delivery attempts fetched");
        return ResponseEntity.ok(response);
    }
}