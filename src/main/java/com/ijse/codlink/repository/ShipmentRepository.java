package com.ijse.codlink.repository;

import com.ijse.codlink.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select e from Shipment e where e.shipmentId = :id")
    java.util.Optional<Shipment> findLockedById(@org.springframework.data.repository.query.Param("id") Long id);


    List<Shipment> findByCustomer_CustomerId(Long customerId);

}
