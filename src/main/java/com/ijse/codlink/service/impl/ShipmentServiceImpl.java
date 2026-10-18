package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.InvoiceDTO;
import com.ijse.codlink.dto.ShipmentDTO;
import com.ijse.codlink.entity.Customer;
import com.ijse.codlink.entity.Invoice;
import com.ijse.codlink.entity.Parcel;
import com.ijse.codlink.entity.Shipment;
import com.ijse.codlink.enumeration.ParcelStatus;
import com.ijse.codlink.enumeration.ShipmentStatus;
import com.ijse.codlink.exception.CustomException;import com.ijse.codlink.repository.CodCollectionRepository;
import com.ijse.codlink.repository.CustomerRepository;
import com.ijse.codlink.repository.InvoiceRepository;
import com.ijse.codlink.repository.ParcelRepository;
import com.ijse.codlink.repository.PaymentRepository;
import com.ijse.codlink.repository.ShipmentRepository;
import com.ijse.codlink.service.InvoiceService;
import com.ijse.codlink.service.PaymentService;
import com.ijse.codlink.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final CustomerRepository customerRepository;
    private final ParcelRepository parcelRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;
    private final CodCollectionRepository codCollectionRepository;

    @Override
    public ShipmentDTO create(ShipmentDTO shipmentDTO) {

        if (shipmentDTO.getCustomerId() == null) {
            throw new CustomException(400, "Customer ID is required");
        }

        Optional<Customer> optionalCustomer = customerRepository.findById(shipmentDTO.getCustomerId());

        if(optionalCustomer.isEmpty()){
            throw new CustomException(404, "Customer not found");
        }

        Shipment shipment = new Shipment();
        shipment.setCreatedAt(LocalDateTime.now());
        shipment.setCustomer(optionalCustomer.get());

        Shipment save = shipmentRepository.save(shipment);

        return new ShipmentDTO(save.getShipmentId(), save.getCustomer().getCustomerId(), save.getCreatedAt(), save.getStatus());
    }

    @Override
    public List<ShipmentDTO> getByCustomerId(Long customerId) {

        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);

        if(optionalCustomer.isEmpty()){
            throw new CustomException(404, "Customer not found");
        }

        List<Shipment> shipmentList = shipmentRepository.findByCustomer_CustomerId(optionalCustomer.get().getCustomerId());

        List<ShipmentDTO> list = new ArrayList<>();

        for(Shipment shipment : shipmentList){
            list.add(new ShipmentDTO(shipment.getShipmentId(), shipment.getCustomer().getCustomerId(),
                    shipment.getCreatedAt(), shipment.getStatus()));
        }

        return list;
    }

    @Override
    public ShipmentDTO getById(Long shipmentId) {
        Optional<Shipment> optionalShipment = shipmentRepository.findById(shipmentId);
        if (optionalShipment.isEmpty()) {
            throw new CustomException(404, "Shipment not found");
        }
        Shipment s = optionalShipment.get();
        return new ShipmentDTO(s.getShipmentId(), s.getCustomer().getCustomerId(), s.getCreatedAt(), s.getStatus());
    }

    @Override
    @Transactional
    public void delete(Long shipmentId) {
        Optional<Shipment> optionalShipment = shipmentRepository.findLockedById(shipmentId);
        if (optionalShipment.isEmpty()) {
            throw new CustomException(404, "Shipment not found");
        }
        if (parcelRepository.existsByShipment_ShipmentId(shipmentId)) {
            throw new CustomException(409, "Cannot delete a shipment that still has parcels attached");
        }
        if (invoiceRepository.existsByShipment_ShipmentId(shipmentId)) {
            throw new CustomException(409, "Cannot delete a shipment that has an invoice on record");
        }
        shipmentRepository.delete(optionalShipment.get());
    }

    @Override
    @Transactional
    public ShipmentDTO cancel(Long shipmentId) {
        Optional<Shipment> optionalShipment = shipmentRepository.findLockedById(shipmentId);
        if (optionalShipment.isEmpty()) {
            throw new CustomException(404, "Shipment not found");
        }
        Shipment shipment = optionalShipment.get();

        if (shipment.getStatus() == ShipmentStatus.CANCELLED) {
            throw new CustomException(409, "Shipment already cancelled");
        }
        if (shipment.getStatus() == ShipmentStatus.COMPLETED) {
            throw new CustomException(409, "Cannot cancel a shipment that has already been completed");
        }
        if (invoiceRepository.existsByShipment_ShipmentId(shipmentId)) {
            throw new CustomException(409, "Cannot cancel a shipment that has already been invoiced");
        }

        List<Parcel> attachedParcels = parcelRepository.findByShipment_ShipmentId(shipmentId);
        for (Parcel parcel : attachedParcels) {
            parcel.setShipment(null);
            parcelRepository.save(parcel);
        }

        shipment.setStatus(ShipmentStatus.CANCELLED);
        Shipment saved = shipmentRepository.save(shipment);
        return new ShipmentDTO(saved.getShipmentId(), saved.getCustomer().getCustomerId(), saved.getCreatedAt(), saved.getStatus());
    }

    @Override
    public List<ShipmentDTO> getAll() {
        List<ShipmentDTO> list = new ArrayList<>();
        for (Shipment shipment : shipmentRepository.findAll()) {
            list.add(new ShipmentDTO(shipment.getShipmentId(), shipment.getCustomer().getCustomerId(),
                    shipment.getCreatedAt(), shipment.getStatus()));
        }
        return list;
    }

    @Override
    @Transactional
    public ShipmentDTO finalizeShipment(Long shipmentId) {
        Optional<Shipment> optionalShipment = shipmentRepository.findLockedById(shipmentId);
        if (optionalShipment.isEmpty()) {
            throw new CustomException(404, "Shipment not found");
        }
        Shipment shipment = optionalShipment.get();

        if (shipment.getStatus() == ShipmentStatus.CANCELLED) {
            throw new CustomException(409, "Cannot finalize a cancelled shipment");
        }
        if (invoiceRepository.existsByShipment_ShipmentId(shipmentId)) {
            throw new CustomException(409, "Shipment already finalized — invoice already exists");
        }

        List<Parcel> parcels = parcelRepository.findByShipment_ShipmentId(shipmentId);
        if (parcels.isEmpty()) {
            throw new CustomException(400, "Cannot finalize a shipment with no parcels attached");
        }

        Boolean isCod = parcels.get(0).getIsCod();
        for (Parcel p : parcels) {
            if (isCod == null || p.getIsCod() == null || !isCod.equals(p.getIsCod())) {
                throw new CustomException(409, "Shipment has a mix of prepaid and COD parcels — this should not be possible, check assignToShipment validation");
            }
        }

        InvoiceDTO invoiceDto = new InvoiceDTO();
        invoiceDto.setShipmentId(shipmentId);
        InvoiceDTO createdInvoice = invoiceService.create(invoiceDto);


        resolveShipmentIfComplete(shipmentId);

        return getById(shipmentId);
    }

    @Override
    @Transactional
    public void resolveShipmentIfComplete(Long shipmentId) {
        Optional<Shipment> optionalShipment = shipmentRepository.findLockedById(shipmentId);
        if (optionalShipment.isEmpty()) {
            return;
        }
        Shipment shipment = optionalShipment.get();
        if (shipment.getStatus() == ShipmentStatus.COMPLETED || shipment.getStatus() == ShipmentStatus.CANCELLED) {
            return;
        }

        List<Parcel> parcels = parcelRepository.findByShipment_ShipmentId(shipmentId);
        if (parcels.isEmpty()) {
            return;
        }

        Optional<Invoice> optionalInvoice = invoiceRepository.findLockedByShipmentId(shipmentId);
        if (optionalInvoice.isEmpty()) return;
        Invoice invoice = optionalInvoice.get();
        if (paymentRepository.existsByInvoice_InvoiceIdAndRequestReferenceIsNull(invoice.getInvoiceId())) {
            throw new CustomException(409, "Historical payments need review before changing this shipment");
        }
        invoice.setAmount(invoiceService.calculateAmount(parcels));
        invoiceRepository.save(invoice);

        boolean allFinished = parcels.stream().allMatch(p ->
                p.getStatus() == ParcelStatus.DELIVERED || p.getStatus() == ParcelStatus.RETURNED);
        boolean collected = parcels.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsCod()) && p.getStatus() == ParcelStatus.DELIVERED)
                .allMatch(p -> codCollectionRepository.existsByParcel_ParcelId(p.getParcelId()));
        BigDecimal balance = invoice.getAmount().subtract(paymentRepository.sumForInvoice(invoice.getInvoiceId()));
        if (allFinished && collected && balance.signum() == 0) {
            shipment.setStatus(ShipmentStatus.COMPLETED);
            shipmentRepository.save(shipment);
        }

    }
}