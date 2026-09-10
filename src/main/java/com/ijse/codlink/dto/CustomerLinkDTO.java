package com.ijse.codlink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerLinkDTO {
    @NotNull(message = "customerId is required")
    private Long customerId;

    @NotBlank(message = "phoneNumber is required")
    private String phoneNumber;
}