package edu.cit.lariosa.quickparcel.features.tracking;

import edu.cit.lariosa.quickparcel.features.auth.UserDetailsImpl;
import edu.cit.lariosa.quickparcel.features.delivery.DeliveryService;
import edu.cit.lariosa.quickparcel.features.shared.entity.Delivery;
import edu.cit.lariosa.quickparcel.features.shared.entity.TrackingHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deliveries")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TrackingController {

    @Autowired
    private DeliveryService deliveryService;

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

    @GetMapping("/{id}/track")
    public ResponseEntity<?> getTrackingHistory(@PathVariable Long id) {
        List<TrackingHistory> history = deliveryService.getTrackingHistory(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", history);
        return ResponseEntity.ok(response);
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
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", deliveries);
        return ResponseEntity.ok(response);
    }
}