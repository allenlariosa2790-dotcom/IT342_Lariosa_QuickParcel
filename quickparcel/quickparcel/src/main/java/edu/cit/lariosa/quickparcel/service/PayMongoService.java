package edu.cit.lariosa.quickparcel.service;

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

    /**
     * Create a Payment Intent for GCash or Card payment
     */
    public Map<String, Object> createPaymentIntent(int amount, String description, String successUrl, String cancelUrl) {
        try {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("amount", amount);
            attributes.put("currency", "PHP");
            attributes.put("description", description);
            attributes.put("statement_descriptor", "QuickParcel");
            attributes.put("payment_method_allowed", List.of("gcash", "card"));

            Map<String, Object> data = new HashMap<>();
            data.put("attributes", attributes);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("data", data);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", getAuthHeader());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = apiUrl + "/payment_intents";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String paymentIntentId = jsonNode.path("data").path("id").asText();

            // Fix: Extract checkout URL correctly
            String checkoutUrl = null;
            JsonNode nextAction = jsonNode.path("data").path("attributes").path("next_action");
            if (!nextAction.isMissingNode() && nextAction.has("redirect")) {
                checkoutUrl = nextAction.path("redirect").path("url").asText();
            }

            // If no checkout URL, create a payment source (for GCash)
            if (checkoutUrl == null || checkoutUrl.isEmpty()) {
                checkoutUrl = createPaymentSource(paymentIntentId, successUrl, cancelUrl);
            }

            String status = jsonNode.path("data").path("attributes").path("status").asText();

            Map<String, Object> result = new HashMap<>();
            result.put("paymentIntentId", paymentIntentId);
            result.put("checkoutUrl", checkoutUrl);
            result.put("status", status);

            System.out.println("PayMongo Response - ID: " + paymentIntentId);
            System.out.println("PayMongo Response - Checkout URL: " + checkoutUrl);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create PayMongo payment intent: " + e.getMessage());
        }
    }

    // Add this method to create a payment source (GCash specific)
    private String createPaymentSource(String paymentIntentId, String successUrl, String cancelUrl) {
        try {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("type", "gcash");
            attributes.put("currency", "PHP");

            Map<String, Object> data = new HashMap<>();
            data.put("attributes", attributes);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("data", data);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", getAuthHeader());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = apiUrl + "/payment_intents/" + paymentIntentId + "/sources";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String checkoutUrl = jsonNode.path("data").path("attributes").path("redirect").path("checkout_url").asText();

            return checkoutUrl;
        } catch (Exception e) {
            System.err.println("Failed to create payment source: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieve Payment Intent status
     */
    public String getPaymentIntentStatus(String paymentIntentId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", getAuthHeader());

            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = apiUrl + "/payment_intents/" + paymentIntentId;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.path("data").path("attributes").path("status").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get payment intent status: " + e.getMessage());
        }
    }
}