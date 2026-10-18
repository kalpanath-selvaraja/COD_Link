package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.CustomerDTO;
import com.ijse.codlink.dto.CustomerLinkDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "api/customers")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> createMyProfile(@jakarta.validation.Valid @RequestBody CustomerDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        CustomerDTO created = customerService.createMyProfile(principal.getUsername(), dto);
        return new ResponseEntity<>(new CommonResponse(0, created, "Customer profile created"), HttpStatus.CREATED);
    }

    @PostMapping(value = "/walk-in", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> createWalkIn(@jakarta.validation.Valid @RequestBody CustomerDTO dto) {
        CustomerDTO created = customerService.createWalkIn(dto);
        return new ResponseEntity<>(new CommonResponse(0, created, "Walk-in customer created"), HttpStatus.CREATED);
    }

    @PostMapping(value = "/link", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> linkToMyAccount(@Valid @RequestBody CustomerLinkDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        CustomerDTO linked = customerService.linkToMyAccount(principal.getUsername(), dto);
        return new ResponseEntity<>(new CommonResponse(0, linked, "Customer profile linked to your account"), HttpStatus.CREATED);
    }

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(new CommonResponse(0, customerService.getMyProfile(principal.getUsername()), "Customer profile fetched"));
    }

    @PutMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> updateMyProfile(@jakarta.validation.Valid @RequestBody CustomerDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(new CommonResponse(0, customerService.updateMyProfile(principal.getUsername(), dto), "Profile updated"));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new CommonResponse(0, customerService.getById(id), "Customer fetched"));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> getAll() {
        return ResponseEntity.ok(new CommonResponse(0, customerService.getAll(), "Customers fetched"));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<CommonResponse> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.ok(new CommonResponse(0, "Customer deleted"));
    }
}