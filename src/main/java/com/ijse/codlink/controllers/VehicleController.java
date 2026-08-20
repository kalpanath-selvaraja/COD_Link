package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.VehicleDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse create(@RequestBody VehicleDTO dto) {
        return new CommonResponse(0, vehicleService.create(dto), "Vehicle registered");
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getById(@PathVariable Long id) {
        return new CommonResponse(0, vehicleService.getById(id), "Vehicle fetched");
    }

    @GetMapping(value = "/courier/{courierId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getByCourier(@PathVariable Long courierId) {
        return new CommonResponse(0, vehicleService.getByCourierId(courierId), "Vehicles fetched");
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse update(@PathVariable Long id, @RequestBody VehicleDTO dto) {
        return new CommonResponse(0, vehicleService.update(id, dto), "Vehicle updated");
    }

    @DeleteMapping(value = "/{id}")
    public CommonResponse delete(@PathVariable Long id) {
        vehicleService.delete(id);
        return new CommonResponse(0, "Vehicle deleted");
    }

    @PatchMapping(value = "/{id}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse updateStatus(@PathVariable Long id, @RequestBody VehicleDTO dto) {
        return new CommonResponse(0, vehicleService.updateStatus(id, dto), "Vehicle status updated");
    }


}