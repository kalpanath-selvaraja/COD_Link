package com.ijse.codlink.entity;

import com.ijse.codlink.enumeration.ParcelStatus;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Parcel {
    @Id
    private Long parcelId;

    private String tracking_number;
    private Double weight;
    private Double cod_amount;

    @Enumerated(EnumType.STRING)
    private ParcelStatus status;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    @ToString.Exclude
    private Customer customer;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "pickup_address_id")
    @ToString.Exclude
    private Address pickupAddress;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "delivery_address_id")
    @ToString.Exclude
    private Address deliveryAddress;

    @ManyToOne
    @JoinColumn(name = "origin_branch_id")
    @ToString.Exclude
    private Branch originBranch;




//
//    @ManyToOne
//    @JoinColumn(name = "shipment_id")
//    @ToString.Exclude
//    private Shipment shipment;




}
