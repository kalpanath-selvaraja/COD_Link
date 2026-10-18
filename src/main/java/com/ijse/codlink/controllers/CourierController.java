package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.CourierDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.CourierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/couriers")
@RequiredArgsConstructor
public class CourierController {

    private final CourierService courierService;

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        CommonResponse response = new CommonResponse(0, courierService.getMyProfile(principal.getUsername()), "Courier profile fetched");
        return ResponseEntity.ok(response);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> create(@jakarta.validation.Valid @RequestBody CourierDTO dto) {
        CommonResponse response = new CommonResponse(0, courierService.createCourierAccount(dto), "Courier account created");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getById(@PathVariable Long id) {
        CommonResponse response = new CommonResponse(0, courierService.getById(id), "Courier fetched");
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/branch/{branchId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getByBranch(@PathVariable Long branchId) {
        CommonResponse response = new CommonResponse(0, courierService.getByBranch(branchId), "Couriers fetched");
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> update(@PathVariable Long id, @jakarta.validation.Valid @RequestBody CourierDTO dto) {
        CommonResponse response = new CommonResponse(0, courierService.updateCourierAccount(id, dto), "Courier updated");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<CommonResponse> delete(@PathVariable Long id) {
        courierService.delete(id);
        CommonResponse response = new CommonResponse(0, "Courier deleted");
        return ResponseEntity.ok(response);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getAll() {
        CommonResponse response = new CommonResponse(0, courierService.getAll(), "Couriers fetched");
        return ResponseEntity.ok(response);
    }
}