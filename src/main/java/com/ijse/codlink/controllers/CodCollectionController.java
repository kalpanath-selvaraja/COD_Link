package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.CodCollectionDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.CodCollectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
    public CommonResponse create(@Valid @RequestBody CodCollectionDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        return new CommonResponse(0, codCollectionService.create(principal.getUsername(), dto), "COD collection recorded");
    }

    @GetMapping(value = "/courier/{courierId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getByCourier(@PathVariable Long courierId) {
        return new CommonResponse(0, codCollectionService.getByCourierId(courierId), "COD collections fetched");
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getAll() {
        return new CommonResponse(0, codCollectionService.getAll(), "COD collections fetched");
    }
}