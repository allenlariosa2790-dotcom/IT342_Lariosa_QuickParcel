package edu.cit.lariosa.quickparcel.features.rider;

import edu.cit.lariosa.quickparcel.features.auth.UserDetailsImpl;
import edu.cit.lariosa.quickparcel.features.delivery.DeliveryService;
import edu.cit.lariosa.quickparcel.features.shared.entity.Delivery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rider/deliveries")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RiderDeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableDeliveries() {
        List<Delivery> deliveries = deliveryService.getAvailableDeliveries();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", deliveries);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptDelivery(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Delivery delivery = deliveryService.acceptDelivery(id, userDetails.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", delivery);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateDeliveryStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            String location = request.get("location");
            Delivery delivery = deliveryService.updateDeliveryStatus(id, status, location);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", delivery);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}