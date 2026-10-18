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
@org.springframework.transaction.annotation.Transactional
@Slf4j
@RequiredArgsConstructor
public class DeliveryAttemptServiceImpl implements DeliveryAttemptService {
    private final com.ijse.codlink.security.RecordAccess recordAccess;

    private final com.ijse.codlink.service.ParcelLifecycle parcelLifecycle;
    private final DeliveryAttemptRepository deliveryAttemptRepository;
    private final ParcelRepository parcelRepository;
    private final CourierRepository courierRepository;
    private final UserRepository userRepository;
    private final TrackingEventRepository trackingEventRepository;
    private final ShipmentService shipmentService;

    @Override
    @Transactional
    public DeliveryAttemptDTO create(String username, DeliveryAttemptDTO dto) {
        log.info("Execute method create()");
        Optional<Parcel> optionalParcel = parcelRepository.findLockedById(dto.getParcelId());
        if (optionalParcel.isEmpty()) {
            log.error("Parcel not found with ID: {}", dto.getParcelId());
            throw new CustomException(404, "Parcel not found");
        }

        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            log.error("User not found with username: {}", username);
            throw new CustomException(404, "User not found");
        }
        Optional<Courier> optionalCourier = courierRepository.findByUser_UserId(optionalUser.get().getUserId());
        if (optionalCourier.isEmpty()) {
            log.error("No courier profile linked to user ID: {}", optionalUser.get().getUserId());
            throw new CustomException(404, "No courier profile linked to this account");
        }
        Courier courier = optionalCourier.get();

        if (dto.getOutcomeCode() == null) {
            throw new CustomException(400, "outcomeCode is required (DELIVERED, FAILED, or RESCHEDULED)");
        }

        Parcel parcel = optionalParcel.get();
        recordAccess.assigned(parcel);

        if (parcel.getStatus() == ParcelStatus.DELIVERED
                || parcel.getStatus() == ParcelStatus.FAILED
                || parcel.getStatus() == ParcelStatus.RETURNED) {
            throw new CustomException(409,
                    "Cannot log a delivery attempt - parcel status is " + parcel.getStatus());
        }

        if (parcel.getStatus() == ParcelStatus.BOOKED) {
            parcelLifecycle.transition(parcel, ParcelStatus.IN_TRANSIT);
        }

        DeliveryAttempt attempt = new DeliveryAttempt();
        attempt.setOutcomeCode(dto.getOutcomeCode());
        attempt.setOutcome(dto.getOutcome());
        attempt.setAttemptedAt(LocalDateTime.now());
        attempt.setParcel(parcel);
        attempt.setCourier(courier);
        DeliveryAttempt savedAttempt = deliveryAttemptRepository.save(attempt);

        if (dto.getOutcomeCode() == AttemptOutcome.FAILED) {
            parcelLifecycle.transition(parcel, ParcelStatus.FAILED);
        } else if (dto.getOutcomeCode() == AttemptOutcome.DELIVERED) {
            parcelLifecycle.transition(parcel, ParcelStatus.DELIVERED);
        }

        return toDto(savedAttempt);
    }

    @Override
    public List<DeliveryAttemptDTO> getByParcelId(Long parcelId) {
        recordAccess.parcel(parcelId);
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

    @Override
    public List<DeliveryAttemptDTO> getAll() {
        List<DeliveryAttemptDTO> list = new ArrayList<>();
        for (DeliveryAttempt a : deliveryAttemptRepository.findAllByOrderByAttemptedAtDesc()) {
            if (recordAccess.hasRole("ADMIN") || recordAccess.owns(a.getCourier())) list.add(toDto(a));
        }
        return list;
    }

    private DeliveryAttemptDTO toDto(DeliveryAttempt a) {
        return new DeliveryAttemptDTO(a.getAttemptId(), a.getParcel().getParcelId(),
                a.getCourier().getCourierId(), a.getOutcomeCode(), a.getOutcome(), a.getAttemptedAt());
    }
}