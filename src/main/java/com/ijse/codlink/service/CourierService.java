package com.ijse.codlink.service;

import com.ijse.codlink.dto.CourierDTO;

import java.util.List;

public interface CourierService {
    CourierDTO createCourierAccount(CourierDTO courierDTO);
    CourierDTO getById(Long id);
    List<CourierDTO> getByBranch(Long branchId);
    CourierDTO updateCourierAccount(Long courierId ,CourierDTO courierDTO);
    void delete(Long courierId);

    List<CourierDTO> getAll();

    CourierDTO getMyProfile(String username);

}
