package com.ijse.codlink.service;

import com.ijse.codlink.dto.PaymentDTO;

import java.util.List;

public interface PaymentService {
    PaymentDTO create(PaymentDTO dto);
    List<PaymentDTO> getByInvoiceId(Long invoiceId);


    PaymentDTO createAuto(Long invoiceId, String method);

    List<PaymentDTO> getAll();
}