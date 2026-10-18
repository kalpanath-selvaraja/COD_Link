package com.ijse.codlink.repository;

import com.ijse.codlink.entity.ServiceZone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceZoneRepository extends JpaRepository<ServiceZone,Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select e from ServiceZone e where e.zoneId = :id")
    java.util.Optional<ServiceZone> findLockedById(@org.springframework.data.repository.query.Param("id") Long id);

}
