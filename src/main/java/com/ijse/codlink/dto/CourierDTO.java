package com.ijse.codlink.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CourierDTO {
    @jakarta.validation.constraints.Positive
    private Long courierId;
    @jakarta.validation.constraints.Size(max = 250)
    private String licenseNumber;
    @jakarta.validation.constraints.Positive
    private Long userId;
    @jakarta.validation.constraints.Positive
    private Long branchId;
    @jakarta.validation.constraints.Size(max = 250)
    private String userName;
    @jakarta.validation.constraints.Size(max = 72)
    private String password;
}
