package edu.cit.lariosa.quickparcel.features.payment;

import edu.cit.lariosa.quickparcel.features.delivery.DeliveryService;
import edu.cit.lariosa.quickparcel.features.shared.entity.Delivery;
import edu.cit.lariosa.quickparcel.features.shared.entity.Payment;
import edu.cit.lariosa.quickparcel.features.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private DeliveryService deliveryService;

    public Payment createPaymentRecord(Long deliveryId, Double amount, String paymentMethod) {
        Delivery delivery = deliveryService.getDeliveryById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        Payment payment = new Payment();
        payment.setDelivery(delivery);
        payment.setSender(delivery.getSender());
        payment.setAmount(amount);
        payment.setCurrency("PHP");
        payment.setStatus("PENDING");
        payment.setPaymentMethod(paymentMethod);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setPaymentReference("PAY_" + System.currentTimeMillis());

        return paymentRepository.save(payment);
    }

    public Payment markAsPaid(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setStatus("COMPLETED");
        payment.setCompletedAt(LocalDateTime.now());

        // Mark delivery as paid
        Delivery delivery = payment.getDelivery();
        delivery.setPaymentStatus("PAID");
        deliveryService.saveDelivery(delivery); // Add this method to DeliveryService

        return paymentRepository.save(payment);
    }
}