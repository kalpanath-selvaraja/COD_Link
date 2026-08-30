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


    public BranchDTO(String branchName, String phone, AddressDTO address) {
        this.branchName = branchName;
        this.phone = phone;
        this.address = address;
    }
}
