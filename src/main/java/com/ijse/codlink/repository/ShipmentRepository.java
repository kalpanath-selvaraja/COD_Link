package com.ijse.codlink.repository;

import com.ijse.codlink.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    List<Shipment> findByCustomer_CustomerId(Long customerId);

}
