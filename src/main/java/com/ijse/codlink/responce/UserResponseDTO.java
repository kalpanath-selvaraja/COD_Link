package com.ijse.codlink.responce;

import com.ijse.codlink.enumeration.UserRole;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private long userId;
    private String token;
    private UserRole role;
}
