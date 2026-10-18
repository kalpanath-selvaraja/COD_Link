package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.ServiceZoneDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.ServiceZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/service-zones")
@RequiredArgsConstructor
public class ServiceZoneController {

    private final ServiceZoneService serviceZoneService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> create(@jakarta.validation.Valid @RequestBody ServiceZoneDTO dto) {
        CommonResponse response = new CommonResponse(0, serviceZoneService.create(dto), "Service zone created");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getAll() {
        CommonResponse response = new CommonResponse(0, serviceZoneService.getAll(), "Service zones fetched");
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> update(@PathVariable Long id, @jakarta.validation.Valid @RequestBody ServiceZoneDTO dto) {
        CommonResponse response = new CommonResponse(0, serviceZoneService.update(id, dto), "Service zone updated");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<CommonResponse> delete(@PathVariable Long id) {
        serviceZoneService.delete(id);
        CommonResponse response = new CommonResponse(0, "Service zone deleted");
        return ResponseEntity.ok(response);
    }
}