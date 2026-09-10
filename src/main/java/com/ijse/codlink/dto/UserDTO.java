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

        private Long userId;
        private String username;
        private UserRole userRole;
        private Boolean active;
        private Boolean isReceptionist;

}
