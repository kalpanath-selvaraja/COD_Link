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

    @jakarta.validation.constraints.Positive
    private Long branchId;
    @jakarta.validation.constraints.Size(max = 250)
    @jakarta.validation.constraints.NotBlank
    private String branchName;
    @jakarta.validation.constraints.Size(max = 250)
    private String phone;
    @jakarta.validation.Valid
    private AddressDTO address;


    public BranchDTO(String branchName, String phone, AddressDTO address) {
        this.branchName = branchName;
        this.phone = phone;
        this.address = address;
    }
}
