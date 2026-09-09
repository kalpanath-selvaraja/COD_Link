package com.ijse.codlink.repository;

import com.ijse.codlink.entity.CodCollection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodCollectionRepository extends JpaRepository<CodCollection, Long> {
    List<CodCollection> findByCourier_CourierId(Long courierId);
    boolean existsByParcel_ParcelId(Long parcelId);
    List<CodCollection> findByCourier_Branch_BranchIdAndReconciliationBatchIsNull(Long branchId);  // use by Recocliation

    List<CodCollection> findAll();
}