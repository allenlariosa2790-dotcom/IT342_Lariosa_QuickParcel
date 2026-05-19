package edu.cit.lariosa.quickparcel.features.delivery;

import edu.cit.lariosa.quickparcel.features.delivery.dto.CreateDeliveryRequest;
import edu.cit.lariosa.quickparcel.features.shared.entity.Delivery;
import edu.cit.lariosa.quickparcel.features.auth.UserDetailsImpl;
import edu.cit.lariosa.quickparcel.features.delivery.DeliveryService;
import edu.cit.lariosa.quickparcel.features.delivery.DistanceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/deliveries")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private DistanceService distanceService;

    // Create a new delivery (Sender only)
    @PostMapping
    public ResponseEntity<?> createDelivery(@Valid @RequestBody CreateDeliveryRequest request,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Delivery savedDelivery = deliveryService.createDelivery(request, userDetails.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", savedDelivery);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Accept a delivery (Rider only)
    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptDelivery(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Delivery delivery = deliveryService.acceptDelivery(id, userDetails.getId());
            return ResponseEntity.ok(Map.of("success", true, "data", delivery));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
            return ResponseEntity.ok(Map.of("success", true, "data", delivery));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Cancel delivery (Sender or Admin)
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelDelivery(@PathVariable Long id) {
        try {
            Delivery delivery = deliveryService.cancelDelivery(id);
            return ResponseEntity.ok(Map.of("success", true, "data", delivery));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Mark payment as collected (Rider)
    @PutMapping("/{id}/mark-paid")
    public ResponseEntity<?> markDeliveryAsPaid(@PathVariable Long id) {
        try {
            Delivery delivery = deliveryService.markPaymentAsPaid(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Payment marked as collected.", "data", delivery));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Calculate distance (utility endpoint)
    @PostMapping("/calculate-distance")
    public ResponseEntity<?> calculateDistance(@RequestBody Map<String, Object> request) {
        String origin = (String) request.get("pickupAddress");
        String destination = (String) request.get("dropoffAddress");
        Double weight = request.containsKey("weight") ? ((Number) request.get("weight")).doubleValue() : 1.0;

        if (origin == null || destination == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing addresses"));
        }

        double distanceKm = distanceService.calculateDistanceInKm(origin, destination);
        double baseFare = 50.0;
        double perKmRate = 20.0;
        double weightSurcharge = Math.max(0, (weight - 2) * 10);
        double estimatedCost = baseFare + (distanceKm * perKmRate) + weightSurcharge;

        return ResponseEntity.ok(Map.of(
                "distance", distanceKm,
                "estimatedCost", estimatedCost
        ));
    }
}