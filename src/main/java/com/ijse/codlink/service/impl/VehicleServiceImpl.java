package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.VehicleDTO;
import com.ijse.codlink.entity.Courier;
import com.ijse.codlink.entity.Vehicle;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.CourierRepository;
import com.ijse.codlink.repository.VehicleRepository;
import com.ijse.codlink.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CourierRepository courierRepository;

    @Override
    public VehicleDTO create(VehicleDTO dto) {
        Optional<Courier> optionalCourier = courierRepository.findById(dto.getCourierId());
        if (optionalCourier.isEmpty()) {
            throw new CustomException(404, "Courier not found");
        }
        if (dto.getPlateNumber() == null || dto.getPlateNumber().isBlank()) {
            throw new CustomException(400, "Plate number is required");
        }
        Vehicle vehicle = new Vehicle();
        vehicle.setType(dto.getType());
        vehicle.setPlateNumber(dto.getPlateNumber());
        vehicle.setCourier(optionalCourier.get());
        return toDto(vehicleRepository.save(vehicle));
    }

    @Override
    public VehicleDTO getById(Long vehicleId) {
        Optional<Vehicle> optionalVehicle = vehicleRepository.findById(vehicleId);
        if (optionalVehicle.isEmpty()) {
            throw new CustomException(404, "Vehicle not found");
        }
        return toDto(optionalVehicle.get());
    }

    @Override
    public List<VehicleDTO> getByCourierId(Long courierId) {
        Optional<Courier> optionalCourier = courierRepository.findById(courierId);
        if (optionalCourier.isEmpty()) {
            throw new CustomException(404, "Courier not found");
        }
        List<VehicleDTO> list = new ArrayList<>();
        for (Vehicle v : vehicleRepository.findByCourier_CourierId(courierId)) {
            list.add(toDto(v));
        }
        return list;
    }

    @Override
    public VehicleDTO update(Long vehicleId, VehicleDTO dto) {
        Optional<Vehicle> optionalVehicle = vehicleRepository.findById(vehicleId);
        if (optionalVehicle.isEmpty()) {
            throw new CustomException(404, "Vehicle not found");
        }
        Vehicle vehicle = optionalVehicle.get();
        if (dto.getType() != null) vehicle.setType(dto.getType());
        if (dto.getPlateNumber() != null) vehicle.setPlateNumber(dto.getPlateNumber());
        return toDto(vehicleRepository.save(vehicle));
    }

    @Override
    public void delete(Long vehicleId) {
        Optional<Vehicle> optionalVehicle = vehicleRepository.findById(vehicleId);
        if (optionalVehicle.isEmpty()) {
            throw new CustomException(404, "Vehicle not found");
        }
        vehicleRepository.delete(optionalVehicle.get());
    }

    @Override
    public VehicleDTO updateStatus(Long vehicleId, VehicleDTO dto) {
        Optional<Vehicle> optionalVehicle = vehicleRepository.findById(vehicleId);
        if (optionalVehicle.isEmpty()) {
            throw new CustomException(404, "Vehicle not found");
        }
        if (dto.getStatus() == null) {
            throw new CustomException(400, "Status is required");
        }
        Vehicle vehicle = optionalVehicle.get();
        vehicle.setStatus(dto.getStatus());
        return toDto(vehicleRepository.save(vehicle));
    }

    @Override
    public List<VehicleDTO> getAll() {
        List<VehicleDTO> list = new ArrayList<>();
        for (Vehicle v : vehicleRepository.findAll()) {
            list.add(toDto(v));
        }
        return list;
    }

    private VehicleDTO toDto(Vehicle v) {
        return new VehicleDTO(v.getVehicleId(), v.getType(), v.getPlateNumber(), v.getCourier().getCourierId(), v.getStatus());
    }
}