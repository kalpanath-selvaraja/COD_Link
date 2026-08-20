package com.ijse.codlink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "delivery_attempts")
public class DeliveryAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attemptId;

    private String outcome;
    private LocalDateTime attemptedAt;

    @ManyToOne
    @JoinColumn(name = "parcel_id", nullable = false)
    @ToString.Exclude
    private Parcel parcel;

    @ManyToOne
    @JoinColumn(name = "courier_id", nullable = false)
    @ToString.Exclude
    private Courier courier;
}