package edu.cit.lariosa.quickparcel.repository;

import edu.cit.lariosa.quickparcel.entity.TrackingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TrackingHistoryRepository extends JpaRepository<TrackingHistory, Long> {
    @Query("SELECT t FROM TrackingHistory t WHERE t.delivery.id = :deliveryId ORDER BY t.timestamp ASC")
    List<TrackingHistory> findByDeliveryIdOrderByTimestampAsc(@Param("deliveryId") Long deliveryId);
}