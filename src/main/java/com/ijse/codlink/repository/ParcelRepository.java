package com.ijse.codlink.repository;

import com.ijse.codlink.entity.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParcelRepository extends JpaRepository<Parcel,Long> {

    boolean existsByTrackingNumber(String candidate);
}
