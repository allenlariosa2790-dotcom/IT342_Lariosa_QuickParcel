package edu.cit.lariosa.quickparcel.repository;

import edu.cit.lariosa.quickparcel.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByTrackingNumber(String trackingNumber);

    @Query("SELECT d FROM Delivery d WHERE d.sender.userId = :senderId")
    List<Delivery> findBySenderId(@Param("senderId") Long senderId);

    @Query("SELECT d FROM Delivery d WHERE d.rider.userId = :riderId")
    List<Delivery> findByRiderId(@Param("riderId") Long riderId);

    List<Delivery> findByStatus(String status);
}