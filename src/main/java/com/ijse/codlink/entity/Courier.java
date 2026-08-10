package com.ijse.codlink.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Courier{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courierId;
    private String licenseNumber;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @ToString.Exclude
    private User user;

    @ManyToOne
    @JoinColumn(name = "branch-id", nullable = false)
    @ToString.Exclude
    private Branch branch;


}
