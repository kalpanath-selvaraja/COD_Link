package com.ijse.codlink.service;

import com.ijse.codlink.dto.ReconciliationBatchDTO;

import java.util.List;

public interface ReconciliationBatchService {
    ReconciliationBatchDTO createForBranch(Long branchId);
    List<ReconciliationBatchDTO> getByBranchId(Long branchId);
}