package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.BranchTransferDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.BranchTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/branch-transfers")
@RequiredArgsConstructor
public class BranchTransferController {

    private final BranchTransferService branchTransferService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse create(@RequestBody BranchTransferDTO dto) {
        return new CommonResponse(0, branchTransferService.create(dto), "Branch transfer recorded");
    }

    @GetMapping(value = "/parcel/{parcelId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getByParcel(@PathVariable Long parcelId) {
        return new CommonResponse(0, branchTransferService.getByParcelId(parcelId), "Branch transfers fetched");
    }
}