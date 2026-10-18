package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.InvoiceDTO;
import com.ijse.codlink.entity.Invoice;
import com.ijse.codlink.entity.Parcel;
import com.ijse.codlink.entity.Shipment;
import com.ijse.codlink.enumeration.ShipmentStatus;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.InvoiceRepository;
import com.ijse.codlink.repository.ParcelRepository;
import com.ijse.codlink.repository.ShipmentRepository;
import com.ijse.codlink.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@org.springframework.transaction.annotation.Transactional
@Slf4j
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final com.ijse.codlink.repository.PaymentRepository paymentRepository;
    private final ShipmentRepository shipmentRepository;
    private final ParcelRepository parcelRepository;

    @Override
    @org.springframework.transaction.annotation.Transactional
    public InvoiceDTO create(InvoiceDTO dto) {
        if (dto.getShipmentId() == null) throw new CustomException(400, "Shipment ID is required");
        log.info("Execute method create()");
        Optional<Shipment> optionalShipment = shipmentRepository.findLockedById(dto.getShipmentId());
        if (optionalShipment.isEmpty()) {
            throw new CustomException(404, "Shipment not found");
        }
        Shipment shipment = optionalShipment.get();

        if (shipment.getStatus() == ShipmentStatus.CANCELLED) {
            throw new CustomException(409, "Cannot invoice a cancelled shipment");
        }
        if (invoiceRepository.existsByShipment_ShipmentId(dto.getShipmentId())) {
            throw new CustomException(409, "Invoice already exists for this shipment");
        }

        List<Parcel> parcels = parcelRepository.findByShipment_ShipmentId(dto.getShipmentId());
        if (parcels.isEmpty()) {
            throw new CustomException(400, "Cannot invoice a shipment with no parcels attached");
        }

        BigDecimal amount = calculateAmount(parcels);

        Invoice invoice = new Invoice();
        invoice.setShipment(shipment);
        invoice.setAmount(amount);
        invoice.setCreatedAt(LocalDateTime.now());
        return toDto(invoiceRepository.save(invoice));
    }

    @Override
    public BigDecimal calculateAmount(List<Parcel> parcels) {
        BigDecimal amount = BigDecimal.ZERO;
        for (Parcel parcel : parcels) {
            if (parcel.getShippingFee() == null) {
                throw new CustomException(409, "Parcel " + parcel.getParcelId()
                        + " (tracking " + parcel.getTrackingNumber() + ") has no shippingFee — "
                        + "it was booked before pricing was wired in. Backfill it before invoicing this shipment.");
            }
            amount = amount.add(parcel.getShippingFee());

            if (parcel.getReturnFee() != null) {
                amount = amount.add(parcel.getReturnFee());
            }
        }
        return amount;
    }

    @Override
    public InvoiceDTO getByShipmentId(Long shipmentId) {
        Optional<Invoice> optionalInvoice = invoiceRepository.findByShipment_ShipmentId(shipmentId);
        if (optionalInvoice.isEmpty()) {
            throw new CustomException(404, "No invoice found for this shipment");
        }
        return toDto(optionalInvoice.get());
    }


    @Override
    public List<InvoiceDTO> getAll() {
        List<InvoiceDTO> list = new ArrayList<>();
        for (Invoice c : invoiceRepository.findAll()) list.add(toDto(c));
        return list;
    }


    private InvoiceDTO toDto(Invoice i) {
        BigDecimal paid = paymentRepository.sumForInvoice(i.getInvoiceId());
        return new InvoiceDTO(i.getInvoiceId(), i.getShipment().getShipmentId(), i.getAmount(), paid, i.getAmount().subtract(paid));
    }
}