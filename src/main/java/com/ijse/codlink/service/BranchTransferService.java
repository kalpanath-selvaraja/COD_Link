package com.ijse.codlink.service;

import com.ijse.codlink.dto.BranchTransferDTO;

import java.util.List;

public interface BranchTransferService {
    BranchTransferDTO create(BranchTransferDTO dto);
    List<BranchTransferDTO> getByParcelId(Long parcelId);
}