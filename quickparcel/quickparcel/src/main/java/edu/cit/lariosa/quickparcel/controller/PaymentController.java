package edu.cit.lariosa.quickparcel.controller;

import edu.cit.lariosa.quickparcel.entity.Delivery;
import edu.cit.lariosa.quickparcel.entity.Payment;
import edu.cit.lariosa.quickparcel.entity.Sender;
import edu.cit.lariosa.quickparcel.repository.DeliveryRepository;
import edu.cit.lariosa.quickparcel.repository.PaymentRepository;
import edu.cit.lariosa.quickparcel.repository.SenderRepository;
import edu.cit.lariosa.quickparcel.service.PayMongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Autowired
    private PayMongoService payMongoService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private SenderRepository senderRepository;

    @PostMapping("/paymongo/create")
    public ResponseEntity<?> createPayMongoPayment(@RequestBody Map<String, Object> request) {
        try {
            Long deliveryId = Long.valueOf(request.get("deliveryId").toString());

            // Fix: Handle amount safely
            Number amountNumber = (Number) request.get("amount");
            double amount = amountNumber.doubleValue();
            int amountInCentavos = (int) Math.round(amount * 100);

            String description = (String) request.get("description");

            String successUrl = frontendUrl + "/payment/success?deliveryId=" + deliveryId;
            String cancelUrl = frontendUrl + "/payment/cancel";

            // Create PayMongo payment intent
            Map<String, Object> paymentIntent = payMongoService.createPaymentIntent(amountInCentavos, description, successUrl, cancelUrl);

            // Get delivery and sender
            Delivery delivery = deliveryRepository.findById(deliveryId)
                    .orElseThrow(() -> new RuntimeException("Delivery not found"));
            Sender sender = delivery.getSender();

            // Save payment record using your existing Payment entity
            Payment payment = new Payment();
            payment.setDelivery(delivery);
            payment.setSender(sender);
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

    @GetMapping("/paymongo/status/{paymentReference}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable String paymentReference,
                                              @RequestParam Long deliveryId) {
        try {
            Optional<Payment> paymentOpt = paymentRepository.findByPaymentReference(paymentReference);
            if (paymentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Payment payment = paymentOpt.get();

            // For sandbox/testing, automatically mark as COMPLETED
            // In production, you would verify with PayMongo API
            if ("PENDING".equals(payment.getStatus())) {
                payment.setStatus("COMPLETED");
                payment.setCompletedAt(LocalDateTime.now());
                paymentRepository.save(payment);

                // Mark delivery as paid
                Delivery delivery = payment.getDelivery();
                delivery.setPaymentStatus("PAID");
                deliveryRepository.save(delivery);

                System.out.println("Payment completed for delivery: " + deliveryId);
            }

            return ResponseEntity.ok(Map.of(
                    "status", payment.getStatus(),
                    "deliveryId", payment.getDelivery().getId()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}