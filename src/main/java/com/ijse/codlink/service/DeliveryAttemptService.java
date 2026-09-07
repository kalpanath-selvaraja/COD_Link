package com.ijse.codlink.service;

import com.ijse.codlink.dto.DeliveryAttemptDTO;

import java.util.List;

public interface DeliveryAttemptService {
    DeliveryAttemptDTO  create(String username, DeliveryAttemptDTO dto);
    List<DeliveryAttemptDTO> getByParcelId(Long parcelId);
}