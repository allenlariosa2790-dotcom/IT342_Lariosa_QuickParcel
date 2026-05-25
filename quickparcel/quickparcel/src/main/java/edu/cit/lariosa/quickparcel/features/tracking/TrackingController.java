package edu.cit.lariosa.quickparcel.features.tracking;

import edu.cit.lariosa.quickparcel.features.auth.UserDetailsImpl;
import edu.cit.lariosa.quickparcel.features.delivery.DeliveryService;
import edu.cit.lariosa.quickparcel.features.shared.entity.Delivery;
import edu.cit.lariosa.quickparcel.features.shared.entity.File;
import edu.cit.lariosa.quickparcel.features.shared.entity.Parcel;
import edu.cit.lariosa.quickparcel.features.shared.entity.TrackingHistory;
import edu.cit.lariosa.quickparcel.features.shared.repository.FileRepository;
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

    @Autowired
    private FileRepository fileRepository;

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

    @GetMapping("/delivery/{id}/image")
    public ResponseEntity<?> getParcelImage(@PathVariable Long id) {
        try {
            Delivery delivery = deliveryService.getDeliveryById(id)
                    .orElseThrow(() -> new RuntimeException("Delivery not found"));

            Parcel parcel = delivery.getParcel();
            if (parcel == null) {
                return ResponseEntity.ok(Map.of("hasImage", false));
            }

            // Find image associated with this parcel
            List<File> files = fileRepository.findByParcel(parcel);
            java.util.Optional<File> imageFile = files.stream()
                    .filter(f -> !f.isProfilePicture())
                    .findFirst();

            if (imageFile.isPresent()) {
                return ResponseEntity.ok(Map.of(
                        "hasImage", true,
                        "imageUrl", imageFile.get().getFilePath()
                ));
            }

            return ResponseEntity.ok(Map.of("hasImage", false));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}