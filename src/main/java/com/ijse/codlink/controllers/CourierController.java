package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.CourierDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.CourierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/couriers")
@RequiredArgsConstructor
public class CourierController {

    private final CourierService courierService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse create(@RequestBody CourierDTO dto) {
        return new CommonResponse(0, courierService.createCourierAccount(dto), "Courier account created");
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getById(@PathVariable Long id) {
        return new CommonResponse(0, courierService.getById(id), "Courier fetched");
    }

    @GetMapping(value = "/branch/{branchId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getByBranch(@PathVariable Long branchId) {
        return new CommonResponse(0, courierService.getByBranch(branchId), "Couriers fetched");
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse update(@PathVariable Long id, @RequestBody CourierDTO dto) {
        return new CommonResponse(0, courierService.updateCourierAccount(id, dto), "Courier updated");
    }

    @DeleteMapping(value = "/{id}")
    public CommonResponse delete(@PathVariable Long id) {
        courierService.delete(id);
        return new CommonResponse(0, "Courier deleted");
    }
}
