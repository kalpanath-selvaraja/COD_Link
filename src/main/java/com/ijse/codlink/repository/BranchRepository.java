package com.ijse.codlink.repository;

import com.ijse.codlink.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch,Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select b from Branch b where b.branchId = :id")
    java.util.Optional<Branch> findLockedById(@org.springframework.data.repository.query.Param("id") Long id);

}
