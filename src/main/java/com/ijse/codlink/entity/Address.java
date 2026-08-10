package com.ijse.codlink.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class Address{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    private String street;
    private String city;
    private String postalCode;

    @ManyToOne
    @JoinColumn(name = "zone_id")
    @ToString.Exclude
    private ServiceZone serviceZone;


}
