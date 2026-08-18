package com.ijse.codlink.dto;

import com.ijse.codlink.enumeration.ParcelStatus;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParcelStatusUpdateDTO {
    private ParcelStatus status;
}
