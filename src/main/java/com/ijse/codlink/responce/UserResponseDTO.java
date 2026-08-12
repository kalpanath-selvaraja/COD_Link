package com.ijse.codlink.responce;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private long userId;
    private String token;
}
