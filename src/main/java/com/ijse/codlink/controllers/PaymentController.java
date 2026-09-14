package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.PaymentDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> create(@RequestBody PaymentDTO dto) {
        CommonResponse response = new CommonResponse(0, paymentService.create(dto), "Payment recorded");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping(value = "/invoice/{invoiceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getByInvoice(@PathVariable Long invoiceId) {
        CommonResponse response = new CommonResponse(0, paymentService.getByInvoiceId(invoiceId), "Payments fetched");
        return ResponseEntity.ok(response);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getAll() {
        CommonResponse response = new CommonResponse(0, paymentService.getAll(), "payments fetched");
        return ResponseEntity.ok(response);
    }
}