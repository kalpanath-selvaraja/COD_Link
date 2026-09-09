package com.ijse.codlink.service;

import com.ijse.codlink.dto.InvoiceDTO;
import com.ijse.codlink.entity.Parcel;

import java.math.BigDecimal;
import java.util.List;

public interface InvoiceService {
    InvoiceDTO create(InvoiceDTO dto);
    InvoiceDTO getByShipmentId(Long shipmentId);
    BigDecimal calculateAmount(List<Parcel> parcels);

    List<InvoiceDTO> getAll();
}