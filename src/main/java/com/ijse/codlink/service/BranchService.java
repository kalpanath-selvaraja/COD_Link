package com.ijse.codlink.service;

import com.ijse.codlink.dto.BranchDTO;

import java.util.List;

public interface BranchService {



    BranchDTO createBranch(BranchDTO branchDTO);
    BranchDTO getById (Long branchId);
    List<BranchDTO> getAll();
    BranchDTO updateBranch(Long branchId,  BranchDTO branchDTO);
    void deleteBranch(Long branchId);
}
