package edu.cit.lariosa.quickparcel.features.payment;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {

    @Value("${stripe.secret.key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    public Map<String, String> createPaymentIntent(Long deliveryId, Double amount, String description) throws Exception {
        // Convert amount to cents (Stripe uses smallest currency unit)
        long amountInCents = Math.round(amount * 100);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("php")
                .setDescription(description)
                .putMetadata("deliveryId", deliveryId.toString())
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        Map<String, String> response = new HashMap<>();
        response.put("clientSecret", paymentIntent.getClientSecret());
        response.put("paymentIntentId", paymentIntent.getId());

        return response;
    }

    public String testConnection() {
        try {
            // Simple test to verify Stripe API key works
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(100L)
                    .setCurrency("php")
                    .build();
            PaymentIntent intent = PaymentIntent.create(params);
            return "Stripe working! Intent ID: " + intent.getId();
        } catch (Exception e) {
            return "Stripe error: " + e.getMessage();
        }
    }

    public PaymentIntent getPaymentIntent(String paymentIntentId) throws Exception {
        return PaymentIntent.retrieve(paymentIntentId);
    }
}