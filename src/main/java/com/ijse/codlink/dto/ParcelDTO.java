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
    @jakarta.validation.constraints.Positive
    private Long parcelId;
    @jakarta.validation.constraints.Positive
    private Long assignedCourierId;
    @jakarta.validation.constraints.Size(max = 250)
    private String trackingNumber;
    @jakarta.validation.constraints.Size(max = 250)
    private String customerName;
    @jakarta.validation.constraints.Size(max = 250)
    private String originBranchName;
    @jakarta.validation.constraints.Size(max = 250)
    private String currentBranchName;
    private Double weight;
    private BigDecimal codAmount;
    private ParcelStatus status;
    @jakarta.validation.constraints.Positive
    private Long customerId;
    @jakarta.validation.constraints.Positive
    private Long originBranchId;
    @jakarta.validation.constraints.Positive
    private Long currentBranchId;
    @jakarta.validation.constraints.Positive
    private Long shipmentId;   // shipment can be nullable
    private BigDecimal shippingFee;
    private BigDecimal returnFee;
    private Boolean needsTransfer;
    @jakarta.validation.Valid
    private AddressDTO pickupAddress;
    @jakarta.validation.Valid
    private AddressDTO deliveryAddress;
    private Boolean isCod;  // new status

}
