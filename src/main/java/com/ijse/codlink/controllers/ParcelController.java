package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.ParcelDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.ParcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "api/parcels")
@RequiredArgsConstructor
public class ParcelController {

    private final ParcelService parcelService;

    @PostMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> createMine(@RequestBody ParcelDTO parcelDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        ParcelDTO createdParcel = parcelService.createMine(principal.getUsername(), parcelDTO);
        CommonResponse response = new CommonResponse(0, createdParcel, "Parcel booked");
        return new ResponseEntity<>(response, HttpStatus.CREATED); //  201
    }

    @PostMapping(value = "/for-customer/{customerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> createForCustomer(@PathVariable Long customerId, @RequestBody ParcelDTO parcelDTO) {
        ParcelDTO forCustomer = parcelService.createForCustomer(customerId, parcelDTO);
        CommonResponse response = new CommonResponse(0, forCustomer, "Parcel booked");
        return new ResponseEntity<>(response, HttpStatus.CREATED); //  201
    }

    @PostMapping(value = "/for-customer/{customerId}/cart", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> bookCartForWalkIn(@PathVariable Long customerId, @RequestBody List<ParcelDTO> parcelDtos) {
        List<ParcelDTO> booked = parcelService.bookCartForWalkIn(customerId, parcelDtos);
        CommonResponse response = new CommonResponse(0, booked, "Parcels booked and invoiced successfully");
        return new ResponseEntity<>(response, HttpStatus.CREATED); //  201
    }

    @PostMapping(value = "/me/cart", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> bookCart(@RequestBody List<ParcelDTO> parcelDtos) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        List<ParcelDTO> booked = parcelService.bookCartForCustomer(principal.getUsername(), parcelDtos);
        CommonResponse response = new CommonResponse(0, booked, "Parcels booked and invoiced successfully");
        return new ResponseEntity<>(response, HttpStatus.CREATED); //  201
    }

    @GetMapping(value = "/track/{trackingNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> track(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(new CommonResponse(0, parcelService.getByTrackingNumber(trackingNumber), "Parcel found"));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new CommonResponse(0, parcelService.getById(id), "Parcel found"));
    }

    @GetMapping(value = "/customer/{customerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getByCustomerId(@PathVariable Long customerId) {
        List<ParcelDTO> parcelDTOList = parcelService.getByCustomerId(customerId);
        return ResponseEntity.ok(new CommonResponse(0, parcelDTOList, "Parcel found"));
    }

    @PatchMapping(value = "/{id}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> updateStatus(@PathVariable Long id, @RequestBody ParcelDTO parcelDTO) {
        return ResponseEntity.ok(new CommonResponse(0, parcelService.updateStatus(id, parcelDTO), "Parcel status updated"));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<CommonResponse> delete(@PathVariable Long id) {
        parcelService.delete(id);
        return ResponseEntity.ok(new CommonResponse(0, "Parcel deleted"));
    }

    @PatchMapping(value = "/{id}/shipment/{shipmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> assignToShipment(@PathVariable Long id, @PathVariable Long shipmentId) {
        return ResponseEntity.ok(new CommonResponse(0, parcelService.assignToShipment(id, shipmentId), "Parcel attached to shipment"));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getAll() {
        return ResponseEntity.ok(new CommonResponse(0, parcelService.getAll(), "Parcels fetched"));
    }
}