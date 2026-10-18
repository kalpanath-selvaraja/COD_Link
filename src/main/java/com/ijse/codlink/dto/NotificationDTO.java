package com.ijse.codlink.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    @jakarta.validation.constraints.Positive
    private Long notificationId;
    @jakarta.validation.constraints.Size(max = 250)
    @jakarta.validation.constraints.NotBlank
    private String message;
    @jakarta.validation.constraints.Positive
    @jakarta.validation.constraints.NotNull
    private Long userId;

    public NotificationDTO(String message, Long userId) {
        this.message = message;
        this.userId = userId;
    }
}
