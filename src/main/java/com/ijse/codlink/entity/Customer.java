package com.ijse.codlink.entity;

import com.ijse.codlink.enumeration.UserStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    private String name;
    private String phoneNumber;
    private UserStatus status;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id", unique = true)
    @ToString.Exclude
    private User user;


}
