package edu.cit.lariosa.quickparcel.features.payment;

import edu.cit.lariosa.quickparcel.features.auth.UserDetailsImpl;
import edu.cit.lariosa.quickparcel.features.delivery.DeliveryService;
import edu.cit.lariosa.quickparcel.features.shared.entity.Delivery;
import edu.cit.lariosa.quickparcel.features.shared.entity.Payment;
import edu.cit.lariosa.quickparcel.features.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PayMongoService payMongoService;

    @Autowired
    private StripeService stripeService;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    // GET /api/payments/my - Get payments for the current user
    @GetMapping("/my")
    public ResponseEntity<?> getMyPayments(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            List<Payment> payments = new ArrayList<>();

            if ("SENDER".equals(userDetails.getUserType())) {
                payments = paymentRepository.findBySenderId(userDetails.getId());
            } else if ("RIDER".equals(userDetails.getUserType())) {
                List<Delivery> deliveries = deliveryService.getDeliveriesByRiderId(userDetails.getId());
                payments = deliveries.stream()
                        .map(delivery -> paymentRepository.findByDeliveryId(delivery.getId()).orElse(null))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }

            List<Map<String, Object>> paymentList = payments.stream().map(payment -> {
                Map<String, Object> p = new HashMap<>();
                p.put("id", payment.getId());
                p.put("deliveryId", payment.getDelivery().getId());
                p.put("trackingNumber", payment.getDelivery().getTrackingNumber());
                p.put("amount", payment.getAmount());
                p.put("paymentMethod", payment.getPaymentMethod());
                p.put("paymentStatus", payment.getStatus());
                p.put("createdAt", payment.getCreatedAt().toString());
                return p;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("success", true, "data", paymentList));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{deliveryId}/mark-paid")
    public ResponseEntity<?> markDeliveryAsPaid(@PathVariable Long deliveryId) {
        try {
            Delivery delivery = deliveryService.getDeliveryById(deliveryId)
                    .orElseThrow(() -> new RuntimeException("Delivery not found"));
            delivery.setPaymentStatus("PAID");
            deliveryService.saveDelivery(delivery);
            System.out.println("✅ Delivery " + deliveryId + " marked as PAID");
            return ResponseEntity.ok(Map.of("success", true, "message", "Payment marked as collected.", "paymentStatus", "PAID"));
        } catch (Exception e) {
            System.err.println("❌ Failed to mark delivery as paid: " + e.getMessage());
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

    @PostMapping("/paymongo/create")
    public ResponseEntity<?> createPayMongoPayment(@RequestBody Map<String, Object> request) {
        try {
            Long deliveryId = Long.valueOf(request.get("deliveryId").toString());
            Number amountNumber = (Number) request.get("amount");
            double amount = amountNumber.doubleValue();
            int amountInCentavos = (int) Math.round(amount * 100);
            String description = (String) request.get("description");

            String successUrl = frontendUrl + "/payment/success?deliveryId=" + deliveryId;
            String cancelUrl = frontendUrl + "/payment/cancel";

            Map<String, Object> paymentIntent = payMongoService.createGcashPayment(
                    amountInCentavos, description, successUrl, cancelUrl);

            Delivery delivery = deliveryService.getDeliveryById(deliveryId)
                    .orElseThrow(() -> new RuntimeException("Delivery not found"));

            Payment payment = new Payment();
            payment.setDelivery(delivery);
            payment.setSender(delivery.getSender());
            payment.setAmount(amount);
            payment.setCurrency("PHP");
            payment.setStatus("PENDING");
            payment.setPaymentMethod("PAYMONGO_GCASH");
            payment.setPaymentReference(paymentIntent.get("paymentIntentId").toString());
            payment.setCreatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            Map<String, Object> response = new HashMap<>();
            response.put("checkoutUrl", paymentIntent.get("checkoutUrl"));
            response.put("paymentIntentId", paymentIntent.get("paymentIntentId"));
            response.put("paymentId", payment.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/stripe/create-payment-intent")
    public ResponseEntity<?> createStripePaymentIntent(@RequestBody Map<String, Object> request) {
        try {
            Long deliveryId = Long.valueOf(request.get("deliveryId").toString());
            Double amount = Double.valueOf(request.get("amount").toString());
            String description = (String) request.get("description");

            Map<String, String> paymentIntent = stripeService.createPaymentIntent(deliveryId, amount, description);

            return ResponseEntity.ok(Map.of(
                    "clientSecret", paymentIntent.get("clientSecret"),
                    "paymentIntentId", paymentIntent.get("paymentIntentId")
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stripe/status/{paymentIntentId}")
    public ResponseEntity<?> getStripePaymentStatus(@PathVariable String paymentIntentId) {
        try {
            var paymentIntent = stripeService.getPaymentIntent(paymentIntentId);
            return ResponseEntity.ok(Map.of("status", paymentIntent.getStatus()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stripe/test")
    public ResponseEntity<?> testStripe() {
        return ResponseEntity.ok(Map.of("result", stripeService.testConnection()));
    }
}