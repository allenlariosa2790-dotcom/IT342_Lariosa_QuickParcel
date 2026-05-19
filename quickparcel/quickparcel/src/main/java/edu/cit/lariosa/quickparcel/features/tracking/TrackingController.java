package edu.cit.lariosa.quickparcel.features.tracking;

import edu.cit.lariosa.quickparcel.features.delivery.DeliveryService;
import edu.cit.lariosa.quickparcel.features.shared.entity.Delivery;
import edu.cit.lariosa.quickparcel.features.shared.entity.TrackingHistory;
import edu.cit.lariosa.quickparcel.features.auth.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tracking")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TrackingController {

    @Autowired
    private DeliveryService deliveryService;

    @GetMapping("/delivery/{id}")
    public ResponseEntity<?> getDeliveryById(@PathVariable Long id) {
        return deliveryService.getDeliveryById(id)
                .map(delivery -> ResponseEntity.ok(Map.of("success", true, "data", delivery)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tracking-number/{trackingNumber}")
    public ResponseEntity<?> getDeliveryByTrackingNumber(@PathVariable String trackingNumber) {
        return deliveryService.getDeliveryByTrackingNumber(trackingNumber)
                .map(delivery -> ResponseEntity.ok(Map.of("success", true, "data", delivery)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/delivery/{id}/history")
    public ResponseEntity<?> getTrackingHistory(@PathVariable Long id) {
        List<TrackingHistory> history = deliveryService.getTrackingHistory(id);
        return ResponseEntity.ok(Map.of("success", true, "data", history));
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyDeliveries(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<Delivery> deliveries;
        if ("SENDER".equals(userDetails.getUserType())) {
            deliveries = deliveryService.getDeliveriesBySenderId(userDetails.getId());
        } else if ("RIDER".equals(userDetails.getUserType())) {
            deliveries = deliveryService.getDeliveriesByRiderId(userDetails.getId());
        } else {
            deliveries = List.of();
        }
        return ResponseEntity.ok(Map.of("success", true, "data", deliveries));
    }
}