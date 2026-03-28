package edu.cit.lariosa.quickparcel.service;

import edu.cit.lariosa.quickparcel.entity.*;
import edu.cit.lariosa.quickparcel.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DeliveryService {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private ParcelRepository parcelRepository;

    @Autowired
    private SenderRepository senderRepository;

    @Autowired
    private RiderRepository riderRepository;

    @Autowired
    private TrackingHistoryRepository trackingHistoryRepository;

    // Create a new delivery request
    @Transactional
    public Delivery createDelivery(Delivery delivery) {
        // Generate tracking number
        delivery.setTrackingNumber("QP-" + System.currentTimeMillis());
        delivery.setStatus("PENDING");
        delivery.setCreatedAt(LocalDateTime.now());
        return deliveryRepository.save(delivery);
    }

    // Get delivery by ID
    public Optional<Delivery> getDeliveryById(Long id) {
        return deliveryRepository.findById(id);
    }

    // Get delivery by tracking number
    public Optional<Delivery> getDeliveryByTrackingNumber(String trackingNumber) {
        return deliveryRepository.findByTrackingNumber(trackingNumber);
    }

    // Get all deliveries for a sender
    public List<Delivery> getDeliveriesBySenderId(Long senderId) {
        return deliveryRepository.findBySenderId(senderId);
    }

    // Get available deliveries for riders (PENDING status)
    public List<Delivery> getAvailableDeliveries() {
        return deliveryRepository.findByStatus("PENDING");
    }

    // Get deliveries assigned to a rider
    public List<Delivery> getDeliveriesByRiderId(Long riderId) {
        return deliveryRepository.findByRiderId(riderId);
    }

    // Update delivery status
    @Transactional
    public Delivery updateDeliveryStatus(Long deliveryId, String status, String location) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        String oldStatus = delivery.getStatus();
        delivery.setStatus(status);
        delivery.setUpdatedAt(LocalDateTime.now());

        // Record tracking history
        TrackingHistory history = new TrackingHistory();
        history.setDelivery(delivery);
        history.setStatus(status);
        history.setLocation(location);
        history.setTimestamp(LocalDateTime.now());
        trackingHistoryRepository.save(history);

        // Update timestamps based on status
        if ("PICKED_UP".equals(status)) {
            delivery.setPickedUpTime(LocalDateTime.now());
        } else if ("DELIVERED".equals(status)) {
            delivery.setDeliveredTime(LocalDateTime.now());
        }

        return deliveryRepository.save(delivery);
    }

    // Accept delivery by rider
    @Transactional
    public Delivery acceptDelivery(Long deliveryId, Long riderId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new RuntimeException("Rider not found"));

        delivery.setRider(rider);
        delivery.setStatus("ACCEPTED");
        delivery.setUpdatedAt(LocalDateTime.now());

        // Record tracking history
        TrackingHistory history = new TrackingHistory();
        history.setDelivery(delivery);
        history.setStatus("ACCEPTED");
        history.setLocation("Delivery accepted by rider");
        history.setTimestamp(LocalDateTime.now());
        trackingHistoryRepository.save(history);

        return deliveryRepository.save(delivery);
    }

    // Cancel delivery
    @Transactional
    public Delivery cancelDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        delivery.setStatus("CANCELLED");
        delivery.setUpdatedAt(LocalDateTime.now());

        // Record tracking history
        TrackingHistory history = new TrackingHistory();
        history.setDelivery(delivery);
        history.setStatus("CANCELLED");
        history.setLocation("Delivery cancelled");
        history.setTimestamp(LocalDateTime.now());
        trackingHistoryRepository.save(history);

        return deliveryRepository.save(delivery);
    }

    // Get tracking history for a delivery
    public List<TrackingHistory> getTrackingHistory(Long deliveryId) {
        return trackingHistoryRepository.findByDeliveryIdOrderByTimestampAsc(deliveryId);
    }
}