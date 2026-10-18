package com.ijse.codlink.service;

import com.ijse.codlink.dto.AddressDTO;
import com.ijse.codlink.dto.ParcelDTO;
import com.ijse.codlink.entity.Customer;
import com.ijse.codlink.enumeration.UserStatus;
import com.ijse.codlink.exception.CustomException;
import java.math.BigDecimal;

public class BookingValidation {
    public static void validate(Customer customer, ParcelDTO dto, boolean online) {
        if (customer.getStatus() != UserStatus.ACTIVE) throw new CustomException(409, "Customer is inactive");
        if (dto == null || dto.getOriginBranchId() == null || dto.getWeight() == null
                || !Double.isFinite(dto.getWeight()) || dto.getWeight() <= 0 || dto.getWeight() > 70) {
            throw new CustomException(400, "Origin branch and weight between 0 and 70kg are required");
        }
        BigDecimal cod = dto.getCodAmount();
        if (cod != null && (cod.signum() < 0 || cod.scale() > 2)) {
            throw new CustomException(400, "COD amount must be nonnegative with at most two decimal places");
        }
        if (online && (cod == null || cod.signum() <= 0 || dto.getShipmentId() != null)) {
            throw new CustomException(400, "Online bookings require a positive COD amount and no shipment ID");
        }
        address(dto.getPickupAddress());
        address(dto.getDeliveryAddress());
    }

    private static void address(AddressDTO address) {
        if (address == null || address.getZoneId() == null || address.getStreet() == null
                || address.getStreet().isBlank() || address.getCity() == null || address.getCity().isBlank()
                || address.getPostalCode() == null || address.getPostalCode().isBlank()) {
            throw new CustomException(400, "Street, city, postal code and service zone are required");
        }
    }
}
