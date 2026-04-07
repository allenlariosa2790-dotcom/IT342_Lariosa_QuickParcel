package edu.cit.lariosa.quickparcel.payment;

import edu.cit.lariosa.quickparcel.entity.Delivery;
import org.springframework.stereotype.Component;

@Component
public class CodPaymentProcessor implements PaymentProcessor {
    @Override
    public void processPayment(Delivery delivery) {
        delivery.setPaymentStatus("PENDING");
        // no online call
    }
}