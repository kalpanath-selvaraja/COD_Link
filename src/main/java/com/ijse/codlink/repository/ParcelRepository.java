package com.ijse.codlink.repository;

import com.ijse.codlink.entity.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ParcelRepository extends JpaRepository<Parcel,Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Parcel p where p.parcelId = :id")
    Optional<Parcel> findLockedById(@org.springframework.data.repository.query.Param("id") Long id);

    List<Parcel> findByAssignedCourier_CourierId(Long courierId);

    @Query("select p from Parcel p where p.assignedCourier is null "
            + "and coalesce(p.currentBranch.branchId, p.originBranch.branchId) = :branchId "
            + "and p.status in (com.ijse.codlink.enumeration.ParcelStatus.BOOKED, "
            + "com.ijse.codlink.enumeration.ParcelStatus.IN_TRANSIT, com.ijse.codlink.enumeration.ParcelStatus.FAILED)")
    List<Parcel> findAvailable(@org.springframework.data.repository.query.Param("branchId") Long branchId);



    Optional<Parcel> findByTrackingNumber(String trackingNumber);

    List<Parcel> findByCustomer_CustomerId(Long customerId);

    boolean existsByShipment_ShipmentId(Long shipmentId);

    List<Parcel> findByShipment_ShipmentId(Long shipmentId);

    @Query("SELECT p FROM Parcel p " +
            "JOIN FETCH p.customer c " +
            "JOIN FETCH p.originBranch b " +
            "LEFT JOIN FETCH p.currentBranch cb " +
            "LEFT JOIN FETCH p.shipment s")
    List<Parcel> findAllWithCustomerAndBranch();
}
