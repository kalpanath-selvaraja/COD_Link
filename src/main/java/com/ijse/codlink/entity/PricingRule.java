package com.ijse.codlink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "pricing_rules")
public class PricingRule{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ruleId;

    private BigDecimal rate;
    private String weightBracket;
    private Boolean isCod;

    @ManyToOne
    @JoinColumn(name = "zone_id",nullable = false)
    @ToString.Exclude
    private ServiceZone  serviceZone;

}
