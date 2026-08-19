package com.ijse.codlink.service;

import com.ijse.codlink.dto.TrackingEventDTO;

import java.util.List;

public interface TrackingEventService {
    TrackingEventDTO create(TrackingEventDTO dto);
    List<TrackingEventDTO> getByParcelId(Long parcelId);
}
