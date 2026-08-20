package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.InvoiceDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse create(@RequestBody InvoiceDTO dto) {
        return new CommonResponse(0, invoiceService.create(dto), "Invoice created");
    }

    @GetMapping(value = "/shipment/{shipmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getByShipment(@PathVariable Long shipmentId) {
        return new CommonResponse(0, invoiceService.getByShipmentId(shipmentId), "Invoice fetched");
    }
}


