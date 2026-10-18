package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.BranchDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> create(@jakarta.validation.Valid @RequestBody BranchDTO dto) {
        CommonResponse response = new CommonResponse(0, branchService.createBranch(dto), "Branch created");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getById(@PathVariable Long id) {
        CommonResponse response = new CommonResponse(0, branchService.getById(id), "Branch fetched");
        return ResponseEntity.ok(response);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getAll() {
        CommonResponse response = new CommonResponse(0, branchService.getAll(), "Branches fetched");
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> update(@PathVariable Long id, @jakarta.validation.Valid @RequestBody BranchDTO dto) {
        CommonResponse response = new CommonResponse(0, branchService.updateBranch(id, dto), "Branch updated");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<CommonResponse> delete(@PathVariable Long id) {
        branchService.deleteBranch(id);
        CommonResponse response = new CommonResponse(0, "Branch deleted");
        return ResponseEntity.ok(response);
    }
}