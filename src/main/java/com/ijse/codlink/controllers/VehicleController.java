package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.VehicleDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> create(@RequestBody VehicleDTO dto) {
        CommonResponse response = new CommonResponse(0, vehicleService.create(dto), "Vehicle registered");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getById(@PathVariable Long id) {
        CommonResponse response = new CommonResponse(0, vehicleService.getById(id), "Vehicle fetched");
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/courier/{courierId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getByCourier(@PathVariable Long courierId) {
        CommonResponse response = new CommonResponse(0, vehicleService.getByCourierId(courierId), "Vehicles fetched");
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> update(@PathVariable Long id, @RequestBody VehicleDTO dto) {
        CommonResponse response = new CommonResponse(0, vehicleService.update(id, dto), "Vehicle updated");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<CommonResponse> delete(@PathVariable Long id) {
        vehicleService.delete(id);
        CommonResponse response = new CommonResponse(0, "Vehicle deleted");
        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = "/{id}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> updateStatus(@PathVariable Long id, @RequestBody VehicleDTO dto) {
        CommonResponse response = new CommonResponse(0, vehicleService.updateStatus(id, dto), "Vehicle status updated");
        return ResponseEntity.ok(response);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getAll() {
        CommonResponse response = new CommonResponse(0, vehicleService.getAll(), "Vehicles fetched");
        return ResponseEntity.ok(response);
    }
}