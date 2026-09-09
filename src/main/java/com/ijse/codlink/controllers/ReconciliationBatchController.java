package com.ijse.codlink.controllers;

import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.ReconciliationBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/reconciliation-batches")
@RequiredArgsConstructor
public class ReconciliationBatchController {

    private final ReconciliationBatchService reconciliationBatchService;

    @PostMapping(value = "/branch/{branchId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse createForBranch(@PathVariable Long branchId) {
        return new CommonResponse(0, reconciliationBatchService.createForBranch(branchId), "Reconciliation batch created");
    }

    @GetMapping(value = "/branch/{branchId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getByBranch(@PathVariable Long branchId) {
        return new CommonResponse(0, reconciliationBatchService.getByBranchId(branchId), "Reconciliation batches fetched");
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public  CommonResponse getAll(){
        return new CommonResponse(0,reconciliationBatchService.getAll(),"Reconciliation batches are fetched.  fetched");
    }
}

