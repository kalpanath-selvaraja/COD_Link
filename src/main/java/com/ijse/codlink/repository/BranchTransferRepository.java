package com.ijse.codlink.repository;

import com.ijse.codlink.entity.BranchTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BranchTransferRepository extends JpaRepository<BranchTransfer, Long> {
    List<BranchTransfer> findByParcel_ParcelId(Long parcelId);

    List<BranchTransfer> findAll();
}