package edu.cit.lariosa.quickparcel.features.delivery;

import edu.cit.lariosa.quickparcel.features.delivery.dto.CreateDeliveryRequest;
import edu.cit.lariosa.quickparcel.features.shared.entity.*;
import edu.cit.lariosa.quickparcel.features.delivery.repository.*;
import edu.cit.lariosa.quickparcel.features.email.EmailService;
import edu.cit.lariosa.quickparcel.features.shared.repository.RiderRepository;
import edu.cit.lariosa.quickparcel.features.shared.repository.SenderRepository;
import edu.cit.lariosa.quickparcel.features.tracking.repository.TrackingHistoryRepository;
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

    @Autowired
    private DistanceService distanceService;

    @Autowired
    private EmailService emailService;

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
        delivery.setPickupLatitude(request.getPickupLatitude());
        delivery.setPickupLongitude(request.getPickupLongitude());
        delivery.setDropoffLatitude(request.getDropoffLatitude());
        delivery.setDropoffLongitude(request.getDropoffLongitude());

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

    private String generateTrackingNumber() {
        return "QP-" + System.currentTimeMillis();
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

        Delivery savedDelivery = deliveryRepository.save(delivery);

        // Send email notification to sender about status change
        try {
            if (delivery.getSender() != null && delivery.getSender().getUser() != null) {
                emailService.sendDeliveryStatusUpdate(savedDelivery, delivery.getSender().getUser(), oldStatus, status);
                System.out.println("Status update email sent to sender: " + delivery.getSender().getUser().getEmail());
            }
        } catch (Exception e) {
            System.err.println("Failed to send status update email: " + e.getMessage());
        }

        return savedDelivery;
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

        Delivery savedDelivery = deliveryRepository.save(delivery);

        // Send email notification to sender about acceptance
        try {
            if (delivery.getSender() != null && delivery.getSender().getUser() != null) {
                emailService.sendDeliveryStatusUpdate(savedDelivery, delivery.getSender().getUser(), "PENDING", "ACCEPTED");
                System.out.println("Acceptance email sent to sender: " + delivery.getSender().getUser().getEmail());
            }
        } catch (Exception e) {
            System.err.println("Failed to send acceptance email: " + e.getMessage());
        }

        return savedDelivery;
    }

    @Transactional
    public Delivery cancelDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        String oldStatus = delivery.getStatus();
        delivery.setStatus("CANCELLED");
        delivery.setUpdatedAt(LocalDateTime.now());

        TrackingHistory history = new TrackingHistory();
        history.setDelivery(delivery);
        history.setStatus("CANCELLED");
        history.setLocation("Delivery cancelled");
        history.setTimestamp(LocalDateTime.now());
        trackingHistoryRepository.save(history);

        Delivery savedDelivery = deliveryRepository.save(delivery);

        // Send email notification to sender about cancellation
        try {
            if (delivery.getSender() != null && delivery.getSender().getUser() != null) {
                emailService.sendDeliveryStatusUpdate(savedDelivery, delivery.getSender().getUser(), oldStatus, "CANCELLED");
                System.out.println("Cancellation email sent to sender: " + delivery.getSender().getUser().getEmail());
            }
        } catch (Exception e) {
            System.err.println("Failed to send cancellation email: " + e.getMessage());
        }

        return savedDelivery;
    }

    public List<TrackingHistory> getTrackingHistory(Long deliveryId) {
        return trackingHistoryRepository.findByDeliveryIdOrderByTimestampAsc(deliveryId);
    }

    @Transactional
    public Delivery saveDelivery(Delivery delivery) {
        return deliveryRepository.save(delivery);
    }
}