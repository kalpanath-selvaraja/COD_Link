package com.ijse.codlink.service;

import com.ijse.codlink.dto.CodCollectionDTO;

import java.util.List;

public interface CodCollectionService {
    CodCollectionDTO create(String username , CodCollectionDTO dto);
    List<CodCollectionDTO> getByCourierId(Long courierId);

    List<CodCollectionDTO> getAll();
}