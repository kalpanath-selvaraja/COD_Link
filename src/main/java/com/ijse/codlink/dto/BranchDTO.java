package com.ijse.codlink.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BranchDTO {

    private Long branchId;
    private String branchName;
    private String phone;
    private AddressDTO address;

}
