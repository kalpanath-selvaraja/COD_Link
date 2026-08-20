package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.ReconciliationBatchDTO;
import com.ijse.codlink.entity.Branch;
import com.ijse.codlink.entity.CodCollection;
import com.ijse.codlink.entity.ReconciliationBatch;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.BranchRepository;
import com.ijse.codlink.repository.CodCollectionRepository;
import com.ijse.codlink.repository.ReconciliationBatchRepository;
import com.ijse.codlink.service.ReconciliationBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReconciliationBatchServiceImpl implements ReconciliationBatchService {

    private final ReconciliationBatchRepository reconciliationBatchRepository;
    private final BranchRepository branchRepository;
    private final CodCollectionRepository codCollectionRepository;

    @Override
    @Transactional
    public ReconciliationBatchDTO createForBranch(Long branchId) {
        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty()) {
            throw new CustomException(404, "Branch not found");
        }

        List<CodCollection> unbatched = codCollectionRepository
                .findByCourier_Branch_BranchIdAndReconciliationBatchIsNull(branchId);

        if (unbatched.isEmpty()) {
            throw new CustomException(400, "No unbatched COD collections for this branch");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (CodCollection c : unbatched) {
            total = total.add(c.getAmount());
        }

        ReconciliationBatch batch = new ReconciliationBatch();
        batch.setBranch(optionalBranch.get());
        batch.setTotal(total);
        batch.setReconciledAt(LocalDateTime.now());
        ReconciliationBatch savedBatch = reconciliationBatchRepository.save(batch);

        for (CodCollection c : unbatched) {
            c.setReconciliationBatch(savedBatch);
            codCollectionRepository.save(c);
        }

        return toDto(savedBatch);
    }

    @Override
    public List<ReconciliationBatchDTO> getByBranchId(Long branchId) {
        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        if (optionalBranch.isEmpty()) {
            throw new CustomException(404, "Branch not found");
        }
        List<ReconciliationBatchDTO> list = new ArrayList<>();
        for (ReconciliationBatch b : reconciliationBatchRepository.findByBranch_BranchId(branchId)) {
            list.add(toDto(b));
        }
        return list;
    }

    private ReconciliationBatchDTO toDto(ReconciliationBatch b) {
        return new ReconciliationBatchDTO(b.getBatchId(), b.getBranch().getBranchId(),
                b.getTotal(), b.getReconciledAt());
    }
}