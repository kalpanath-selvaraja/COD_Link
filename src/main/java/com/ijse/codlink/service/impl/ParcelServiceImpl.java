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
import com.ijse.codlink.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParcelServiceImpl implements ParcelService {

    private final com.ijse.codlink.service.ParcelLifecycle parcelLifecycle;
    private final ParcelRepository parcelRepository;
    private final CourierRepository courierRepository;
    private final com.ijse.codlink.security.RecordAccess recordAccess;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentService shipmentService;
    private final ServiceZoneRepository serviceZoneRepository;
    private final UserRepository userRepository;
    private final PricingRuleService pricingRuleService;
    private final InvoiceRepository invoiceRepository;

    private static final BigDecimal CROSS_ZONE_PICKUP_SURCHARGE_MULTIPLIER = new BigDecimal("1.15");
    private static final int MAX_PARCELS_PER_CART = 20;
    private static final double MAX_PARCEL_WEIGHT_KG = 70.0;

    @Override
    @Transactional
    public ParcelDTO createForCustomer(Long customerId, ParcelDTO parcelDTO) {
        log.info("Execute method createForCustomer()");
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        if (optionalCustomer.isEmpty()) {
            log.error("Customer not found with ID: {}", customerId);
            throw new CustomException(404, "Customer not found");
        }
        return toDto(buildAndSaveParcel(optionalCustomer.get(), parcelDTO));
    }

    @Override
    @Transactional
    public List<ParcelDTO> bookCartForCustomer(String username, List<ParcelDTO> parcelDtos) {
        log.info("Execute method bookCartForCustomer()");
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            log.error("User not found with username: {}", username);
            throw new CustomException(404, "User not found");
        }
        Optional<Customer> optionalCustomer = customerRepository.findByUser_UserId(optionalUser.get().getUserId());
        if (optionalCustomer.isEmpty()) {
            log.error("No customer profile linked to user ID: {}", optionalUser.get().getUserId());
            throw new CustomException(404, "No customer profile linked to this account — create one at POST /api/customers/me first");
        }

        if (parcelDtos != null) {
            for (ParcelDTO dto : parcelDtos) com.ijse.codlink.service.BookingValidation.validate(optionalCustomer.get(), dto, true);
            for (int i = 0; i < parcelDtos.size(); i++) {
                ParcelDTO dto = parcelDtos.get(i);
                if (dto == null || dto.getCodAmount() == null || dto.getCodAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    log.error("Parcel at position {} is missing a COD amount", (i + 1));
                    throw new CustomException(400, "Parcel at position " + (i + 1)
                            + " is missing a COD amount — online bookings are cash-on-delivery only");
                }
            }
        }

        return processCartBooking(optionalCustomer.get(), parcelDtos);
    }

    @Override
    @Transactional
    public List<ParcelDTO> bookCartForWalkIn(Long customerId, List<ParcelDTO> parcelDtos) {
        log.info("Execute method bookCartForWalkIn()");
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        if (optionalCustomer.isEmpty()) {
            log.error("Customer not found with ID: {}", customerId);
            throw new CustomException(404, "Customer not found");
        }
        return processCartBooking(optionalCustomer.get(), parcelDtos);
    }

    private List<ParcelDTO> processCartBooking(Customer customer, List<ParcelDTO> parcelDtos) {
        if (parcelDtos == null || parcelDtos.isEmpty()) {
            throw new CustomException(400, "Cart is empty — add at least one parcel before booking");
        }
        if (parcelDtos.size() > MAX_PARCELS_PER_CART) {
            throw new CustomException(400, "Cannot book more than " + MAX_PARCELS_PER_CART + " parcels in one request");
        }

        for (int i = 0; i < parcelDtos.size(); i++) {
            ParcelDTO dto = parcelDtos.get(i);
            if (dto == null) {
                throw new CustomException(400, "Parcel at position " + (i + 1) + " is missing");
            }
            if (dto.getWeight() == null || dto.getWeight() <= 0) {
                throw new CustomException(400, "Parcel at position " + (i + 1) + " needs a weight greater than zero");
            }
            if (dto.getWeight() > MAX_PARCEL_WEIGHT_KG) {
                throw new CustomException(400, "Parcel at position " + (i + 1) + " exceeds the maximum weight of "
                        + MAX_PARCEL_WEIGHT_KG + "kg for cart booking — process oversized items individually");
            }
            if (dto.getCodAmount() != null && dto.getCodAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw new CustomException(400, "Parcel at position " + (i + 1) + " has a negative COD amount");
            }
            if (dto.getOriginBranchId() == null) {
                throw new CustomException(400, "Parcel at position " + (i + 1) + " is missing an origin branch");
            }
            if (dto.getPickupAddress() == null || dto.getDeliveryAddress() == null) {
                throw new CustomException(400, "Parcel at position " + (i + 1) + " is missing pickup or delivery address");
            }
            if (dto.getDeliveryAddress().getZoneId() == null) {
                throw new CustomException(400, "Parcel at position " + (i + 1) + " delivery address needs a service zone");
            }
            dto.setShipmentId(null);
        }

        List<Parcel> createdParcels = new ArrayList<>();
        for (ParcelDTO dto : parcelDtos) {
            createdParcels.add(buildAndSaveParcel(customer, dto));
        }

        Map<String, List<Parcel>> groups = new LinkedHashMap<>();
        for (Parcel parcel : createdParcels) {
            Long zoneId = parcel.getDeliveryAddress().getServiceZone().getZoneId();
            boolean isCod = Boolean.TRUE.equals(parcel.getIsCod());
            String key = zoneId + "|" + isCod;
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(parcel);
        }

        List<ParcelDTO> result = new ArrayList<>();
        for (List<Parcel> group : groups.values()) {
            Shipment shipment = new Shipment();
            shipment.setCreatedAt(java.time.LocalDateTime.now());
            shipment.setCustomer(customer);
            Shipment savedShipment = shipmentRepository.save(shipment);

            for (Parcel parcel : group) {
                parcel.setShipment(savedShipment);
                parcelRepository.save(parcel);
            }

            shipmentService.finalizeShipment(savedShipment.getShipmentId());

            for (Parcel parcel : group) {
                result.add(toDto(parcelRepository.findById(parcel.getParcelId()).orElseThrow()));
            }
        }

        return result;
    }

    @Override
    @Transactional
    public ParcelDTO createMine(String username, ParcelDTO parcelDTO) {
        log.info("Execute method createMine()");
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            log.error("User not found with username: {}", username);
            throw new CustomException(404, "User not found");
        }
        Optional<Customer> optionalCustomer = customerRepository.findByUser_UserId(optionalUser.get().getUserId());
        if (optionalCustomer.isEmpty()) {
            log.error("No customer profile linked to user ID: {}", optionalUser.get().getUserId());
            throw new CustomException(404, "No customer profile linked to this account — create one at POST /api/customers/me first");
        }
        com.ijse.codlink.service.BookingValidation.validate(optionalCustomer.get(), parcelDTO, true);
        return toDto(buildAndSaveParcel(optionalCustomer.get(), parcelDTO));
    }

    private Parcel buildAndSaveParcel(Customer customer, ParcelDTO parcelDTO) {
        com.ijse.codlink.service.BookingValidation.validate(customer, parcelDTO, false);
        Optional<Branch> optionalBranch = branchRepository.findById(parcelDTO.getOriginBranchId());
        if (optionalBranch.isEmpty()) {
            throw new CustomException(404, "Origin Branch not found");
        }

        Shipment shipment = null;
        if (parcelDTO.getShipmentId() != null) {
            Optional<Shipment> optionalShipment = shipmentRepository.findLockedById(parcelDTO.getShipmentId());
            if (optionalShipment.isEmpty()) {
            log.error("Shipment not found with ID: {}", parcelDTO.getShipmentId());
                throw new CustomException(404, "Shipment not found");
            }
            shipment = optionalShipment.get();
            if (shipment.getStatus() != ShipmentStatus.ACTIVE
                    || !shipment.getCustomer().getCustomerId().equals(customer.getCustomerId())
                    || invoiceRepository.existsByShipment_ShipmentId(shipment.getShipmentId())) {
                throw new CustomException(409, "Shipment must be active, uninvoiced and owned by this customer");
            }
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

        if (shipment != null) {
            validateShipmentCompatibility(shipment, deliveryAddress, isCod);
        }

        BigDecimal fee = pricingRuleService.calculateShippingFee(
                deliveryAddress.getServiceZone().getZoneId(), parcelDTO.getWeight(), isCod);

        boolean crossZonePickup = pickupAddress.getServiceZone() != null
                && !pickupAddress.getServiceZone().getZoneId().equals(deliveryAddress.getServiceZone().getZoneId());

        if (crossZonePickup) {
            fee = fee.multiply(CROSS_ZONE_PICKUP_SURCHARGE_MULTIPLIER).setScale(2, RoundingMode.HALF_UP);
        }

        Parcel parcel = new Parcel();
        parcel.setTrackingNumber("CDL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        parcel.setWeight(parcelDTO.getWeight());
        parcel.setCodAmount(parcelDTO.getCodAmount());
        parcel.setIsCod(isCod);
        parcel.setShippingFee(fee);
        parcel.setStatus(ParcelStatus.BOOKED);
        parcel.setCustomer(customer);
        parcel.setOriginBranch(optionalBranch.get());
        parcel.setCurrentBranch(optionalBranch.get());
        parcel.setShipment(shipment);
        parcel.setPickupAddress(pickupAddress);
        parcel.setDeliveryAddress(deliveryAddress);

        return parcelRepository.save(parcel);
    }

    private void validateShipmentCompatibility(Shipment shipment, Address incomingDeliveryAddress, boolean incomingIsCod) {
        List<Parcel> parcelList = parcelRepository.findByShipment_ShipmentId(shipment.getShipmentId());
        if (parcelList.isEmpty()) {
            return;
        }
        Parcel first = parcelList.get(0);

        Long firstZoneId = (first.getDeliveryAddress() != null && first.getDeliveryAddress().getServiceZone() != null)
                ? first.getDeliveryAddress().getServiceZone().getZoneId() : null;
        Long incomingZoneId = (incomingDeliveryAddress != null && incomingDeliveryAddress.getServiceZone() != null)
                ? incomingDeliveryAddress.getServiceZone().getZoneId() : null;

        if (firstZoneId == null || !firstZoneId.equals(incomingZoneId)) {
            throw new CustomException(409, "This shipment's delivery zone is #" + firstZoneId
                    + " — parcels for a different zone need a separate shipment");
        }

        Boolean firstIsCod = first.getIsCod();
        if (firstIsCod == null || !firstIsCod.equals(incomingIsCod)) {
            throw new CustomException(409, "This shipment is " + (firstIsCod == null ? "unknown" : (firstIsCod ? "COD" : "prepaid"))
                    + " — mixing prepaid and COD parcels on one shipment is not allowed");
        }
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
        log.info("Execute method getById()");
        Optional<Parcel> optionalParcel = parcelRepository.findById(parcelId);
        if (optionalParcel.isEmpty()) {
            log.error("Parcel not found with ID: {}", parcelId);
            throw new CustomException(404, "Parcel not found");
        }
        return toDto(optionalParcel.get());
    }

    @Override
    public ParcelDTO getByTrackingNumber(String trackingNumber) {
        log.info("Execute method getByTrackingNumber()");
        Optional<Parcel> optionalParcel = parcelRepository.findByTrackingNumber(trackingNumber);
        if (optionalParcel.isEmpty()) {
            log.error("No Parcel with tracking number found: {}", trackingNumber);
            throw new CustomException(404, "No Parcel with that tracking number was found");
        }
        return toDto(optionalParcel.get());
    }

    @Override
    public List<ParcelDTO> getByCustomerId(Long customerId) {
        log.info("Execute method getByCustomerId()");
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        if (optionalCustomer.isEmpty()) {
            log.error("Customer not found with ID: {}", customerId);
            throw new CustomException(404, "Customer not found");
        }
        List<ParcelDTO> parcelDTOList = new ArrayList<>();
        for (Parcel parcel : parcelRepository.findByCustomer_CustomerId(customerId)) {
            parcelDTOList.add(toDto(parcel));
        }
        return parcelDTOList;
    }

    @Override
    @Transactional
    public ParcelDTO updateStatus(Long parcelId, ParcelDTO statusDto) {
        log.info("Execute method updateStatus()");
        if (parcelId == null) {
            log.error("Parcel ID is required");
            throw new CustomException(400, "parcelId is required");
        }
        if (statusDto == null || statusDto.getStatus() == null) {
            log.error("Status is required");
            throw new CustomException(400, "Status is required");
        }

        Optional<Parcel> optionalParcel = parcelRepository.findLockedById(parcelId);
        if (optionalParcel.isEmpty()) {
            log.error("Parcel not found with ID: {}", parcelId);
            throw new CustomException(404, "Parcel not found");
        }

        Parcel parcel = optionalParcel.get();
        recordAccess.assigned(parcel);
        Parcel saved = parcelLifecycle.transition(parcel, statusDto.getStatus());

        return toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long parcelId) {
        log.info("Execute method delete()");
        Optional<Parcel> optionalParcel = parcelRepository.findById(parcelId);
        if (optionalParcel.isEmpty()) {
            log.error("Parcel not found with ID: {}", parcelId);
            throw new CustomException(404, "Parcel not found");
        }
        if (optionalParcel.get().getStatus() != ParcelStatus.BOOKED) {
            log.error("Cannot delete parcel with ID: {} because it is not in BOOKED status", parcelId);
            throw new CustomException(409, "Only parcels still in BOOKED status can be deleted");
        }
        Parcel parcel = optionalParcel.get();
        if (parcel.getShipment() != null && invoiceRepository.existsByShipment_ShipmentId(parcel.getShipment().getShipmentId())) {
            throw new CustomException(409, "Cannot delete an invoiced parcel");
        }
        parcelRepository.delete(optionalParcel.get());
    }

    @Override
    @Transactional
    public ParcelDTO assignToShipment(Long parcelId, Long shipmentId) {
        log.info("Execute method assignToShipment()");
        Optional<Parcel> optionalParcel = parcelRepository.findLockedById(parcelId);
        if (optionalParcel.isEmpty()) {
            log.error("Parcel not found with ID: {}", parcelId);
            throw new CustomException(404, "Parcel not found");
        }
        Optional<Shipment> optionalShipment = shipmentRepository.findLockedById(shipmentId);
        if (optionalShipment.isEmpty()) {
            log.error("Shipment not found with ID: {}", shipmentId);
            throw new CustomException(404, "Shipment not found");
        }

        Parcel parcel = optionalParcel.get();
        recordAccess.assigned(parcel);
        Shipment shipment = optionalShipment.get();
            if (shipment.getStatus() != ShipmentStatus.ACTIVE
                    || !shipment.getCustomer().getCustomerId().equals(parcel.getCustomer().getCustomerId())
                    || invoiceRepository.existsByShipment_ShipmentId(shipment.getShipmentId())) {
                throw new CustomException(409, "Shipment must be active, uninvoiced and owned by this customer");
            }

        if (shipment.getStatus() != ShipmentStatus.ACTIVE) {
            log.error("Cannot attach parcel with ID: {} to cancelled shipment {}", parcelId, shipmentId);
            throw new CustomException(409, "Cannot attach a parcel to a cancelled shipment");
        }
        if (invoiceRepository.existsByShipment_ShipmentId(shipmentId)) {
            log.error("Cannot assign parcel with ID: {} to shipment {} because shipment already has an invoice", parcelId, shipmentId);
            throw new CustomException(409, "Shipment already has an invoice — attaching more parcels would make it stale. No reissue endpoint exists yet.");
        }
        if (parcel.getShipment() != null) {
            log.error("Parcel with ID: {} is already attached to shipment {}", parcelId, parcel.getShipment().getShipmentId());
            throw new CustomException(409, "Parcel is already attached to shipment " + parcel.getShipment().getShipmentId());
        }
        if (parcel.getStatus() != ParcelStatus.BOOKED) {
            log.error("Cannot assign parcel with ID: {} to shipment because its status is {} (expected BOOKED)", parcelId, parcel.getStatus());
            throw new CustomException(409, "Only BOOKED parcels can be assigned to a shipment — current status is " + parcel.getStatus());
        }
        if (!parcel.getCustomer().getCustomerId().equals(shipment.getCustomer().getCustomerId())) {
            log.error("Parcel with ID: {} customer ID does not match shipment with ID: {} customer ID", parcelId, shipmentId);
            throw new CustomException(409, "Parcel's customer does not match shipment's customer");
        }

        boolean parcelIsCod = parcel.getIsCod() != null ? parcel.getIsCod() : (parcel.getCodAmount() != null && parcel.getCodAmount().compareTo(BigDecimal.ZERO) > 0);
        validateShipmentCompatibility(shipment, parcel.getDeliveryAddress(), parcelIsCod);

        parcel.setShipment(shipment);
        return toDto(parcelRepository.save(parcel));
    }

    @Override
    public List<ParcelDTO> getAll() {
        log.info("Execute method getAll()");
        List<ParcelDTO> list = new ArrayList<>();
        for (Parcel parcel : parcelRepository.findAllWithCustomerAndBranch()) {
            list.add(toDto(parcel));
        }
        return list;
    }

    @Override
    public List<ParcelDTO> getAvailable() {
        Courier courier = recordAccess.courier();
        return parcelRepository.findAvailable(courier.getBranch().getBranchId()).stream().map(this::toDto).toList();
    }

    @Override
    public List<ParcelDTO> getAssigned() {
        return parcelRepository.findByAssignedCourier_CourierId(recordAccess.courier().getCourierId())
                .stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public ParcelDTO claim(Long parcelId) {
        Courier courier = recordAccess.courier();
        Parcel parcel = parcelRepository.findLockedById(parcelId)
                .orElseThrow(() -> new CustomException(404, "Parcel not found"));
        if (parcel.getAssignedCourier() != null) {
            if (parcel.getAssignedCourier().getCourierId().equals(courier.getCourierId())) return toDto(parcel);
            throw new CustomException(409, "Parcel has already been claimed");
        }
        validateAssignment(parcel, courier);
        parcel.setAssignedCourier(courier);
        return toDto(parcelRepository.save(parcel));
    }

    @Override
    @Transactional
    public ParcelDTO reassign(Long parcelId, Long courierId) {
        if (!recordAccess.hasRole("ADMIN")) throw new CustomException(403, "Admin access required");
        Parcel parcel = parcelRepository.findLockedById(parcelId)
                .orElseThrow(() -> new CustomException(404, "Parcel not found"));
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new CustomException(404, "Courier not found"));
        validateAssignment(parcel, courier);
        parcel.setAssignedCourier(courier);
        return toDto(parcelRepository.save(parcel));
    }

    private void validateAssignment(Parcel parcel, Courier courier) {
        Branch current = parcel.getCurrentBranch() != null ? parcel.getCurrentBranch() : parcel.getOriginBranch();
        if (!Boolean.TRUE.equals(courier.getUser().getActive()) || current == null
                || !current.getBranchId().equals(courier.getBranch().getBranchId())) {
            throw new CustomException(409, "Courier must be active and belong to the current branch");
        }
        if (parcel.getStatus() == ParcelStatus.DELIVERED || parcel.getStatus() == ParcelStatus.RETURNED) {
            throw new CustomException(409, "Resolved parcels cannot be reassigned");
        }
    }

    private ParcelDTO toDto(Parcel parcel) {
        ParcelDTO dto = new ParcelDTO();
        dto.setParcelId(parcel.getParcelId());
        dto.setAssignedCourierId(parcel.getAssignedCourier() == null ? null : parcel.getAssignedCourier().getCourierId());
        dto.setTrackingNumber(parcel.getTrackingNumber());
        dto.setCustomerName(parcel.getCustomer() != null ? parcel.getCustomer().getName() : null);
        dto.setOriginBranchName(parcel.getOriginBranch() != null ? parcel.getOriginBranch().getBranchName() : null);
        dto.setWeight(parcel.getWeight());
        dto.setCodAmount(parcel.getCodAmount());
        dto.setIsCod(parcel.getIsCod());
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
            return null;
        }
        if (parcel.getDeliveryAddress() == null || parcel.getDeliveryAddress().getServiceZone() == null) {
            return null;
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