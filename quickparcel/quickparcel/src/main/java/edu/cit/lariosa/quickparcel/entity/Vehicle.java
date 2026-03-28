package edu.cit.lariosa.quickparcel.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
@Data
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "rider_id", unique = true)
    private Rider rider;

    @Column(nullable = false)
    private String type; // BIKE, MOTORCYCLE, CAR, VAN

    private String model;

    @Column(name = "plate_number", unique = true)
    private String plateNumber;

    private Double capacity;

    private String color;

    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}