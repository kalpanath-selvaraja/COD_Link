package com.ijse.codlink.entity;

import com.ijse.codlink.enumeration.VehicleStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vehicleId;
    private String type;
    private String plateNumber;

    @ManyToOne
    @JoinColumn(name = "courier_id", nullable = false)
    @ToString.Exclude
    private Courier courier;

    @Enumerated(EnumType.STRING)
    private VehicleStatus status = VehicleStatus.ACTIVE;


}
