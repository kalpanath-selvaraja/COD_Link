package com.ijse.codlink.service;

import com.ijse.codlink.dto.CustomerDTO;
import com.ijse.codlink.dto.CustomerLinkDTO;

public interface CustomerService {
    CustomerDTO createMyProfile(String username, CustomerDTO dto);

    CustomerDTO getMyProfile(String username);
    CustomerDTO createWalkIn(CustomerDTO dto);
    CustomerDTO updateMyProfile(String username, CustomerDTO dto);

    CustomerDTO getById(Long customerId);
    java.util.List<CustomerDTO> getAll();
    void delete(Long customerId);

    CustomerDTO linkToMyAccount(String username, CustomerLinkDTO dto);
}

