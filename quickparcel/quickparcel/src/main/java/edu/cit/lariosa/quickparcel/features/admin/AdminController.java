package edu.cit.lariosa.quickparcel.features.admin;

import edu.cit.lariosa.quickparcel.features.delivery.DeliveryService;
import edu.cit.lariosa.quickparcel.features.shared.entity.Delivery;
import edu.cit.lariosa.quickparcel.features.shared.entity.User;
import edu.cit.lariosa.quickparcel.features.auth.repository.UserRepository;
import edu.cit.lariosa.quickparcel.features.delivery.repository.DeliveryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private DeliveryService deliveryService;

    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        try {
            List<User> allUsers = userRepository.findAll();
            List<Delivery> allDeliveries = deliveryRepository.findAll();

            long totalSenders = allUsers.stream().filter(u -> "SENDER".equals(u.getUserType())).count();
            long totalRiders = allUsers.stream().filter(u -> "RIDER".equals(u.getUserType())).count();
            long activeRiders = allUsers.stream().filter(u -> "RIDER".equals(u.getUserType()) && u.isActive()).count();

            long totalDeliveries = allDeliveries.size();
            long pendingDeliveries = allDeliveries.stream().filter(d -> "PENDING".equals(d.getStatus())).count();
            long completedDeliveries = allDeliveries.stream().filter(d -> "DELIVERED".equals(d.getStatus())).count();

            double totalEarnings = allDeliveries.stream()
                    .filter(d -> "DELIVERED".equals(d.getStatus()))
                    .mapToDouble(d -> d.getEstimatedCost() != null ? d.getEstimatedCost() : 0)
                    .sum();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", allUsers.size());
            stats.put("totalSenders", totalSenders);
            stats.put("totalRiders", totalRiders);
            stats.put("totalDeliveries", totalDeliveries);
            stats.put("pendingDeliveries", pendingDeliveries);
            stats.put("completedDeliveries", completedDeliveries);
            stats.put("totalEarnings", totalEarnings);
            stats.put("activeRiders", activeRiders);

            return ResponseEntity.ok(Map.of("success", true, "data", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            return ResponseEntity.ok(Map.of("success", true, "data", users));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long userId, @RequestBody Map<String, Boolean> request) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setActive(request.get("isActive"));
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("success", true, "message", "User status updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/deliveries")
    public ResponseEntity<?> getAllDeliveries() {
        try {
            List<Delivery> deliveries = deliveryRepository.findAll();
            return ResponseEntity.ok(Map.of("success", true, "data", deliveries));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/deliveries/{deliveryId}/cancel")
    public ResponseEntity<?> cancelDelivery(@PathVariable Long deliveryId) {
        try {
            Delivery delivery = deliveryService.cancelDelivery(deliveryId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Delivery cancelled", "data", delivery));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}