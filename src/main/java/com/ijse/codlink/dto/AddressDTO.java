package com.ijse.codlink.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {

    @jakarta.validation.constraints.Positive
    private Long addressId;
    @jakarta.validation.constraints.Size(max = 250)
    @jakarta.validation.constraints.NotBlank
    private String street;
    @jakarta.validation.constraints.Size(max = 250)
    @jakarta.validation.constraints.NotBlank
    private String city;
    @jakarta.validation.constraints.Size(max = 250)
    @jakarta.validation.constraints.NotBlank
    private String postalCode;
    @jakarta.validation.constraints.Positive
    private Long zoneId;

}
