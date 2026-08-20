package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.PaymentDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse create(@RequestBody PaymentDTO dto) {
        return new CommonResponse(0, paymentService.create(dto), "Payment recorded");
    }

    @GetMapping(value = "/invoice/{invoiceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getByInvoice(@PathVariable Long invoiceId) {
        return new CommonResponse(0, paymentService.getByInvoiceId(invoiceId), "Payments fetched");
    }
}

