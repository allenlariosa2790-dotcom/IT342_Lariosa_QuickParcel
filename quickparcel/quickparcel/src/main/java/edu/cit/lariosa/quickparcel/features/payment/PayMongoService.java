package edu.cit.lariosa.quickparcel.features.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PayMongoService {

    @Value("${paymongo.secret.key}")
    private String secretKey;

    @Value("${paymongo.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String getAuthHeader() {
        String auth = secretKey + ":";
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        return "Basic " + encodedAuth;
    }

    public Map<String, Object> createGcashPayment(int amountInCentavos, String description, String successUrl, String cancelUrl) {
        try {
            // Create payment intent first
            String paymentIntentId = createPaymentIntent(amountInCentavos, description);
            System.out.println("Payment Intent Created: " + paymentIntentId);

            // Then create GCash source
            String checkoutUrl = createGcashSource(paymentIntentId, amountInCentavos, successUrl, cancelUrl);
            System.out.println("Checkout URL: " + checkoutUrl);

            Map<String, Object> result = new HashMap<>();
            result.put("paymentIntentId", paymentIntentId);
            result.put("checkoutUrl", checkoutUrl);
            return result;

        } catch (Exception e) {
            System.err.println("PayMongo Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("PayMongo payment creation failed: " + e.getMessage());
        }
    }

    private String createPaymentIntent(int amount, String description) throws Exception {
        // Build request body
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("amount", amount);
        attributes.put("currency", "PHP");
        attributes.put("description", description);
        attributes.put("statement_descriptor", "QuickParcel");
        attributes.put("payment_method_allowed", List.of("gcash"));  // ← FIXED

        Map<String, Object> data = new HashMap<>();
        data.put("attributes", attributes);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("data", data);

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", getAuthHeader());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Make API call
        String url = apiUrl + "/payment_intents";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        // Parse response
        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        String paymentIntentId = jsonNode.path("data").path("id").asText();

        return paymentIntentId;
    }

    private String createGcashSource(String paymentIntentId, int amount, String successUrl, String cancelUrl) throws Exception {
        // Build request body for GCash source
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("type", "gcash");
        attributes.put("amount", amount);
        attributes.put("currency", "PHP");
        attributes.put("redirect", Map.of(
                "success", successUrl,
                "failed", cancelUrl
        ));

        Map<String, Object> data = new HashMap<>();
        data.put("attributes", attributes);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("data", data);

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", getAuthHeader());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Make API call
        String url = apiUrl + "/payment_intents/" + paymentIntentId + "/sources";
        System.out.println("Creating source at: " + url);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        // Parse response
        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        String checkoutUrl = jsonNode.path("data").path("attributes").path("redirect").path("checkout_url").asText();

        return checkoutUrl;
    }

    public String getPaymentStatus(String paymentIntentId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", getAuthHeader());

            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = apiUrl + "/payment_intents/" + paymentIntentId;

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String status = jsonNode.path("data").path("attributes").path("status").asText();

            return status;
        } catch (Exception e) {
            System.err.println("Failed to get payment status: " + e.getMessage());
            return "UNKNOWN";
        }
    }
}