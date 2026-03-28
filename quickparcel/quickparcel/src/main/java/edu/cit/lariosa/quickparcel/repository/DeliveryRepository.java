package edu.cit.lariosa.quickparcel.repository;

import edu.cit.lariosa.quickparcel.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByTrackingNumber(String trackingNumber);
    List<Delivery> findBySenderId(Long senderId);
    List<Delivery> findByRiderId(Long riderId);
    List<Delivery> findByStatus(String status);
}