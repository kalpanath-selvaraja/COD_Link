package com.ijse.codlink.repository;

import com.ijse.codlink.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByInvoice_InvoiceId(Long invoiceId);
    boolean existsByInvoice_InvoiceId(Long invoiceId);

    java.util.Optional<Payment> findByRequestReference(String requestReference);
    java.util.Optional<Payment> findByCollection_CollectionId(Long collectionId);

    @org.springframework.data.jpa.repository.Query("select coalesce(sum(p.amount), 0) from Payment p where p.invoice.invoiceId = :id")
    java.math.BigDecimal sumForInvoice(@org.springframework.data.repository.query.Param("id") Long id);

    boolean existsByInvoice_InvoiceIdAndRequestReferenceIsNull(Long invoiceId);
    @org.springframework.data.jpa.repository.Query("select coalesce(sum(p.amount), 0) from Payment p "
            + "where p.requestReference is not null and p.paidAt >= :start and p.paidAt < :end")
    java.math.BigDecimal sumReceiptsBetween(@org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
            @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

    List<Payment> findAll();
}