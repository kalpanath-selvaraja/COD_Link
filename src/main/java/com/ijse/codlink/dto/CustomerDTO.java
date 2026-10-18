package com.ijse.codlink.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDTO {
    @jakarta.validation.constraints.Positive
    private Long customerId;
    @jakarta.validation.constraints.Size(max = 250)
    @jakarta.validation.constraints.NotBlank
    private String name;
    @jakarta.validation.constraints.Size(max = 250)
    private String phoneNumber;
    @jakarta.validation.constraints.Positive
    private Long userId;


}
