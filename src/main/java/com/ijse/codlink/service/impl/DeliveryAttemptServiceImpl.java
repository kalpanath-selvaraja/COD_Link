package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.DeliveryAttemptDTO;
import com.ijse.codlink.entity.Courier;
import com.ijse.codlink.entity.DeliveryAttempt;
import com.ijse.codlink.entity.Parcel;
import com.ijse.codlink.entity.TrackingEvent;
import com.ijse.codlink.entity.User;
import com.ijse.codlink.enumeration.AttemptOutcome;
import com.ijse.codlink.enumeration.ParcelStatus;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.CourierRepository;
import com.ijse.codlink.repository.DeliveryAttemptRepository;
import com.ijse.codlink.repository.ParcelRepository;
import com.ijse.codlink.repository.TrackingEventRepository;
import com.ijse.codlink.repository.UserRepository;
import com.ijse.codlink.service.DeliveryAttemptService;
import com.ijse.codlink.service.ShipmentService;
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
    private final UserRepository userRepository;
    private final TrackingEventRepository trackingEventRepository;
    private final ShipmentService shipmentService;

    @Override
    @Transactional
    public DeliveryAttemptDTO create(String username, DeliveryAttemptDTO dto) {
        Optional<Parcel> optionalParcel = parcelRepository.findById(dto.getParcelId());
        if (optionalParcel.isEmpty()) {
            throw new CustomException(404, "Parcel not found");
        }

        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            throw new CustomException(404, "User not found");
        }
        Optional<Courier> optionalCourier = courierRepository.findByUser_UserId(optionalUser.get().getUserId());
        if (optionalCourier.isEmpty()) {
            throw new CustomException(404, "No courier profile linked to this account");
        }
        Courier courier = optionalCourier.get();

        if (dto.getOutcomeCode() == null) {
            throw new CustomException(400, "outcomeCode is required (DELIVERED, FAILED, or RESCHEDULED)");
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
        attempt.setOutcomeCode(dto.getOutcomeCode());
        attempt.setOutcome(dto.getOutcome());
        attempt.setAttemptedAt(LocalDateTime.now());
        attempt.setParcel(parcel);
        attempt.setCourier(courier);
        DeliveryAttempt savedAttempt = deliveryAttemptRepository.save(attempt);

        if (dto.getOutcomeCode() == AttemptOutcome.FAILED) {
            TrackingEvent failedEvent = new TrackingEvent();
            failedEvent.setStatus("FAILED");
            failedEvent.setEventTime(LocalDateTime.now());
            failedEvent.setParcel(parcel);
            trackingEventRepository.save(failedEvent);

            parcel.setStatus(ParcelStatus.FAILED);
            parcelRepository.save(parcel);
        }

        if (dto.getOutcomeCode() == AttemptOutcome.DELIVERED) {
            TrackingEvent deliveredEvent = new TrackingEvent();
            deliveredEvent.setStatus("DELIVERED");
            deliveredEvent.setEventTime(LocalDateTime.now());
            deliveredEvent.setParcel(parcel);
            trackingEventRepository.save(deliveredEvent);

            parcel.setStatus(ParcelStatus.DELIVERED);
            parcelRepository.save(parcel);

            if (parcel.getShipment() != null) {
                shipmentService.resolveShipmentIfComplete(parcel.getShipment().getShipmentId());
            }
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
                a.getCourier().getCourierId(), a.getOutcomeCode(), a.getOutcome(), a.getAttemptedAt());
    }
}