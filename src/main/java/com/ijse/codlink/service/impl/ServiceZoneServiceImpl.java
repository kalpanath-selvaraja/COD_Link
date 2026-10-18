package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.ServiceZoneDTO;
import com.ijse.codlink.entity.ServiceZone;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.ServiceZoneRepository;
import com.ijse.codlink.service.ServiceZoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@org.springframework.transaction.annotation.Transactional
@Slf4j
@RequiredArgsConstructor
public class ServiceZoneServiceImpl implements ServiceZoneService {

    private final ServiceZoneRepository serviceZoneRepository;

    @Override
    public ServiceZoneDTO create(ServiceZoneDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new CustomException(400, "Zone name is required");
        }
        ServiceZone zone = new ServiceZone();
        zone.setName(dto.getName());
        return toDto(serviceZoneRepository.save(zone));
    }

    @Override
    public List<ServiceZoneDTO> getAll() {
        List<ServiceZoneDTO> list = new ArrayList<>();
        for (ServiceZone zone : serviceZoneRepository.findAll()) {
            list.add(toDto(zone));
        }
        return list; // [] empty if none
    }

    @Override
    public ServiceZoneDTO update(Long zoneId, ServiceZoneDTO dto) {
        Optional<ServiceZone> optionalZone = serviceZoneRepository.findById(zoneId);
        if (optionalZone.isEmpty()) {
            throw new CustomException(404, "Service zone not found");
        }
        ServiceZone zone = optionalZone.get();
        if (dto.getName() != null) zone.setName(dto.getName());
        return toDto(serviceZoneRepository.save(zone));
    }

    @Override
    public void delete(Long zoneId) {
        Optional<ServiceZone> optionalZone = serviceZoneRepository.findById(zoneId);
        if (optionalZone.isEmpty()) {
            throw new CustomException(404, "Service zone not found");
        }
        serviceZoneRepository.delete(optionalZone.get());
    }

    private ServiceZoneDTO toDto(ServiceZone zone) {
        return new ServiceZoneDTO(zone.getZoneId(), zone.getName());
    }
}