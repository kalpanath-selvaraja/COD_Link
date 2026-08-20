package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.PaymentDTO;
import com.ijse.codlink.entity.Invoice;
import com.ijse.codlink.entity.Payment;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.InvoiceRepository;
import com.ijse.codlink.repository.PaymentRepository;
import com.ijse.codlink.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    @Override
    public PaymentDTO create(PaymentDTO dto) {
        Optional<Invoice> optionalInvoice = invoiceRepository.findById(dto.getInvoiceId());
        if (optionalInvoice.isEmpty()) {
            throw new CustomException(404, "Invoice not found");
        }
        if (dto.getMethod() == null || dto.getMethod().isBlank()) {
            throw new CustomException(400, "Payment method is required");
        }
        Payment payment = new Payment();
        payment.setInvoice(optionalInvoice.get());
        payment.setMethod(dto.getMethod());
        payment.setPaidAt(LocalDateTime.now());
        return toDto(paymentRepository.save(payment));
    }

    @Override
    public List<PaymentDTO> getByInvoiceId(Long invoiceId) {
        Optional<Invoice> optionalInvoice = invoiceRepository.findById(invoiceId);
        if (optionalInvoice.isEmpty()) {
            throw new CustomException(404, "Invoice not found");
        }
        List<PaymentDTO> list = new ArrayList<>();
        for (Payment p : paymentRepository.findByInvoice_InvoiceId(invoiceId)) {
            list.add(toDto(p));
        }
        return list;
    }

    private PaymentDTO toDto(Payment p) {
        return new PaymentDTO(p.getPaymentId(), p.getInvoice().getInvoiceId(), p.getMethod(), p.getPaidAt());
    }
}