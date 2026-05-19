package edu.cit.lariosa.quickparcel.features.rider;

import edu.cit.lariosa.quickparcel.features.auth.UserDetailsImpl;
import edu.cit.lariosa.quickparcel.features.shared.entity.Delivery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rider/deliveries")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RiderDeliveryController {

    @Autowired
    private RiderDeliveryService riderDeliveryService;

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableDeliveries() {
        List<Delivery> deliveries = riderDeliveryService.getAvailableDeliveries();
        return ResponseEntity.ok(Map.of("success", true, "data", deliveries));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptDelivery(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Delivery delivery = riderDeliveryService.acceptDelivery(id, userDetails.getId());
            return ResponseEntity.ok(Map.of("success", true, "data", delivery));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateDeliveryStatus(@PathVariable Long id,
                                                  @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            String location = request.get("location");
            Delivery delivery = riderDeliveryService.updateDeliveryStatus(id, status, location);
            return ResponseEntity.ok(Map.of("success", true, "data", delivery));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<?> getMyActiveDelivery(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Delivery active = riderDeliveryService.getFirstActiveDelivery(userDetails.getId());
        return ResponseEntity.ok(Map.of("success", true, "data", active));
    }
}