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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CodCollectionServiceImpl implements CodCollectionService {

    private final CodCollectionRepository codCollectionRepository;
    private final ParcelRepository parcelRepository;
    private final CourierRepository courierRepository;
    private final UserRepository userRepository;

    @Override
    public CodCollectionDTO create(String username, CodCollectionDTO dto) {
        Optional<Parcel> optionalParcel = parcelRepository.findById(dto.getParcelId());
        if (optionalParcel.isEmpty()) {
            throw new CustomException(404, "Parcel not found");
        }
        Parcel parcel = optionalParcel.get();

        if (parcel.getStatus() != ParcelStatus.DELIVERED) {
            throw new CustomException(409, "Parcels that are not at DELIVERED stage cannot have COD");
        }

        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(400, "Amount must be greater than zero");
        }

        if (parcel.getCodAmount() == null) {
            throw new CustomException(409, "This parcel has no COD amount on record — it cannot be a cash-on-delivery collection.");
        }
        if (parcel.getShippingFee() == null) {
            throw new CustomException(409, "This parcel has no shipping fee on record — cannot verify the collected amount.");
        }

        BigDecimal expectedTotal = parcel.getCodAmount().add(parcel.getShippingFee());

        if (expectedTotal.compareTo(dto.getAmount()) != 0) {
            throw new CustomException(409, "Collected amount does not match. Expected LKR " + expectedTotal
                    + " (goods value LKR " + parcel.getCodAmount() + " + shipping fee LKR " + parcel.getShippingFee() + ").");
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

        if (codCollectionRepository.existsByParcel_ParcelId(dto.getParcelId())) {
            throw new CustomException(409, "COD already collected for this parcel");
        }

        CodCollection collection = new CodCollection();
        collection.setParcel(parcel);
        collection.setCourier(courier);
        collection.setAmount(dto.getAmount());
        collection.setReconciliationBatch(null);
        return toDto(codCollectionRepository.save(collection));
    }

    @Override
    public List<CodCollectionDTO> getByCourierId(Long courierId) {
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
        List<CodCollectionDTO> list = new ArrayList<>();
        for (CodCollection c : codCollectionRepository.findAll()) list.add(toDto(c));
        return list;
    }

    private CodCollectionDTO toDto(CodCollection c) {
        return new CodCollectionDTO(c.getCollectionId(), c.getParcel().getParcelId(),
                c.getCourier().getCourierId(), c.getAmount(),
                c.getReconciliationBatch() != null ? c.getReconciliationBatch().getBatchId() : null);
    }
}