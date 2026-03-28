package edu.cit.lariosa.quickparcel.repository;

import edu.cit.lariosa.quickparcel.entity.TrackingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrackingHistoryRepository extends JpaRepository<TrackingHistory, Long> {
    List<TrackingHistory> findByDeliveryIdOrderByTimestampAsc(Long deliveryId);
}