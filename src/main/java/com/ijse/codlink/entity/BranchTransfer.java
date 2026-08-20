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
@Table(name = "branch_transfers")
public class BranchTransfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transferId;

    private LocalDateTime dispatchedAt;

    @ManyToOne
    @JoinColumn(name = "parcel_id", nullable = false)
    @ToString.Exclude
    private Parcel parcel;

    @ManyToOne
    @JoinColumn(name = "from_branch_id", nullable = false)
    @ToString.Exclude
    private Branch fromBranch;

    @ManyToOne
    @JoinColumn(name = "to_branch_id", nullable = false)
    @ToString.Exclude
    private Branch toBranch;
}