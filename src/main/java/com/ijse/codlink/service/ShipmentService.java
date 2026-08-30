package com.ijse.codlink.service;

import com.ijse.codlink.dto.ShipmentDTO;

import java.util.List;

public interface ShipmentService {
    ShipmentDTO create(ShipmentDTO shipmentDTO);
    List<ShipmentDTO> getByCustomerId(Long customerId);
    ShipmentDTO getById(Long shipmentId);
    void delete(Long shipmentId);

    ShipmentDTO cancel(Long shipmentId);
    List<ShipmentDTO> getAll();
}
