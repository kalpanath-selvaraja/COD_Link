package com.ijse.codlink.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthDTO {
    @NotBlank(message = "Username is required")
    @jakarta.validation.constraints.Size(max = 250)
    private String username;
    @NotBlank(message = "Password is required")
    @jakarta.validation.constraints.Size(max = 72)
    private String password;
}
