package com.ijse.codlink.repository;

import com.ijse.codlink.entity.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ParcelRepository extends JpaRepository<Parcel,Long> {


    Optional<Parcel> findByTrackingNumber(String trackingNumber);

    List<Parcel> findByCustomer_CustomerId(Long customerId);

    boolean existsByShipment_ShipmentId(Long shipmentId);

    List<Parcel> findByShipment_ShipmentId(Long shipmentId);

    @Query("SELECT p FROM Parcel p " +
            "JOIN FETCH p.customer c " +
            "JOIN FETCH p.originBranch b " +
            "LEFT JOIN FETCH p.currentBranch cb " +
            "LEFT JOIN FETCH p.shipment s")
    List<Parcel> findAllWithCustomerAndBranch();
}
