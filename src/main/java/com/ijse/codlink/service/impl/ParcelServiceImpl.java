package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.AddressDTO;
import com.ijse.codlink.dto.ParcelDTO;
import com.ijse.codlink.entity.*;
import com.ijse.codlink.enumeration.ParcelStatus;
import com.ijse.codlink.enumeration.ShipmentStatus;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.*;
import com.ijse.codlink.service.ParcelService;
import com.ijse.codlink.service.PricingRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParcelServiceImpl implements ParcelService {

    private final ParcelRepository parcelRepository;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final ShipmentRepository shipmentRepository;
    private final ServiceZoneRepository serviceZoneRepository;
    private final UserRepository userRepository;
    private final PricingRuleService pricingRuleService;
    private final InvoiceRepository invoiceRepository;

    private static final BigDecimal RETURN_SURCHARGE_MULTIPLIER = new BigDecimal("1.10");
    private static final Map<ParcelStatus, Set<ParcelStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(ParcelStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(ParcelStatus.BOOKED, EnumSet.of(ParcelStatus.IN_TRANSIT));
        ALLOWED_TRANSITIONS.put(ParcelStatus.IN_TRANSIT, EnumSet.of(ParcelStatus.DELIVERED, ParcelStatus.FAILED));
        ALLOWED_TRANSITIONS.put(ParcelStatus.FAILED, EnumSet.of(ParcelStatus.RETURNED, ParcelStatus.IN_TRANSIT));
        ALLOWED_TRANSITIONS.put(ParcelStatus.DELIVERED, EnumSet.noneOf(ParcelStatus.class));
        ALLOWED_TRANSITIONS.put(ParcelStatus.RETURNED, EnumSet.noneOf(ParcelStatus.class));
    }

    @Override
    public ParcelDTO createForCustomer(Long customerId, ParcelDTO parcelDTO) {
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        if (optionalCustomer.isEmpty()) {
            throw new CustomException(404, "Customer not found");
        }
        return toDto(buildAndSaveParcel(optionalCustomer.get(), parcelDTO));
    }

    @Override
    public ParcelDTO createMine(String username, ParcelDTO parcelDTO) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            throw new CustomException(404, "User not found");
        }
        Optional<Customer> optionalCustomer = customerRepository.findByUser_UserId(optionalUser.get().getUserId());
        if (optionalCustomer.isEmpty()) {
            throw new CustomException(404, "No customer profile linked to this account — create one at POST /api/customers/me first");
        }
        return toDto(buildAndSaveParcel(optionalCustomer.get(), parcelDTO));
    }


    private Parcel buildAndSaveParcel(Customer customer, ParcelDTO parcelDTO) {
        Optional<Branch> optionalBranch = branchRepository.findById(parcelDTO.getOriginBranchId());
        if (optionalBranch.isEmpty()) {
            throw new CustomException(404, "Origin Branch not found");
        }

        Shipment shipment = null;
        if (parcelDTO.getShipmentId() != null) {
            Optional<Shipment> optionalShipment = shipmentRepository.findById(parcelDTO.getShipmentId());
            if (optionalShipment.isEmpty()) {
                throw new CustomException(404, "Shipment not found");
            }
            shipment = optionalShipment.get();
        }


        Address pickupAddress = toAddress(parcelDTO.getPickupAddress());
        Address deliveryAddress = toAddress(parcelDTO.getDeliveryAddress());

        if (deliveryAddress.getServiceZone() == null) {
            throw new CustomException(400, "Delivery address must include a service zone so shipping fee can be calculated");
        }
        if (parcelDTO.getWeight() == null || parcelDTO.getWeight() <= 0) {
            throw new CustomException(400, "Weight must be greater than zero");
        }

        boolean isCod = parcelDTO.getCodAmount() != null && parcelDTO.getCodAmount().compareTo(BigDecimal.ZERO) > 0;
        BigDecimal fee = pricingRuleService.calculateShippingFee(
                deliveryAddress.getServiceZone().getZoneId(), parcelDTO.getWeight(), isCod);

        Parcel parcel = new Parcel();
        parcel.setTrackingNumber("CDL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        parcel.setWeight(parcelDTO.getWeight());
        parcel.setCodAmount(parcelDTO.getCodAmount());
        parcel.setShippingFee(fee.doubleValue());
        parcel.setStatus(ParcelStatus.BOOKED);
        parcel.setCustomer(customer);
        parcel.setOriginBranch(optionalBranch.get());
        parcel.setCurrentBranch(optionalBranch.get());
        parcel.setShipment(shipment);
        parcel.setPickupAddress(pickupAddress);
        parcel.setDeliveryAddress(deliveryAddress);

        return parcelRepository.save(parcel);
    }

    private Address toAddress(AddressDTO dto) {
        if (dto == null) {
            throw new CustomException(400, "Pickup and delivery addresses are both required");
        }
        Address address = new Address();
        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setPostalCode(dto.getPostalCode());

        if (dto.getZoneId() != null) {
            Optional<ServiceZone> zone = serviceZoneRepository.findById(dto.getZoneId());
            if (zone.isEmpty()) {
                throw new CustomException(404, "Service zone not found");
            }
            address.setServiceZone(zone.get());
        }
        return address;
    }

    @Override
    public ParcelDTO getById(Long parcelId) {
        Optional<Parcel> optionalParcel = parcelRepository.findById(parcelId);
        if (optionalParcel.isEmpty()) {
            throw new CustomException(404, "Parcel not found");
        }
        return toDto(optionalParcel.get());
    }

    @Override
    public ParcelDTO getByTrackingNumber(String trackingNumber) {
        Optional<Parcel> optionalParcel = parcelRepository.findByTrackingNumber(trackingNumber);
        if (optionalParcel.isEmpty()) {
            throw new CustomException(404, "No Parcel with that tracking number was found");
        }
        return toDto(optionalParcel.get());
    }

    @Override
    public List<ParcelDTO> getByCustomerId(Long customerId) {
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        if (optionalCustomer.isEmpty()) {
            throw new CustomException(404, "Customer not found");
        }
        List<ParcelDTO> parcelDTOList = new ArrayList<>();
        for (Parcel parcel : parcelRepository.findByCustomer_CustomerId(customerId)) {
            parcelDTOList.add(toDto(parcel));
        }
        return parcelDTOList;
    }




    @Override
    public ParcelDTO updateStatus(Long parcelId, ParcelDTO statusDto) {
        Optional<Parcel> optionalParcel = parcelRepository.findById(parcelId);
        if (optionalParcel.isEmpty()) {
            throw new CustomException(404, "Parcel not found");
        }
        if (statusDto.getStatus() == null) {
            throw new CustomException(400, "Status not found");
        }
        Parcel parcel = optionalParcel.get();
        ParcelStatus currentStatus = parcel.getStatus();
        ParcelStatus targetStatus = statusDto.getStatus();

        Set<ParcelStatus> allowedNext = ALLOWED_TRANSITIONS.get(currentStatus);
        if (allowedNext == null || !allowedNext.contains(targetStatus)) {
            throw new CustomException(409,
                    "Cannot transition parcel from " + currentStatus + " to " + targetStatus);
        }

        if (targetStatus == ParcelStatus.RETURNED) {
            if (parcel.getDeliveryAddress() == null || parcel.getDeliveryAddress().getServiceZone() == null) {
                throw new CustomException(400, "Cannot calculate return fee — parcel has no delivery zone on record");
            }
            boolean isCod = parcel.getCodAmount() != null && parcel.getCodAmount().compareTo(BigDecimal.ZERO) > 0;
            BigDecimal baseFee = pricingRuleService.calculateShippingFee(
                    parcel.getDeliveryAddress().getServiceZone().getZoneId(), parcel.getWeight(), isCod);
            BigDecimal returnFee = baseFee.multiply(RETURN_SURCHARGE_MULTIPLIER).setScale(2, RoundingMode.HALF_UP);
            parcel.setReturnFee(returnFee.doubleValue());
        }

        parcel.setStatus(targetStatus);
        return toDto(parcelRepository.save(parcel));
    }

    @Override
    public void delete(Long parcelId) {
        Optional<Parcel> optionalParcel = parcelRepository.findById(parcelId);
        if (optionalParcel.isEmpty()) {
            throw new CustomException(404, "Parcel not found");
        }
        if (optionalParcel.get().getStatus() != ParcelStatus.BOOKED) {
            throw new CustomException(409, "Only parcels still in BOOKED status can be deleted");
        }
        parcelRepository.delete(optionalParcel.get());
    }

    @Override
    public ParcelDTO assignToShipment(Long parcelId, Long shipmentId) {
        Optional<Parcel> optionalParcel = parcelRepository.findById(parcelId);
        if (optionalParcel.isEmpty()) {
            throw new CustomException(404, "Parcel not found");
        }
        Optional<Shipment> optionalShipment = shipmentRepository.findById(shipmentId);
        if (optionalShipment.isEmpty()) {
            throw new CustomException(404, "Shipment not found");
        }

        Parcel parcel = optionalParcel.get();
        Shipment shipment = optionalShipment.get();

        if (shipment.getStatus() == ShipmentStatus.CANCELLED) {
            throw new CustomException(409, "Cannot attach a parcel to a cancelled shipment");
        }
        if (invoiceRepository.existsByShipment_ShipmentId(shipmentId)) {
            throw new CustomException(409, "Shipment already has an invoice — attaching more parcels would make it stale. No reissue endpoint exists yet.");
        }
        if (parcel.getShipment() != null) {
            throw new CustomException(409, "Parcel is already attached to shipment " + parcel.getShipment().getShipmentId());
        }
        if (parcel.getStatus() != ParcelStatus.BOOKED) {
            throw new CustomException(409, "Only BOOKED parcels can be assigned to a shipment — current status is " + parcel.getStatus());
        }
        if (!parcel.getCustomer().getCustomerId().equals(shipment.getCustomer().getCustomerId())) {
            throw new CustomException(409, "Parcel's customer does not match shipment's customer");
        }

        parcel.setShipment(shipment);
        return toDto(parcelRepository.save(parcel));
    }

    @Override
    public List<ParcelDTO> getAll() {
        List<ParcelDTO> list = new ArrayList<>();
        for (Parcel parcel : parcelRepository.findAllWithCustomerAndBranch()) {
            list.add(toDto(parcel));
        }
        return list;
    }

    private ParcelDTO toDto(Parcel parcel) {
        ParcelDTO dto = new ParcelDTO();
        dto.setParcelId(parcel.getParcelId());
        dto.setTrackingNumber(parcel.getTrackingNumber());
        dto.setCustomerName(parcel.getCustomer() != null ? parcel.getCustomer().getName() : null);
        dto.setOriginBranchName(parcel.getOriginBranch() != null ? parcel.getOriginBranch().getBranchName() : null);
        dto.setWeight(parcel.getWeight());
        dto.setCodAmount(parcel.getCodAmount());
        dto.setShippingFee(parcel.getShippingFee());
        dto.setStatus(parcel.getStatus());
        dto.setCustomerId(parcel.getCustomer() != null ? parcel.getCustomer().getCustomerId() : null);
        dto.setOriginBranchId(parcel.getOriginBranch() != null ? parcel.getOriginBranch().getBranchId() : null);
        
        Branch current = parcel.getCurrentBranch() != null ? parcel.getCurrentBranch() : parcel.getOriginBranch();
        if (current != null) {
            dto.setCurrentBranchId(current.getBranchId());
            dto.setCurrentBranchName(current.getBranchName());
        }
        
        dto.setShipmentId(parcel.getShipment() != null ? parcel.getShipment().getShipmentId() : null);
        dto.setPickupAddress(toAddressDto(parcel.getPickupAddress()));
        dto.setDeliveryAddress(toAddressDto(parcel.getDeliveryAddress()));
        dto.setReturnFee(parcel.getReturnFee());
        dto.setNeedsTransfer(needsTransfer(parcel));
        return dto;
    }

    private Boolean needsTransfer(Parcel parcel) {
        Branch current = parcel.getCurrentBranch() != null ? parcel.getCurrentBranch() : parcel.getOriginBranch();
        if (current == null
                || current.getAddress() == null
                || current.getAddress().getServiceZone() == null) {
            return null; // current branch has no zone on record
        }
        if (parcel.getDeliveryAddress() == null || parcel.getDeliveryAddress().getServiceZone() == null) {
            return null; // shouldnt happen postbooking
        }
        Long currentZoneId = current.getAddress().getServiceZone().getZoneId();
        Long deliveryZoneId = parcel.getDeliveryAddress().getServiceZone().getZoneId();
        return !currentZoneId.equals(deliveryZoneId);
    }


    private AddressDTO toAddressDto(Address address) {
        if (address == null) return null;

        return new AddressDTO(
                address.getAddressId(),
                address.getStreet(),
                address.getCity(),
                address.getPostalCode(),
                address.getServiceZone() != null ? address.getServiceZone().getZoneId() : null);
    }
}