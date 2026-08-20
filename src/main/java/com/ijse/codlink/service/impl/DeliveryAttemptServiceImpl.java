package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.DeliveryAttemptDTO;
import com.ijse.codlink.entity.Courier;
import com.ijse.codlink.entity.DeliveryAttempt;
import com.ijse.codlink.entity.Parcel;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.CourierRepository;
import com.ijse.codlink.repository.DeliveryAttemptRepository;
import com.ijse.codlink.repository.ParcelRepository;
import com.ijse.codlink.service.DeliveryAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    @Override
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
        DeliveryAttempt attempt = new DeliveryAttempt();
        attempt.setOutcome(dto.getOutcome());
        attempt.setAttemptedAt(LocalDateTime.now());
        attempt.setParcel(optionalParcel.get());
        attempt.setCourier(optionalCourier.get());
        return toDto(deliveryAttemptRepository.save(attempt));
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