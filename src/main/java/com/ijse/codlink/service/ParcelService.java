package com.ijse.codlink.service;

import com.ijse.codlink.dto.ParcelDTO;

import java.util.List;

public interface ParcelService {
    ParcelDTO createForCustomer(Long customerId, ParcelDTO parcelDTO);
    ParcelDTO createMine(String username, ParcelDTO parcelDTO);
    ParcelDTO getById(Long parcelId);
    ParcelDTO getByTrackingNumber(String trackingNumber);
    List<ParcelDTO> getByCustomerId(Long customerId);
    ParcelDTO updateStatus(Long parcelId, ParcelDTO statusDto);
    void delete(Long parcelId);
}
