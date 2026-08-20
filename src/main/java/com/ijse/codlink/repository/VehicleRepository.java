package com.ijse.codlink.repository;

import com.ijse.codlink.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByCourier_CourierId(Long courierId);
}
