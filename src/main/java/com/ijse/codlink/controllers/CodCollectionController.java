package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.CodCollectionDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.CodCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/cod-collections")
@RequiredArgsConstructor
public class CodCollectionController {

    private final CodCollectionService codCollectionService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse create(@RequestBody CodCollectionDTO dto) {
        return new CommonResponse(0, codCollectionService.create(dto), "COD collection recorded");
    }

    @GetMapping(value = "/courier/{courierId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getByCourier(@PathVariable Long courierId) {
        return new CommonResponse(0, codCollectionService.getByCourierId(courierId), "COD collections fetched");
    }
}