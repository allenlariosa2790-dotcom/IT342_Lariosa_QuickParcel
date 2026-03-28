package edu.cit.lariosa.quickparcel.controller;

import edu.cit.lariosa.quickparcel.entity.Delivery;
import edu.cit.lariosa.quickparcel.entity.TrackingHistory;
import edu.cit.lariosa.quickparcel.security.UserDetailsImpl;
import edu.cit.lariosa.quickparcel.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deliveries")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    // Create a new delivery (Sender only)
    @PostMapping
    public ResponseEntity<?> createDelivery(@RequestBody Delivery delivery) {
        try {
            Delivery savedDelivery = deliveryService.createDelivery(delivery);
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
        List<Delivery> deliveries = deliveryService.getDeliveriesBySenderId(userDetails.getId());
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
}