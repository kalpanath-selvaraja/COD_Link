package com.ijse.codlink.controllers;

import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.ReconciliationBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/reconciliation-batches")
@RequiredArgsConstructor
public class ReconciliationBatchController {

    private final ReconciliationBatchService reconciliationBatchService;

    @PostMapping(value = "/branch/{branchId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> createForBranch(@PathVariable Long branchId) {
        CommonResponse response = new CommonResponse(0, reconciliationBatchService.createForBranch(branchId), "Reconciliation batch created");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping(value = "/branch/{branchId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getByBranch(@PathVariable Long branchId) {
        CommonResponse response = new CommonResponse(0, reconciliationBatchService.getByBranchId(branchId), "Reconciliation batches fetched");
        return ResponseEntity.ok(response);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getAll() {
        CommonResponse response = new CommonResponse(0, reconciliationBatchService.getAll(), "Reconciliation batches are fetched.");
        return ResponseEntity.ok(response);
    }
}