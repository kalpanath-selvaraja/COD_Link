package com.ijse.codlink.service;

import com.ijse.codlink.dto.CodCollectionDTO;

import java.util.List;

public interface CodCollectionService {
    CodCollectionDTO create(CodCollectionDTO dto);
    List<CodCollectionDTO> getByCourierId(Long courierId);
}