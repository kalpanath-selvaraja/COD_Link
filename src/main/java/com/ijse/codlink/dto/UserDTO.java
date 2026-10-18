package com.ijse.codlink.dto;

import com.ijse.codlink.enumeration.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

        @jakarta.validation.constraints.Positive
    private Long userId;
        @jakarta.validation.constraints.Size(max = 250)
    private String username;
        private UserRole userRole;
        private Boolean active;
        private Boolean isReceptionist;

}
