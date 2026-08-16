package com.ijse.codlink.service.impl;

import com.ijse.codlink.dto.ShipmentDTO;
import com.ijse.codlink.entity.Customer;
import com.ijse.codlink.entity.Shipment;
import com.ijse.codlink.exception.CustomException;
import com.ijse.codlink.repository.CustomerRepository;
import com.ijse.codlink.repository.ShipmentRepository;
import com.ijse.codlink.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    @Override
    public ShipmentDTO create(ShipmentDTO shipmentDTO) {
        System.out.println(shipmentDTO.getCustomerId());
        Optional<Customer> optionalCustomer = customerRepository.findById(shipmentDTO.getCustomerId());

        if(optionalCustomer.isEmpty()){
            throw new CustomException(404, "Customer not found");
        }

        Shipment shipment = new Shipment();
        shipment.setCreatedAt(LocalDateTime.now());
        shipment.setCustomer(optionalCustomer.get());

        Shipment save = shipmentRepository.save(shipment);

        return new ShipmentDTO(save.getShipmentId(),save.getCustomer().getCustomerId(),save.getCreatedAt());

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
            list.add(
                    new ShipmentDTO(shipment.getShipmentId(),
                            shipment.getCustomer().getCustomerId(),
                            shipment.getCreatedAt()));
        }

        return list;
    }
}
