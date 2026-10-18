package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.PaymentDTO;
import com.ijse.codlink.entity.*;
import com.ijse.codlink.enumeration.ParcelStatus;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.*;
import com.ijse.codlink.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@org.springframework.transaction.annotation.Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final BusinessNotifications notifications;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final ShipmentRepository shipmentRepository;
    private final ParcelRepository parcelRepository;
    private final CodCollectionRepository codCollectionRepository;
    private final ShipmentService shipmentService;

    @Override
    @Transactional
    public PaymentDTO create(PaymentDTO dto) {
        if (dto.getInvoiceId() == null || dto.getAmount() == null || dto.getAmount().signum() <= 0
                || dto.getAmount().scale() > 2 || dto.getMethod() == null
                || !List.of("CASH", "BANK_TRANSFER", "CREDIT_CARD").contains(dto.getMethod())
                || dto.getRequestReference() == null || !dto.getRequestReference().matches("[A-Za-z0-9_-]{8,100}")) {
            throw new CustomException(400, "Invoice, positive amount, payment method and request reference are required");
        }
        Invoice invoice = lockInvoice(dto.getInvoiceId());
        var previous = paymentRepository.findByRequestReference(dto.getRequestReference());
        if (previous.isPresent()) {
            Payment receipt = previous.get();
            if (!receipt.getInvoice().getInvoiceId().equals(dto.getInvoiceId())
                    || receipt.getAmount().compareTo(dto.getAmount()) != 0 || !receipt.getMethod().equals(dto.getMethod())) {
                throw new CustomException(409, "Request reference already used for another receipt");
            }
            return toDto(receipt);
        }
        BigDecimal available = outstanding(invoice);
        for (Parcel parcel : parcelRepository.findByShipment_ShipmentId(invoice.getShipment().getShipmentId())) {
            if (Boolean.TRUE.equals(parcel.getIsCod()) && parcel.getStatus() != ParcelStatus.RETURNED
                    && !codCollectionRepository.existsByParcel_ParcelId(parcel.getParcelId())) {
                available = available.subtract(parcel.getShippingFee());
            }
        }
        if (dto.getAmount().compareTo(available) > 0) {
            throw new CustomException(409, "Amount exceeds the balance available for a manual receipt");
        }
        Payment payment = save(invoice, dto.getAmount(), dto.getMethod(), dto.getRequestReference(), null);
        shipmentService.resolveShipmentIfComplete(invoice.getShipment().getShipmentId());
        return toDto(payment);
    }

    @Override
    @Transactional
    public PaymentDTO createFromCollection(CodCollection collection) {
        Parcel parcel = collection.getParcel();
        if (parcel.getShipment() == null) throw new CustomException(409, "Invoice the parcel before collecting COD");
        shipmentRepository.findLockedById(parcel.getShipment().getShipmentId())
                .orElseThrow(() -> new CustomException(404, "Shipment not found"));
        Invoice invoice = invoiceRepository.findLockedByShipmentId(parcel.getShipment().getShipmentId())
                .orElseThrow(() -> new CustomException(409, "Invoice the shipment before collecting COD"));
        var previous = paymentRepository.findByCollection_CollectionId(collection.getCollectionId());
        if (previous.isPresent()) return toDto(previous.get());
        if (parcel.getShippingFee().compareTo(outstanding(invoice)) > 0) {
            throw new CustomException(409, "Shipping receipt exceeds the invoice balance");
        }
        return toDto(save(invoice, parcel.getShippingFee(), "COD", "COD_" + collection.getCollectionId(), collection));
    }

    private Invoice lockInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new CustomException(404, "Invoice not found"));
        shipmentRepository.findLockedById(invoice.getShipment().getShipmentId())
                .orElseThrow(() -> new CustomException(404, "Shipment not found"));
        return invoiceRepository.findLockedById(invoiceId).orElseThrow();
    }

    private BigDecimal outstanding(Invoice invoice) {
        if (paymentRepository.existsByInvoice_InvoiceIdAndRequestReferenceIsNull(invoice.getInvoiceId())) {
            throw new CustomException(409, "Historical payments need review before adding receipts");
        }
        return invoice.getAmount().subtract(paymentRepository.sumForInvoice(invoice.getInvoiceId()));
    }

    private Payment save(Invoice invoice, BigDecimal amount, String method, String reference, CodCollection collection) {
        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setRequestReference(reference);
        payment.setCollection(collection);
        payment.setPaidAt(LocalDateTime.now(ZoneId.of("Asia/Colombo")));
        Payment saved = paymentRepository.save(payment);
        notifications.customer(invoice.getShipment().getCustomer(), "Received LKR " + amount + " for invoice #" + invoice.getInvoiceId() + ".");
        return saved;
    }

    @Override
    public List<PaymentDTO> getAll() {
        return paymentRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public List<PaymentDTO> getByInvoiceId(Long invoiceId) {
        if (!invoiceRepository.existsById(invoiceId)) throw new CustomException(404, "Invoice not found");
        return paymentRepository.findByInvoice_InvoiceId(invoiceId).stream().map(this::toDto).toList();
    }

    private PaymentDTO toDto(Payment payment) {
        return new PaymentDTO(payment.getRequestReference(), payment.getCollection() == null ? null : payment.getCollection().getCollectionId(),
                payment.getPaymentId(), payment.getInvoice().getInvoiceId(), payment.getMethod(), payment.getAmount(), payment.getPaidAt());
    }
}
