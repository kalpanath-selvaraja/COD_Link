package com.ijse.codlink.repository;

import com.ijse.codlink.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByShipment_ShipmentId(Long shipmentId);
    boolean existsByShipment_ShipmentId(Long shipmentId);
}