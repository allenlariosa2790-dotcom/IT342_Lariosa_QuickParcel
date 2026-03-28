package edu.cit.lariosa.quickparcel.repository;

import edu.cit.lariosa.quickparcel.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByDeliveryId(Long deliveryId);
    List<Payment> findBySenderId(Long senderId);
}