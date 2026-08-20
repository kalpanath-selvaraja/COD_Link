package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.AddressDTO;
import com.ijse.codlink.dto.ParcelDTO;
import com.ijse.codlink.entity.*;
import com.ijse.codlink.enumeration.ParcelStatus;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.*;
import com.ijse.codlink.service.ParcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

        Parcel parcel = new Parcel();
        parcel.setTrackingNumber("CDL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        parcel.setWeight(parcelDTO.getWeight());
        parcel.setCodAmount(parcelDTO.getCodAmount());
        parcel.setStatus(ParcelStatus.BOOKED);
        parcel.setCustomer(customer);
        parcel.setOriginBranch(optionalBranch.get());
        parcel.setShipment(shipment);
        parcel.setPickupAddress(toAddress(parcelDTO.getPickupAddress()));
        parcel.setDeliveryAddress(toAddress(parcelDTO.getDeliveryAddress()));

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
        parcel.setStatus(statusDto.getStatus());
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

    private ParcelDTO toDto(Parcel parcel) {
        ParcelDTO dto = new ParcelDTO();
        dto.setParcelId(parcel.getParcelId());
        dto.setTrackingNumber(parcel.getTrackingNumber());
        dto.setWeight(parcel.getWeight());
        dto.setCodAmount(parcel.getCodAmount());
        dto.setStatus(parcel.getStatus());
        dto.setCustomerId(parcel.getCustomer().getCustomerId());
        dto.setOriginBranchId(parcel.getOriginBranch().getBranchId());
        dto.setShipmentId(parcel.getShipment() != null ? parcel.getShipment().getShipmentId() : null);
        return dto;
    }
}