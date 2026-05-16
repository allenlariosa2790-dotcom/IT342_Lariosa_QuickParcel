package edu.cit.lariosa.quickparcel.features.payment;

import edu.cit.lariosa.quickparcel.features.delivery.DeliveryService;
import edu.cit.lariosa.quickparcel.features.shared.entity.Delivery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private DeliveryService deliveryService;

    @PutMapping("/{deliveryId}/mark-paid")
    public ResponseEntity<?> markDeliveryAsPaid(@PathVariable Long deliveryId) {
        try {
            Delivery delivery = deliveryService.getDeliveryById(deliveryId)
                    .orElseThrow(() -> new RuntimeException("Delivery not found"));
            delivery.setPaymentStatus("PAID");
            deliveryService.saveDelivery(delivery);
            return ResponseEntity.ok(Map.of("success", true, "message", "Payment marked as collected."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody Map<String, Object> request) {
        try {
            Long deliveryId = Long.valueOf(request.get("deliveryId").toString());
            Double amount = Double.valueOf(request.get("amount").toString());
            String paymentMethod = (String) request.get("paymentMethod");

            var payment = paymentService.createPaymentRecord(deliveryId, amount, paymentMethod);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("paymentId", payment.getId());
            response.put("status", payment.getStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}