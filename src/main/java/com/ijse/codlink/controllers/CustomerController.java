package com.ijse.codlink.controllers;

import com.ijse.codlink.dto.CustomerDTO;
import com.ijse.codlink.dto.CustomerLinkDTO;
import com.ijse.codlink.responce.CommonResponse;
import com.ijse.codlink.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
    public CommonResponse createMyProfile(@RequestBody CustomerDTO dto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();

        CustomerDTO created = customerService.createMyProfile(principal.getUsername(), dto);


        return new CommonResponse(0, created, "Customer profile created");
    }


    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getMyProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();

        CustomerDTO dto = customerService.getMyProfile(principal.getUsername());
        return new CommonResponse(0, dto, "Customer profile fetched");
    }

    @PostMapping(value = "/walk-in", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse createWalkIn(@RequestBody CustomerDTO dto) {
        CustomerDTO created = customerService.createWalkIn(dto);
        return new CommonResponse(0, created, "Walk-in customer created");
    }

    @PutMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse updateMyProfile(@RequestBody CustomerDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        return new CommonResponse(0, customerService.updateMyProfile(principal.getUsername(), dto), "Profile updated");
    }


    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getById(@PathVariable Long id) {
        return new CommonResponse(0, customerService.getById(id), "Customer fetched");
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse getAll() {
        return new CommonResponse(0, customerService.getAll(), "Customers fetched");
    }

    @PostMapping(value = "/link", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse linkToMyAccount(@RequestBody CustomerLinkDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        CustomerDTO linked = customerService.linkToMyAccount(principal.getUsername(), dto);
        return new CommonResponse(0, linked, "Customer profile linked to your account");
    }

    @DeleteMapping(value = "/{id}")
    public CommonResponse delete(@PathVariable Long id) {
        customerService.delete(id);
        return new CommonResponse(0, "Customer deleted");
    }
}
