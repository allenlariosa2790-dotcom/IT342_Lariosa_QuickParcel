package edu.cit.lariosa.quickparcel.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "senders")
@Data
public class Sender {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "default_pickup_address")
    private String defaultPickupAddress;

    @Column(name = "default_dropoff_address")
    private String defaultDropoffAddress;

    @Column(name = "preferred_payment_method")
    private String preferredPaymentMethod;

    @Column(name = "total_deliveries_requested")
    private Integer totalDeliveriesRequested = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}