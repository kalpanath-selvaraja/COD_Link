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
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ShipmentRepository shipmentRepository;
    private final ParcelRepository parcelRepository;

    @Override
    public InvoiceDTO create(InvoiceDTO dto) {
        Optional<Shipment> optionalShipment = shipmentRepository.findById(dto.getShipmentId());
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

    private InvoiceDTO toDto(Invoice i) {
        return new InvoiceDTO(i.getInvoiceId(), i.getShipment().getShipmentId(), i.getAmount());
    }
}