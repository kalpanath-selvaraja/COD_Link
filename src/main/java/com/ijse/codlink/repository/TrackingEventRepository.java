package com.ijse.codlink.repository;

import com.ijse.codlink.entity.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {
    List<TrackingEvent> findByParcel_ParcelId(Long parcelId);
}
