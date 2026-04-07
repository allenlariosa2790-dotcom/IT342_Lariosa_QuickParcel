package edu.cit.lariosa.quickparcel.service;

import edu.cit.lariosa.quickparcel.dto.CreateDeliveryRequest;
import edu.cit.lariosa.quickparcel.entity.*;
import edu.cit.lariosa.quickparcel.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Autowired
    private DistanceService distanceService;

    @Transactional
    public Delivery markPaymentAsPaid(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));
        delivery.setPaymentStatus("PAID");
        delivery.setUpdatedAt(LocalDateTime.now());
        return deliveryRepository.save(delivery);
    }

    @Transactional
    public Delivery createDelivery(CreateDeliveryRequest request, Long senderUserId) {
        Sender sender = senderRepository.findByUserId(senderUserId)
                .orElseThrow(() -> new RuntimeException("Sender not found for user id: " + senderUserId));

        Parcel parcel = new Parcel();
        parcel.setName(request.getParcel().getName());
        parcel.setDescription(request.getParcel().getDescription());
        parcel.setWeight(request.getParcel().getWeight());
        parcel.setSize(request.getParcel().getSize());
        parcel.setCategory(request.getParcel().getCategory());
        parcel.setIsFragile(request.getParcel().getIsFragile());
        parcel.setSender(sender);
        parcel.setCreatedAt(LocalDateTime.now());
        parcel = parcelRepository.save(parcel);

        Delivery delivery = new Delivery();
        delivery.setParcel(parcel);
        delivery.setSender(sender);
        delivery.setPickupAddress(request.getPickupAddress());
        delivery.setDropoffAddress(request.getDropoffAddress());
        delivery.setNotes(request.getNotes());
        delivery.setScheduledTime(request.getScheduledTime());
        delivery.setStatus("PENDING");
        delivery.setTrackingNumber(generateTrackingNumber());
        delivery.setCreatedAt(LocalDateTime.now());
        delivery.setPaymentMethod(request.getPaymentMethod());
        delivery.setPaymentStatus(request.getPaymentStatus());

        // Calculate real distance and cost
        double distanceKm = distanceService.calculateDistanceInKm(
                request.getPickupAddress(),
                request.getDropoffAddress()
        );
        double estimatedCost = calculateEstimatedCost(distanceKm, request.getParcel().getWeight());

        delivery.setDistance(distanceKm);
        delivery.setEstimatedCost(estimatedCost);

        return deliveryRepository.save(delivery);
    }

    private double calculateEstimatedCost(double distanceKm, double weightKg) {
        double baseFare = 50.0;
        double perKmRate = 20.0;
        double weightSurcharge = Math.max(0, (weightKg - 2) * 10);
        return baseFare + (distanceKm * perKmRate) + weightSurcharge;
    }

    // Helper to generate a unique tracking number
    private String generateTrackingNumber() {
        return "QP-" + System.currentTimeMillis();
        // Alternative: return "QP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }


    public Optional<Delivery> getDeliveryById(Long id) {
        return deliveryRepository.findById(id);
    }

    public Optional<Delivery> getDeliveryByTrackingNumber(String trackingNumber) {
        return deliveryRepository.findByTrackingNumber(trackingNumber);
    }

    public List<Delivery> getDeliveriesBySenderId(Long senderId) {
        return deliveryRepository.findBySenderId(senderId);
    }

    public List<Delivery> getAvailableDeliveries() {
        return deliveryRepository.findByStatus("PENDING");
    }

    public List<Delivery> getDeliveriesByRiderId(Long riderId) {
        return deliveryRepository.findByRiderId(riderId);
    }

    @Transactional
    public Delivery updateDeliveryStatus(Long deliveryId, String status, String location) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        String oldStatus = delivery.getStatus();
        delivery.setStatus(status);
        delivery.setUpdatedAt(LocalDateTime.now());

        TrackingHistory history = new TrackingHistory();
        history.setDelivery(delivery);
        history.setStatus(status);
        history.setLocation(location);
        history.setTimestamp(LocalDateTime.now());
        trackingHistoryRepository.save(history);

        if ("PICKED_UP".equals(status)) {
            delivery.setPickedUpTime(LocalDateTime.now());
        } else if ("DELIVERED".equals(status)) {
            delivery.setDeliveredTime(LocalDateTime.now());
        }

        return deliveryRepository.save(delivery);
    }

    @Transactional
    public Delivery acceptDelivery(Long deliveryId, Long riderId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> new RuntimeException("Rider not found"));

        delivery.setRider(rider);
        delivery.setStatus("ACCEPTED");
        delivery.setUpdatedAt(LocalDateTime.now());

        TrackingHistory history = new TrackingHistory();
        history.setDelivery(delivery);
        history.setStatus("ACCEPTED");
        history.setLocation("Delivery accepted by rider");
        history.setTimestamp(LocalDateTime.now());
        trackingHistoryRepository.save(history);

        return deliveryRepository.save(delivery);
    }

    @Transactional
    public Delivery cancelDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        delivery.setStatus("CANCELLED");
        delivery.setUpdatedAt(LocalDateTime.now());

        TrackingHistory history = new TrackingHistory();
        history.setDelivery(delivery);
        history.setStatus("CANCELLED");
        history.setLocation("Delivery cancelled");
        history.setTimestamp(LocalDateTime.now());
        trackingHistoryRepository.save(history);

        return deliveryRepository.save(delivery);
    }

    public List<TrackingHistory> getTrackingHistory(Long deliveryId) {
        return trackingHistoryRepository.findByDeliveryIdOrderByTimestampAsc(deliveryId);
    }
}