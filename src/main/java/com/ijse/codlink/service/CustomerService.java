package com.ijse.codlink.service;

import com.ijse.codlink.dto.CustomerDTO;

public interface CustomerService {
    CustomerDTO createMyProfile(String username, CustomerDTO dto);

    CustomerDTO getMyProfile(String username);
    CustomerDTO createWalkIn(CustomerDTO dto);
    CustomerDTO updateMyProfile(String username, CustomerDTO dto);

    CustomerDTO getById(Long customerId);
    java.util.List<CustomerDTO> getAll();
    void delete(Long customerId);
}
