package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.TrackingEventDTO;
import com.ijse.codlink.entity.Parcel;
import com.ijse.codlink.entity.TrackingEvent;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.ParcelRepository;
import com.ijse.codlink.repository.TrackingEventRepository;
import com.ijse.codlink.service.TrackingEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingEventServiceImpl implements TrackingEventService {

    private final TrackingEventRepository trackingEventRepository;
    private final ParcelRepository parcelRepository;

    @Override
    public TrackingEventDTO create(TrackingEventDTO dto) {

        Optional<Parcel> optionalParcel = parcelRepository.findById(dto.getParcelId());

        if(optionalParcel.isEmpty()){
            throw new CustomException(404, "parcel not found");
        }

        if(dto.getStatus() == null || dto.getStatus().isBlank()){
            throw new CustomException(404, "status is required");
        }

        TrackingEvent trackingEvent = new TrackingEvent();

        trackingEvent.setStatus(dto.getStatus());
        trackingEvent.setEventTime(LocalDateTime.now());
        trackingEvent.setParcel(optionalParcel.get());

        TrackingEvent save = trackingEventRepository.save(trackingEvent);


        return toDto(save);
    }

    @Override
    public List<TrackingEventDTO> getByParcelId(Long parcelId) {
        Optional<Parcel> optionalParcel = parcelRepository.findById(parcelId);
        if (optionalParcel.isEmpty()) {
            throw new CustomException(404, "Parcel not found");
        }

        List<TrackingEvent> events = trackingEventRepository.findByParcel_ParcelId(parcelId);
        List<TrackingEventDTO> list = new ArrayList<>();
        for (TrackingEvent e : events) list.add(toDto(e));
        return list; // [] if none — never 404 on an empty list, per convention
    }

    private TrackingEventDTO toDto(TrackingEvent e) {
        return new TrackingEventDTO(e.getEventId(), e.getParcel().getParcelId(), e.getStatus(), e.getEventTime());
    }
}
