package com.ijse.codlink.service;

import com.ijse.codlink.entity.*;
import com.ijse.codlink.repository.*;
import com.ijse.codlink.enumeration.ParcelStatus;
import com.ijse.codlink.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ParcelLifecycle {
    private final BusinessNotifications notifications;
    private final ParcelRepository parcelRepository;
    private final TrackingEventRepository trackingEventRepository;
    private final ShipmentService shipmentService;
    private final PricingRuleService pricingRuleService;
    private static final BigDecimal RETURN_SURCHARGE_MULTIPLIER = new BigDecimal("1.10");
    private static final Map<ParcelStatus, Set<ParcelStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(ParcelStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(ParcelStatus.BOOKED, EnumSet.of(ParcelStatus.IN_TRANSIT));
        ALLOWED_TRANSITIONS.put(ParcelStatus.IN_TRANSIT, EnumSet.of(ParcelStatus.DELIVERED, ParcelStatus.FAILED));
        ALLOWED_TRANSITIONS.put(ParcelStatus.FAILED, EnumSet.of(ParcelStatus.RETURNED, ParcelStatus.IN_TRANSIT));
        ALLOWED_TRANSITIONS.put(ParcelStatus.DELIVERED, EnumSet.noneOf(ParcelStatus.class));
        ALLOWED_TRANSITIONS.put(ParcelStatus.RETURNED, EnumSet.noneOf(ParcelStatus.class));
    }


    @Transactional
    public Parcel transition(Parcel parcel, ParcelStatus targetStatus) {
        ParcelStatus currentStatus = parcel.getStatus();


        Set<ParcelStatus> allowedNext = ALLOWED_TRANSITIONS.get(currentStatus);
        if (allowedNext == null || !allowedNext.contains(targetStatus)) {
            throw new CustomException(409,
                    "Cannot transition parcel from " + currentStatus + " to " + targetStatus);
        }

        if (targetStatus == ParcelStatus.RETURNED) {
            if (parcel.getDeliveryAddress() == null || parcel.getDeliveryAddress().getServiceZone() == null) {
                throw new CustomException(400, "Cannot calculate return fee — parcel has no delivery zone on record");
            }
            boolean isCod = parcel.getIsCod() != null ? parcel.getIsCod()
                    : (parcel.getCodAmount() != null && parcel.getCodAmount().compareTo(BigDecimal.ZERO) > 0);
            BigDecimal baseFee = pricingRuleService.calculateShippingFee(
                    parcel.getDeliveryAddress().getServiceZone().getZoneId(), parcel.getWeight(), isCod);
            BigDecimal returnFee = baseFee.multiply(RETURN_SURCHARGE_MULTIPLIER).setScale(2, RoundingMode.HALF_UP);
            parcel.setReturnFee(returnFee);
        }

        if (targetStatus == ParcelStatus.DELIVERED) {
            if (parcel.getAssignedCourier() == null) throw new CustomException(409, "Assign a courier before delivery");
            parcel.setDeliveredBy(parcel.getAssignedCourier());
        }
        parcel.setStatus(targetStatus);
        Parcel saved = parcelRepository.save(parcel);
        TrackingEvent event = new TrackingEvent();
        event.setParcel(saved);
        event.setStatus(targetStatus.name());
        event.setEventTime(java.time.LocalDateTime.now());
        trackingEventRepository.save(event);
        notifications.customer(saved.getCustomer(), "Parcel " + saved.getTrackingNumber() + ": " + targetStatus.name() + ".");

        if (saved.getShipment() != null) {
            shipmentService.resolveShipmentIfComplete(saved.getShipment().getShipmentId());
        }

        return saved;
    }
}
