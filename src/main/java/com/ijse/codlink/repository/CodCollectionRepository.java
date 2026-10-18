package com.ijse.codlink.repository;

import com.ijse.codlink.entity.CodCollection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodCollectionRepository extends JpaRepository<CodCollection, Long> {
    List<CodCollection> findByCourier_CourierId(Long courierId);
    boolean existsByParcel_ParcelId(Long parcelId);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select c from CodCollection c where c.collectionBranch.branchId = :branchId "
            + "and c.reconciliationBatch is null order by c.collectionId")
    List<CodCollection> findUnbatchedForUpdate(@org.springframework.data.repository.query.Param("branchId") Long branchId);

    boolean existsByCollectionBranchIsNullAndReconciliationBatchIsNull();

    List<CodCollection> findAll();
}