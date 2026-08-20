package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.BranchDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse create(@RequestBody BranchDTO dto) {
        return new CommonResponse(0, branchService.createBranch(dto), "Branch created");
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getById(@PathVariable Long id) {
        return new CommonResponse(0, branchService.getById(id), "Branch fetched");
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getAll() {
        return new CommonResponse(0, branchService.getAll(), "Branches fetched");
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse update(@PathVariable Long id, @RequestBody BranchDTO dto) {
        return new CommonResponse(0, branchService.updateBranch(id, dto), "Branch updated");
    }

    @DeleteMapping(value = "/{id}")
    public CommonResponse delete(@PathVariable Long id) {
        branchService.deleteBranch(id);
        return new CommonResponse(0, "Branch deleted");
    }
}