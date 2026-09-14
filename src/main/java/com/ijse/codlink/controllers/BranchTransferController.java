package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.BranchTransferDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.BranchTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/branch-transfers")
@RequiredArgsConstructor
public class BranchTransferController {

    private final BranchTransferService branchTransferService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> create(@Valid @RequestBody BranchTransferDTO dto) {
        CommonResponse response = new CommonResponse(0, branchTransferService.create(dto), "Branch transfer recorded");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping(value = "/parcel/{parcelId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getByParcel(@PathVariable Long parcelId) {
        CommonResponse response = new CommonResponse(0, branchTransferService.getByParcelId(parcelId), "Branch transfers fetched");
        return ResponseEntity.ok(response);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getAll() {
        CommonResponse response = new CommonResponse(0, branchTransferService.getAll(), "Branch Transfers fetched");
        return ResponseEntity.ok(response);
    }
}