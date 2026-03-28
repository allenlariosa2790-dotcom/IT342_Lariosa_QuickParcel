package edu.cit.lariosa.quickparcel.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "delivery_id", unique = true, nullable = false)
    private Delivery delivery;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private Sender sender;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(nullable = false)
    private Double amount;

    private String currency = "PHP";

    @Column(nullable = false)
    private String status; // PENDING, COMPLETED, FAILED, REFUNDED

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payer_email")
    private String payerEmail;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}