package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.InvoiceDTO;
import com.ijse.codlink.entity.Invoice;
import com.ijse.codlink.entity.Shipment;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.InvoiceRepository;
import com.ijse.codlink.repository.ShipmentRepository;
import com.ijse.codlink.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ShipmentRepository shipmentRepository;

    @Override
    public InvoiceDTO create(InvoiceDTO dto) {
        Optional<Shipment> optionalShipment = shipmentRepository.findById(dto.getShipmentId());
        if (optionalShipment.isEmpty()) {
            throw new CustomException(404, "Shipment not found");
        }
        if (invoiceRepository.existsByShipment_ShipmentId(dto.getShipmentId())) {
            throw new CustomException(409, "Invoice already exists for this shipment");
        }
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(400, "Amount must be greater than zero");
        }
        Invoice invoice = new Invoice();
        invoice.setShipment(optionalShipment.get());
        invoice.setAmount(dto.getAmount());
        return toDto(invoiceRepository.save(invoice));
    }

    @Override
    public InvoiceDTO getByShipmentId(Long shipmentId) {
        Optional<Invoice> optionalInvoice = invoiceRepository.findByShipment_ShipmentId(shipmentId);
        if (optionalInvoice.isEmpty()) {
            throw new CustomException(404, "No invoice found for this shipment");
        }
        return toDto(optionalInvoice.get());
    }

    private InvoiceDTO toDto(Invoice i) {
        return new InvoiceDTO(i.getInvoiceId(), i.getShipment().getShipmentId(), i.getAmount());
    }
}