package com.ijse.codlink.security;

import com.ijse.codlink.entity.*;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecordAccess {
    private final CustomerRepository customerRepository;
    private final ParcelRepository parcelRepository;
    private final ShipmentRepository shipmentRepository;
    private final CourierRepository courierRepository;
    private final UserRepository userRepository;

    public boolean hasRole(String role) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    public String username() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new CustomException(401, "Login required");
        return auth.getName();
    }

    public User user() {
        User user = userRepository.findByUsername(username())
                .orElseThrow(() -> new CustomException(401, "Account not found"));
        if (!Boolean.TRUE.equals(user.getActive())) throw new CustomException(401, "Account inactive");
        return user;
    }

    public Courier courier() {
        if (!hasRole("COURIER")) throw new CustomException(403, "Courier access required");
        return courierRepository.findByUser_UserId(user().getUserId())
                .orElseThrow(() -> new CustomException(403, "Courier profile required"));
    }

    public boolean owns(Customer customer) {
        return customer != null && customer.getUser() != null
                && username().equals(customer.getUser().getUsername());
    }

    public void customer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomException(404, "Customer not found"));
        if (!hasRole("ADMIN") && !owns(customer)) throw new CustomException(403, "Customer access denied");
    }

    public void shipment(Long id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new CustomException(404, "Shipment not found"));
        if (!hasRole("ADMIN") && !hasRole("RECEPTIONIST") && !owns(shipment.getCustomer())) {
            throw new CustomException(403, "Shipment access denied");
        }
    }

    public boolean canRead(Parcel parcel) {
        return hasRole("ADMIN") || owns(parcel.getCustomer()) || owns(parcel.getAssignedCourier());
    }

    public void assigned(Parcel parcel) {
        if (!hasRole("ADMIN") && !owns(parcel.getAssignedCourier())) {
            throw new CustomException(403, "Claim this parcel before changing it");
        }
    }

    public void parcel(Long id) {
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new CustomException(404, "Parcel not found"));
        if (!canRead(parcel)) throw new CustomException(403, "Parcel access denied");
    }

    public boolean owns(Courier courier) {
        return hasRole("COURIER") && courier != null && courier.getUser() != null
                && username().equals(courier.getUser().getUsername());
    }

    public void courier(Long id) {
        Courier courier = courierRepository.findById(id)
                .orElseThrow(() -> new CustomException(404, "Courier not found"));
        if (!hasRole("ADMIN") && !owns(courier)) throw new CustomException(403, "Courier access denied");
    }
}
