package edu.cit.lariosa.quickparcel.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PaymentProcessorFactory {
    private final Map<String, PaymentProcessor> processors = new ConcurrentHashMap<>();

    @Autowired
    public PaymentProcessorFactory(CodPaymentProcessor codProcessor) {
        processors.put("COD", codProcessor);
        // future: processors.put("PAYPAL", paypalProcessor);
    }

    public PaymentProcessor getProcessor(String method) {
        return processors.getOrDefault(method, processors.get("COD"));
    }
}