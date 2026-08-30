package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.DeliveryAttemptDTO;
import com.ijse.codlink.entity.Courier;
import com.ijse.codlink.entity.DeliveryAttempt;
import com.ijse.codlink.entity.Parcel;
import com.ijse.codlink.entity.TrackingEvent;
import com.ijse.codlink.enumeration.ParcelStatus;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.CourierRepository;
import com.ijse.codlink.repository.DeliveryAttemptRepository;
import com.ijse.codlink.repository.ParcelRepository;
import com.ijse.codlink.repository.TrackingEventRepository;
import com.ijse.codlink.service.DeliveryAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeliveryAttemptServiceImpl implements DeliveryAttemptService {

    private final DeliveryAttemptRepository deliveryAttemptRepository;
    private final ParcelRepository parcelRepository;
    private final CourierRepository courierRepository;
    private final TrackingEventRepository trackingEventRepository;

    @Override
    @Transactional
    public DeliveryAttemptDTO create(DeliveryAttemptDTO dto) {
        Optional<Parcel> optionalParcel = parcelRepository.findById(dto.getParcelId());
        if (optionalParcel.isEmpty()) {
            throw new CustomException(404, "Parcel not found");
        }
        Optional<Courier> optionalCourier = courierRepository.findById(dto.getCourierId());
        if (optionalCourier.isEmpty()) {
            throw new CustomException(404, "Courier not found");
        }
        if (dto.getOutcome() == null || dto.getOutcome().isBlank()) {
            throw new CustomException(400, "Outcome is required");
        }

        Parcel parcel = optionalParcel.get();

        if (parcel.getStatus() == ParcelStatus.DELIVERED
                || parcel.getStatus() == ParcelStatus.FAILED
                || parcel.getStatus() == ParcelStatus.RETURNED) {
            throw new CustomException(409,
                    "Cannot log a delivery attempt — parcel status is " + parcel.getStatus());
        }

        if (parcel.getStatus() == ParcelStatus.BOOKED) {
            TrackingEvent pickedUp = new TrackingEvent();
            pickedUp.setStatus("PICKED_UP");
            pickedUp.setEventTime(LocalDateTime.now());
            pickedUp.setParcel(parcel);
            trackingEventRepository.save(pickedUp);

            parcel.setStatus(ParcelStatus.IN_TRANSIT);
            parcelRepository.save(parcel);
        }

        DeliveryAttempt attempt = new DeliveryAttempt();
        attempt.setOutcome(dto.getOutcome());
        attempt.setAttemptedAt(LocalDateTime.now());
        attempt.setParcel(parcel);
        attempt.setCourier(optionalCourier.get());
        DeliveryAttempt savedAttempt = deliveryAttemptRepository.save(attempt);

        if ("FAILED".equalsIgnoreCase(dto.getOutcome().trim())) {
            TrackingEvent failedEvent = new TrackingEvent();
            failedEvent.setStatus("FAILED");
            failedEvent.setEventTime(LocalDateTime.now());
            failedEvent.setParcel(parcel);
            trackingEventRepository.save(failedEvent);

            parcel.setStatus(ParcelStatus.FAILED);
            parcelRepository.save(parcel);
        }

        if ("DELIVERED".equalsIgnoreCase(dto.getOutcome().trim())) {
            TrackingEvent deliveredEvent = new TrackingEvent();
            deliveredEvent.setStatus("DELIVERED");
            deliveredEvent.setEventTime(LocalDateTime.now());
            deliveredEvent.setParcel(parcel);
            trackingEventRepository.save(deliveredEvent);

            parcel.setStatus(ParcelStatus.DELIVERED);
            parcelRepository.save(parcel);
        }



        return toDto(savedAttempt);
    }

    @Override
    public List<DeliveryAttemptDTO> getByParcelId(Long parcelId) {
        Optional<Parcel> optionalParcel = parcelRepository.findById(parcelId);
        if (optionalParcel.isEmpty()) {
            throw new CustomException(404, "Parcel not found");
        }
        List<DeliveryAttemptDTO> list = new ArrayList<>();
        for (DeliveryAttempt a : deliveryAttemptRepository.findByParcel_ParcelId(parcelId)) {
            list.add(toDto(a));
        }
        return list;
    }

    private DeliveryAttemptDTO toDto(DeliveryAttempt a) {
        return new DeliveryAttemptDTO(a.getAttemptId(), a.getParcel().getParcelId(),
                a.getCourier().getCourierId(), a.getOutcome(), a.getAttemptedAt());
    }
}