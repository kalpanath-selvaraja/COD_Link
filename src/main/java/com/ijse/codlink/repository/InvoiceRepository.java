package com.ijse.codlink.repository;

import com.ijse.codlink.entity.CodCollection;
import com.ijse.codlink.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByShipment_ShipmentId(Long shipmentId);
    boolean existsByShipment_ShipmentId(Long shipmentId);


    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Invoice i " +
            "WHERE EXISTS (SELECT p FROM Payment p WHERE p.invoice = i)")
    BigDecimal sumAmountWithPayment();

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Invoice i " +
            "WHERE EXISTS (SELECT p FROM Payment p WHERE p.invoice = i) " +
            "AND i.createdAt >= :since")
    BigDecimal sumAmountWithPaymentSince(@Param("since") LocalDateTime since);

    List<Invoice> findAll();
}