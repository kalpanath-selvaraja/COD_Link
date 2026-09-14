package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.RegisterRequestDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Admin access confirmed");
    }

    @PostMapping(value = "/create-admin", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> createAdmin(@Valid @RequestBody RegisterRequestDTO request) {
        CommonResponse response = new CommonResponse(0, userService.createAdminAccount(request), "Admin account created");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}