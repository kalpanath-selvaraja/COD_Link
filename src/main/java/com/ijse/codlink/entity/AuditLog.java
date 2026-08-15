package com.ijse.codlink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    private String action;
    private LocalDateTime logTime;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    @ToString.Exclude
    private User user;
}
