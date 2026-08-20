package com.ijse.codlink.repository;

import com.ijse.codlink.entity.ReconciliationBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReconciliationBatchRepository extends JpaRepository<ReconciliationBatch, Long> {
    List<ReconciliationBatch> findByBranch_BranchId(Long branchId);
}