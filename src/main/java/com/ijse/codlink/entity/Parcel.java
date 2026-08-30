package com.ijse.codlink.entity;

import com.ijse.codlink.enumeration.ParcelStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "parcels")
public class Parcel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long parcelId;

    @Column(unique = true)
    private String trackingNumber;

    private Double weight;

    private BigDecimal codAmount;

    private Double shippingFee;
    private Double returnFee;

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

    @ManyToOne
    @JoinColumn(name = "current_branch_id")
    @ToString.Exclude
    private Branch currentBranch;

    @ManyToOne
    @JoinColumn(name = "shipment_id")
    @ToString.Exclude
    private Shipment shipment;




}
