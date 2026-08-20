package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.CodCollectionDTO;
import com.ijse.codlink.entity.CodCollection;
import com.ijse.codlink.entity.Courier;
import com.ijse.codlink.entity.Parcel;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.CodCollectionRepository;
import com.ijse.codlink.repository.CourierRepository;
import com.ijse.codlink.repository.ParcelRepository;
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

    @Override
    public CodCollectionDTO create(CodCollectionDTO dto) {
        Optional<Parcel> optionalParcel = parcelRepository.findById(dto.getParcelId());
        if (optionalParcel.isEmpty()) {
            throw new CustomException(404, "Parcel not found");
        }
        Optional<Courier> optionalCourier = courierRepository.findById(dto.getCourierId());
        if (optionalCourier.isEmpty()) {
            throw new CustomException(404, "Courier not found");
        }
        if (codCollectionRepository.existsByParcel_ParcelId(dto.getParcelId())) {
            throw new CustomException(409, "COD already collected for this parcel");
        }
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(400, "Amount must be greater than zero");
        }
        CodCollection collection = new CodCollection();
        collection.setParcel(optionalParcel.get());
        collection.setCourier(optionalCourier.get());
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

    private CodCollectionDTO toDto(CodCollection c) {
        return new CodCollectionDTO(c.getCollectionId(), c.getParcel().getParcelId(),
                c.getCourier().getCourierId(), c.getAmount(),
                c.getReconciliationBatch() != null ? c.getReconciliationBatch().getBatchId() : null);
    }
}