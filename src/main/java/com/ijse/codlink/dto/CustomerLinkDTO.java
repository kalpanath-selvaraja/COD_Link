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
    @jakarta.validation.constraints.Positive
    private Long customerId;

    @NotBlank(message = "phoneNumber is required")
    @jakarta.validation.constraints.Size(max = 250)
    private String phoneNumber;
}