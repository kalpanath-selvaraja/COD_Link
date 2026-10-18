package com.ijse.codlink.service;

import com.ijse.codlink.dto.PaymentDTO;

import java.util.List;

public interface PaymentService {
    PaymentDTO create(PaymentDTO dto);
    List<PaymentDTO> getByInvoiceId(Long invoiceId);


    PaymentDTO createFromCollection(com.ijse.codlink.entity.CodCollection collection);

    List<PaymentDTO> getAll();
}