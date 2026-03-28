package edu.cit.lariosa.quickparcel.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "senders")
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

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getDefaultPickupAddress() { return defaultPickupAddress; }
    public void setDefaultPickupAddress(String defaultPickupAddress) { this.defaultPickupAddress = defaultPickupAddress; }

    public String getDefaultDropoffAddress() { return defaultDropoffAddress; }
    public void setDefaultDropoffAddress(String defaultDropoffAddress) { this.defaultDropoffAddress = defaultDropoffAddress; }

    public String getPreferredPaymentMethod() { return preferredPaymentMethod; }
    public void setPreferredPaymentMethod(String preferredPaymentMethod) { this.preferredPaymentMethod = preferredPaymentMethod; }

    public Integer getTotalDeliveriesRequested() { return totalDeliveriesRequested; }
    public void setTotalDeliveriesRequested(Integer totalDeliveriesRequested) { this.totalDeliveriesRequested = totalDeliveriesRequested; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}