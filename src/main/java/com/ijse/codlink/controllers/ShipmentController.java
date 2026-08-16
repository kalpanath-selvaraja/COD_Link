package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.ShipmentDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "api/shipments")
@RequiredArgsConstructor
public class ShipmentController {
    private final ShipmentService shipmentService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse createShipment(@RequestBody ShipmentDTO shipmentDTO) {
        ShipmentDTO dto = shipmentService.create(shipmentDTO);

        return new CommonResponse(0, dto,"Shipment created successfully");
    }

    @GetMapping(value = "/customer/{customerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getShipmentByCustomer(@PathVariable Long customerId) {
        List<ShipmentDTO> shipmentDTOList = shipmentService.getByCustomerId(customerId);

        return new CommonResponse(0, shipmentDTOList,"Shipment by customerId successfully");
    }
}
