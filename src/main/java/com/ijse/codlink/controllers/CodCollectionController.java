package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.CodCollectionDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.CodCollectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/cod-collections")
@RequiredArgsConstructor
public class CodCollectionController {

    private final CodCollectionService codCollectionService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> create(@Valid @RequestBody CodCollectionDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        CommonResponse response = new CommonResponse(0, codCollectionService.create(principal.getUsername(), dto), "COD collection recorded");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping(value = "/courier/{courierId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getByCourier(@PathVariable Long courierId) {
        CommonResponse response = new CommonResponse(0, codCollectionService.getByCourierId(courierId), "COD collections fetched");
        return ResponseEntity.ok(response);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getAll() {
        CommonResponse response = new CommonResponse(0, codCollectionService.getAll(), "COD collections fetched");
        return ResponseEntity.ok(response);
    }
}