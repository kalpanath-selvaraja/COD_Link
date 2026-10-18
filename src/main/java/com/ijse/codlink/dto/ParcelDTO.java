package com.ijse.codlink.dto;

import com.ijse.codlink.enumeration.ParcelStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParcelDTO {
    private Long parcelId;
    private Long assignedCourierId;
    private String trackingNumber;
    private String customerName;
    private String originBranchName;
    private String currentBranchName;
    private Double weight;
    private BigDecimal codAmount;
    private ParcelStatus status;
    private Long customerId;
    private Long originBranchId;
    private Long currentBranchId;
    private Long shipmentId;   // shipment can be nullable
    private BigDecimal shippingFee;
    private BigDecimal returnFee;
    private Boolean needsTransfer;
    private AddressDTO pickupAddress;
    private AddressDTO deliveryAddress;
    private Boolean isCod;  // new status

}
