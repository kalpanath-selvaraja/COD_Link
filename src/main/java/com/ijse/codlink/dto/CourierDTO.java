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
    private Long courierId;
    private String licenseNumber;
    private Long userId;
    private Long branchId;
    private String userName;
    private String password;
}
