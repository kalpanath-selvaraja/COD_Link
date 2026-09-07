package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.ParcelDTO;
import com.ijse.codlink.entity.Address;
import com.ijse.codlink.repository.ParcelRepository;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.ParcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
    public CommonResponse createMine(@RequestBody ParcelDTO parcelDTO) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal =  (UserDetails) authentication.getPrincipal();


        ParcelDTO createdParcel = parcelService.createMine(principal.getUsername(), parcelDTO);
        return new CommonResponse(0,createdParcel, "Parcel booked");
    }


    @PostMapping(value = "for-customer/{customerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse createForCustomer(@PathVariable Long customerId , @RequestBody ParcelDTO parcelDTO) {
        ParcelDTO forCustomer = parcelService.createForCustomer(customerId, parcelDTO);

        return new CommonResponse(0,forCustomer, "Parcel booked");
    }
    @PostMapping(value = "/for-customer/{customerId}/cart", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse bookCartForWalkIn(@PathVariable Long customerId, @RequestBody List<ParcelDTO> parcelDtos) {
        List<ParcelDTO> booked = parcelService.bookCartForWalkIn(customerId, parcelDtos);
        return new CommonResponse(0, booked, "Parcels booked and invoiced successfully");
    }


    @GetMapping(value = "/track/{trackingNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse track(@PathVariable String trackingNumber) {
         return new CommonResponse(0, parcelService.getByTrackingNumber(trackingNumber) , "Parcel found");
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getById(@PathVariable Long id) {
        return new CommonResponse(0, parcelService.getById(id) , "Parcel found");
    }

    @GetMapping(value = "/customer/{customerId}" , produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getByCustomerId(@PathVariable Long customerId) {

        List<ParcelDTO> parcelDTOList = parcelService.getByCustomerId(customerId);
        return new CommonResponse(0, parcelDTOList , "Parcel found");
    }

    @PatchMapping(value = "/{id}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse updateStatus(@PathVariable Long id, @RequestBody ParcelDTO parcelDTO) {
        return new CommonResponse(0, parcelService.updateStatus(id, parcelDTO) , "Parcel status updated");
    }

    @DeleteMapping(value = "/{id}")
    public CommonResponse delete(@PathVariable Long id) {
        parcelService.delete(id);
        return new CommonResponse(0, "Parcel deleted");
    }


    @PatchMapping(value = "/{id}/shipment/{shipmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse assignToShipment(@PathVariable Long id, @PathVariable Long shipmentId) {
        return new CommonResponse(0, parcelService.assignToShipment(id, shipmentId), "Parcel attached to shipment");
    }


    @PostMapping(value = "/me/cart", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse bookCart(@RequestBody List<ParcelDTO> parcelDtos) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();

        List<ParcelDTO> booked = parcelService.bookCartForCustomer(principal.getUsername(), parcelDtos);
        return new CommonResponse(0, booked, "Parcels booked and invoiced successfully");
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getAll() {
        return new CommonResponse(0, parcelService.getAll(), "Parcels fetched");
    }
}
