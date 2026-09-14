package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.InvoiceDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> create(@RequestBody InvoiceDTO dto) {
        CommonResponse response = new CommonResponse(0, invoiceService.create(dto), "Invoice created");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping(value = "/shipment/{shipmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getByShipment(@PathVariable Long shipmentId) {
        CommonResponse response = new CommonResponse(0, invoiceService.getByShipmentId(shipmentId), "Invoice fetched");
        return ResponseEntity.ok(response);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getAll() {
        CommonResponse response = new CommonResponse(0, invoiceService.getAll(), "Invoice fetched");
        return ResponseEntity.ok(response);
    }
}