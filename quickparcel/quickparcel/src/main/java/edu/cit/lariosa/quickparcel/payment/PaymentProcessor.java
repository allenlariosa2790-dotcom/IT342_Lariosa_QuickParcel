package edu.cit.lariosa.quickparcel.payment;

import edu.cit.lariosa.quickparcel.entity.Delivery;

public interface PaymentProcessor {
    void processPayment(Delivery delivery);
}