package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.CodCollectionDTO;
import com.ijse.codlink.entity.CodCollection;
import com.ijse.codlink.entity.Courier;
import com.ijse.codlink.entity.Parcel;
import com.ijse.codlink.entity.User;
import com.ijse.codlink.enumeration.ParcelStatus;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.CodCollectionRepository;
import com.ijse.codlink.repository.CourierRepository;
import com.ijse.codlink.repository.ParcelRepository;
import com.ijse.codlink.repository.UserRepository;
import com.ijse.codlink.service.CodCollectionService;
import com.ijse.codlink.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CodCollectionServiceImpl implements CodCollectionService {
    private final com.ijse.codlink.security.RecordAccess recordAccess;

    private final CodCollectionRepository codCollectionRepository;
    private final ParcelRepository parcelRepository;
    private final CourierRepository courierRepository;
    private final UserRepository userRepository;
    private final ShipmentService shipmentService;
    private final com.ijse.codlink.service.PaymentService paymentService;

    @Override
    @Transactional
    public CodCollectionDTO create(String username, CodCollectionDTO dto) {
        log.info("Execute method create()");
        Optional<Parcel> optionalParcel = parcelRepository.findLockedById(dto.getParcelId());
        if (optionalParcel.isEmpty()) {
            log.error("Parcel not found with ID: {}", dto.getParcelId());
            throw new CustomException(404, "Parcel not found");
        }
        Parcel parcel = optionalParcel.get();
        recordAccess.assigned(parcel);

        if (parcel.getStatus() != ParcelStatus.DELIVERED) {
            log.error("Parcel status is not DELIVERED for parcel ID: {}", dto.getParcelId());
            throw new CustomException(409, "Parcels that are not at DELIVERED stage cannot have COD");
        }

        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Amount is null or not greater than zero");
            throw new CustomException(400, "Amount must be greater than zero");
        }

        if (!Boolean.TRUE.equals(parcel.getIsCod()) || parcel.getCodAmount() == null || parcel.getCodAmount().signum() <= 0) {
            log.error("Parcel has no COD amount on record for parcel ID: {}", dto.getParcelId());
            throw new CustomException(409, "This parcel has no COD amount on record — it cannot be a cash-on-delivery collection.");
        }
        if (parcel.getShippingFee() == null) {
            log.error("Parcel has no shipping fee on record for parcel ID: {}", dto.getParcelId());
            throw new CustomException(409, "This parcel has no shipping fee on record — cannot verify the collected amount.");
        }

        BigDecimal expectedTotal = parcel.getCodAmount().add(parcel.getShippingFee());

        if (expectedTotal.compareTo(dto.getAmount()) != 0) {
            log.error("Collected amount does not match for parcel ID: {}. Expected: {}, Got: {}", dto.getParcelId(), expectedTotal, dto.getAmount());
            throw new CustomException(409, "Collected amount does not match. Expected LKR " + expectedTotal
                    + " (goods value LKR " + parcel.getCodAmount() + " + shipping fee LKR " + parcel.getShippingFee() + ").");
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
        if (parcel.getDeliveredBy() == null || !parcel.getDeliveredBy().getCourierId().equals(courier.getCourierId())) {
            throw new CustomException(409, "Collection must be recorded by the courier who delivered this parcel");
        }

        if (codCollectionRepository.existsByParcel_ParcelId(dto.getParcelId())) {
            log.error("COD already collected for parcel ID: {}", dto.getParcelId());
            throw new CustomException(409, "COD already collected for this parcel");
        }

        CodCollection collection = new CodCollection();
        collection.setParcel(parcel);
        collection.setCourier(courier);
        collection.setAmount(dto.getAmount());
        collection.setReconciliationBatch(null);
        CodCollection saved = codCollectionRepository.save(collection);
        paymentService.createFromCollection(saved);


        if (parcel.getShipment() != null) {
            shipmentService.resolveShipmentIfComplete(parcel.getShipment().getShipmentId());
        }

        return toDto(saved);
    }

    @Override
    public List<CodCollectionDTO> getByCourierId(Long courierId) {
        recordAccess.courier(courierId);
        log.info("Execute method getByCourierId()");
        Optional<Courier> optionalCourier = courierRepository.findById(courierId);
        if (optionalCourier.isEmpty()) {
            throw new CustomException(404, "Courier not found");
        }
        List<CodCollectionDTO> list = new ArrayList<>();
        for (CodCollection c : codCollectionRepository.findByCourier_CourierId(courierId)) {
            list.add(toDto(c));
        }
        return list;
    }

    @Override
    public List<CodCollectionDTO> getAll() {
        log.info("Execute method getAll()");
        List<CodCollectionDTO> list = new ArrayList<>();
        for (CodCollection c : codCollectionRepository.findAll()) {
            if (recordAccess.hasRole("ADMIN") || recordAccess.owns(c.getCourier())) list.add(toDto(c));
        }
        return list;
    }

    private CodCollectionDTO toDto(CodCollection c) {
        log.info("Execute method toDto()");
        return new CodCollectionDTO(c.getCollectionId(), c.getParcel().getParcelId(),
                c.getCourier().getCourierId(), c.getAmount(),
                c.getReconciliationBatch() != null ? c.getReconciliationBatch().getBatchId() : null);
    }
}