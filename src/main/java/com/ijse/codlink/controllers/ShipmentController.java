package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.ShipmentDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "api/shipments")
@RequiredArgsConstructor
public class ShipmentController {
    private final com.ijse.codlink.security.RecordAccess recordAccess;
    private final ShipmentService shipmentService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> createShipment(@jakarta.validation.Valid @RequestBody ShipmentDTO shipmentDTO) {
        ShipmentDTO dto = shipmentService.create(shipmentDTO);
        CommonResponse response = new CommonResponse(0, dto, "Shipment created successfully");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping(value = "/customer/{customerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getShipmentByCustomer(@PathVariable Long customerId) {
        recordAccess.customer(customerId);
        List<ShipmentDTO> shipmentDTOList = shipmentService.getByCustomerId(customerId);
        CommonResponse response = new CommonResponse(0, shipmentDTOList, "Shipment fetched successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getById(@PathVariable Long id) {
        recordAccess.shipment(id);
        CommonResponse response = new CommonResponse(0, shipmentService.getById(id), "Shipment fetched");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<CommonResponse> delete(@PathVariable Long id) {
        shipmentService.delete(id);
        CommonResponse response = new CommonResponse(0, "Shipment deleted");
        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = "/{id}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> cancel(@PathVariable Long id) {
        CommonResponse response = new CommonResponse(0, shipmentService.cancel(id), "Shipment cancelled");
        return ResponseEntity.ok(response);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getAll() {
        CommonResponse response = new CommonResponse(0, shipmentService.getAll(), "Shipments fetched");
        return ResponseEntity.ok(response);
    }
}