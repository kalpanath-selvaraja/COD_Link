package com.ijse.codlink.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @ToString
@Table(name = "tracking_events")

public class TrackingEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    private String status;
    private LocalDateTime eventTime;

    @ManyToOne
    @JoinColumn(name = "parcel_id", nullable = false)
    @ToString.Exclude
    private Parcel parcel;
}
