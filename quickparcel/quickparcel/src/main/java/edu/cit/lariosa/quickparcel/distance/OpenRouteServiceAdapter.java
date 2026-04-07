package edu.cit.lariosa.quickparcel.distance;

import edu.cit.lariosa.quickparcel.dto.OpenRouteServiceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Component
public class OpenRouteServiceAdapter implements DistanceProvider {

    @Value("${ors.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public double getDistanceInKm(String origin, String destination) {
        if (apiKey == null || apiKey.isEmpty()) {
            return -1;
        }
        double[] originCoords = geocode(origin);
        double[] destCoords = geocode(destination);
        if (originCoords == null || destCoords == null) {
            return -1;
        }

        try {
            Map<String, Object> body = Map.of(
                    "locations", new double[][]{ originCoords, destCoords },
                    "metrics", new String[]{"distance"}
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", apiKey);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<OpenRouteServiceResponse> response = restTemplate.exchange(
                    "https://api.openrouteservice.org/v2/matrix/driving-car",
                    HttpMethod.POST,
                    entity,
                    OpenRouteServiceResponse.class
            );

            if (response.getBody() != null && response.getBody().getDistances() != null) {
                double meters = response.getBody().getDistances().get(0).get(1);
                return meters / 1000.0;
            }
        } catch (Exception e) {
            System.err.println("ORS distance error: " + e.getMessage());
        }
        return -1;
    }

    private double[] geocode(String address) {
        try {
            String url = String.format("https://api.openrouteservice.org/geocode/search?api_key=%s&text=%s&size=1",
                    apiKey, java.net.URLEncoder.encode(address, "UTF-8"));
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("features")) {
                var features = (java.util.List<Map<String, Object>>) body.get("features");
                if (!features.isEmpty()) {
                    var geometry = (Map<String, Object>) features.get(0).get("geometry");
                    var coords = (java.util.List<Double>) geometry.get("coordinates");
                    return new double[]{ coords.get(0), coords.get(1) };
                }
            }
        } catch (Exception e) {
            System.err.println("ORS geocode error: " + e.getMessage());
        }
        return null;
    }
}