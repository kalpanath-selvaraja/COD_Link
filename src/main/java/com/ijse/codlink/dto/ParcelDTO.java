package com.ijse.codlink.dto;

import com.ijse.codlink.enumeration.ParcelStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParcelDTO {
    private Long parcelId;
    private String trackingNumber;
    private Double weight;
    private Double codAmount;
    private ParcelStatus status;
    private Long customerId;
    private Long originBranchId;
    private Long shipmentId;   // shipment can be nullable
    private AddressDTO pickupAddress;
    private AddressDTO deliveryAddress;
}
