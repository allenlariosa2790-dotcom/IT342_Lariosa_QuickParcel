package edu.cit.lariosa.quickparcel.repository;

import edu.cit.lariosa.quickparcel.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByDeliveryId(Long deliveryId);

    @Query("SELECT p FROM Payment p WHERE p.sender.userId = :senderId")
    List<Payment> findBySenderId(@Param("senderId") Long senderId);
}