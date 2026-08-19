package com.ijse.codlink.repository;

import com.ijse.codlink.entity.Courier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourierRepository extends JpaRepository<Courier,Long> {
    List<Courier> findByBranch_BranchId(Long branchBranchId);
}
