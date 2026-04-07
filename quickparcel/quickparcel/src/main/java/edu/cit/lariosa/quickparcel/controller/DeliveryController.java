package edu.cit.lariosa.quickparcel.controller;

import edu.cit.lariosa.quickparcel.dto.CreateDeliveryRequest;
import edu.cit.lariosa.quickparcel.entity.Delivery;
import edu.cit.lariosa.quickparcel.entity.TrackingHistory;
import edu.cit.lariosa.quickparcel.security.UserDetailsImpl;
import edu.cit.lariosa.quickparcel.service.DeliveryService;
import edu.cit.lariosa.quickparcel.service.DistanceService;
import edu.cit.lariosa.quickparcel.strategy.CostCalculationStrategy;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/deliveries")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;
    @Autowired
    private DistanceService distanceService;
    @Autowired
    private CostCalculationStrategy costStrategy;

    // Create a new delivery (Sender only)
    @PostMapping
    public ResponseEntity<?> createDelivery(@Valid @RequestBody CreateDeliveryRequest request,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            // userDetails.getId() is the user_id from the users table.
            // Your DeliveryService should accept the DTO and the sender's user id.
            Delivery savedDelivery = deliveryService.createDelivery(request, userDetails.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", savedDelivery);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Get delivery by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getDeliveryById(@PathVariable Long id) {
        return deliveryService.getDeliveryById(id)
                .map(delivery -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", delivery);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Get delivery by tracking number
    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<?> getDeliveryByTrackingNumber(@PathVariable String trackingNumber) {
        return deliveryService.getDeliveryByTrackingNumber(trackingNumber)
                .map(delivery -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", delivery);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Get available deliveries (for riders)
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableDeliveries() {
        List<Delivery> deliveries = deliveryService.getAvailableDeliveries();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", deliveries);
        return ResponseEntity.ok(response);
    }

    // Get my deliveries (for sender - based on authenticated user)
    @GetMapping("/my")
    public ResponseEntity<?> getMyDeliveries(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<Delivery> deliveries;
        if (userDetails.getUserType().equals("SENDER")) {
            deliveries = deliveryService.getDeliveriesBySenderId(userDetails.getId());
        } else if (userDetails.getUserType().equals("RIDER")) {
            deliveries = deliveryService.getDeliveriesByRiderId(userDetails.getId());
        } else {
            deliveries = List.of();
        }
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", deliveries);
        return ResponseEntity.ok(response);
    }

    // Accept a delivery (Rider only)
    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptDelivery(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Delivery delivery = deliveryService.acceptDelivery(id, userDetails.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", delivery);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Update delivery status (Rider only)
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateDeliveryStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            String location = request.get("location");
            Delivery delivery = deliveryService.updateDeliveryStatus(id, status, location);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", delivery);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Cancel delivery (Sender or Admin)
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelDelivery(@PathVariable Long id) {
        try {
            Delivery delivery = deliveryService.cancelDelivery(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", delivery);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Get tracking history
    @GetMapping("/{id}/track")
    public ResponseEntity<?> getTrackingHistory(@PathVariable Long id) {
        List<TrackingHistory> history = deliveryService.getTrackingHistory(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", history);
        return ResponseEntity.ok(response);
    }

    // Get activedelivery
    @GetMapping("/my/active")
    public ResponseEntity<?> getMyActiveDelivery(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        // Rider's user id -> find rider record -> find delivery with status not DELIVERED/CANCELLED and rider.id = that rider
        List<Delivery> deliveries = deliveryService.getDeliveriesByRiderId(userDetails.getId())
                .stream()
                .filter(d -> !d.getStatus().equals("DELIVERED") && !d.getStatus().equals("CANCELLED"))
                .collect(Collectors.toList());
        Delivery active = deliveries.isEmpty() ? null : deliveries.get(0);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", active);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/mark-paid")
    public ResponseEntity<?> markDeliveryAsPaid(@PathVariable Long id) {
        try {
            Delivery delivery = deliveryService.markPaymentAsPaid(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Payment marked as collected.", "data", delivery));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/calculate-distance")
    public ResponseEntity<?> calculateDistance(@RequestBody Map<String, Object> request) {
        String origin = (String) request.get("pickupAddress");
        String destination = (String) request.get("dropoffAddress");
        Double weight = request.containsKey("weight") ? ((Number) request.get("weight")).doubleValue() : 1.0;

        if (origin == null || destination == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing addresses"));
        }

        double distanceKm = distanceService.calculateDistanceInKm(origin, destination);
        double estimatedCost = costStrategy.calculate(distanceKm, weight);
        return ResponseEntity.ok(Map.of(
                "distance", distanceKm,
                "estimatedCost", estimatedCost
        ));
    }

}