package com.ijse.codlink.repository;

import com.ijse.codlink.entity.DeliveryAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryAttemptRepository extends JpaRepository<DeliveryAttempt, Long> {
    List<DeliveryAttempt> findByParcel_ParcelId(Long parcelId);
}