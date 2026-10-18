package com.ijse.codlink.service;

import com.ijse.codlink.dto.ParcelDTO;

import java.util.List;

public interface ParcelService {
    List<ParcelDTO> getAvailable();
    List<ParcelDTO> getAssigned();
    ParcelDTO claim(Long parcelId);
    ParcelDTO reassign(Long parcelId, Long courierId);

    ParcelDTO createForCustomer(Long customerId, ParcelDTO parcelDTO);
    ParcelDTO createMine(String username, ParcelDTO parcelDTO);
    ParcelDTO getById(Long parcelId);
    ParcelDTO getByTrackingNumber(String trackingNumber);
    List<ParcelDTO> getByCustomerId(Long customerId);
    ParcelDTO updateStatus(Long parcelId, ParcelDTO statusDto);
    void delete(Long parcelId);

    ParcelDTO assignToShipment(Long parcelId, Long shipmentId);

    List<ParcelDTO> getAll();
    List<ParcelDTO> bookCartForCustomer(String username, List<ParcelDTO> parcelDtos);
    List<ParcelDTO> bookCartForWalkIn(Long customerId, List<ParcelDTO> parcelDtos);
}
