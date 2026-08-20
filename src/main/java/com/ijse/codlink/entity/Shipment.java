package com.ijse.codlink.entity;

import com.ijse.codlink.enumeration.ShipmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@Table(name = "shipments")
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shipmentId;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status = ShipmentStatus.ACTIVE;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    @ToString.Exclude
    private Customer customer;

}

