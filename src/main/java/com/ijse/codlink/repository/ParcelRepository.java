package com.ijse.codlink.repository;

import com.ijse.codlink.entity.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParcelRepository extends JpaRepository<Parcel,Long> {
    

    Optional<Parcel> findByTrackingNumber(String trackingNumber);

    List<Parcel> findByCustomer_CustomerId(Long customerId);
}
