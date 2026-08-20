package com.ijse.codlink.service;

import com.ijse.codlink.dto.ServiceZoneDTO;

import java.util.List;

public interface ServiceZoneService {
    ServiceZoneDTO create(ServiceZoneDTO dto);
    List<ServiceZoneDTO> getAll();
    ServiceZoneDTO update(Long zoneId, ServiceZoneDTO dto);
    void delete(Long zoneId);
}