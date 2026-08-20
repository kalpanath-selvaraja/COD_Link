package com.ijse.codlink.service;

import com.ijse.codlink.dto.InvoiceDTO;

public interface InvoiceService {
    InvoiceDTO create(InvoiceDTO dto);
    InvoiceDTO getByShipmentId(Long shipmentId);
}