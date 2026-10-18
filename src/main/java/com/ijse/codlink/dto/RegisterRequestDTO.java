package com.ijse.codlink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDTO {
    @NotBlank(message = "Username is required")
    @jakarta.validation.constraints.Size(max = 250)
    private String username;
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @jakarta.validation.constraints.Size(max = 72)
    private String password;


    private Boolean isReceptionist;
}
