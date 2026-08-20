package com.ijse.codlink.service;

import com.ijse.codlink.dto.VehicleDTO;

import java.util.List;

public interface VehicleService {
    VehicleDTO create(VehicleDTO dto);
    VehicleDTO getById(Long vehicleId);
    List<VehicleDTO> getByCourierId(Long courierId);
    VehicleDTO update(Long vehicleId, VehicleDTO dto);
    void delete(Long vehicleId);
}