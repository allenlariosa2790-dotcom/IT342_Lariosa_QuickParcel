package edu.cit.lariosa.quickparcel.service;

import edu.cit.lariosa.quickparcel.dto.OpenRouteServiceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DistanceService {

    @Value("${ors.api.key:}") // allow empty
    private String apiKey;

    @Value("${ors.distance.matrix.url:https://api.openrouteservice.org/v2/matrix/driving-car}")
    private String matrixUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_SECONDS = 24 * 60 * 60;

    public double calculateDistanceInKm(String origin, String destination) {
        String cacheKey = origin.toLowerCase().trim() + "|" + destination.toLowerCase().trim();
        CacheEntry cached = cache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached.distanceKm;
        }

        double distance = tryOpenRouteService(origin, destination);
        if (distance < 0) {
            // Fallback to OSRM public instance (no key needed)
            distance = tryOsrm(origin, destination);
        }
        if (distance < 0) {
            // Final fallback: heuristic
            distance = calculateFallbackDistance(origin, destination);
            System.err.println("Using fallback heuristic distance: " + distance + " km");
        } else {
            System.out.println("Distance calculated: " + distance + " km");
        }
        cache.put(cacheKey, new CacheEntry(distance, System.currentTimeMillis()));
        return distance;
    }

    private double tryOpenRouteService(String origin, String destination) {
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("ORS API key missing, skipping ORS");
            return -1;
        }
        double[] originCoords = geocodeWithOrs(origin);
        double[] destCoords = geocodeWithOrs(destination);
        if (originCoords == null || destCoords == null) {
            return -1;
        }
        try {
            Map<String, Object> requestBody = Map.of(
                    "locations", new double[][]{ originCoords, destCoords },
                    "metrics", new String[]{"distance"}
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", apiKey);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<OpenRouteServiceResponse> response = restTemplate.exchange(
                    matrixUrl, HttpMethod.POST, entity, OpenRouteServiceResponse.class);
            OpenRouteServiceResponse body = response.getBody();
            if (body != null && body.getDistances() != null && !body.getDistances().isEmpty()) {
                double distanceMeters = body.getDistances().get(0).get(1);
                return distanceMeters / 1000.0;
            }
        } catch (Exception e) {
            System.err.println("ORS distance error: " + e.getMessage());
        }
        return -1;
    }

    private double[] geocodeWithOrs(String address) {
        try {
            String url = String.format("https://api.openrouteservice.org/geocode/search?api_key=%s&text=%s&size=1",
                    apiKey, java.net.URLEncoder.encode(address, "UTF-8"));
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("features")) {
                java.util.List<Map<String, Object>> features = (java.util.List<Map<String, Object>>) body.get("features");
                if (!features.isEmpty()) {
                    Map<String, Object> geometry = (Map<String, Object>) features.get(0).get("geometry");
                    java.util.List<Double> coords = (java.util.List<Double>) geometry.get("coordinates");
                    return new double[]{ coords.get(0), coords.get(1) };
                }
            }
        } catch (Exception e) {
            System.err.println("ORS geocoding failed: " + address + " - " + e.getMessage());
        }
        return null;
    }

    /**
     * Fallback using OSRM public instance (no API key) and Nominatim geocoding.
     */
    private double tryOsrm(String origin, String destination) {
        double[] originCoords = geocodeWithNominatim(origin);
        double[] destCoords = geocodeWithNominatim(destination);
        if (originCoords == null || destCoords == null) {
            return -1;
        }
        try {
            String url = String.format("http://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=false",
                    originCoords[0], originCoords[1], destCoords[0], destCoords[1]);
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("routes")) {
                java.util.List<Map<String, Object>> routes = (java.util.List<Map<String, Object>>) body.get("routes");
                if (!routes.isEmpty()) {
                    double distanceMeters = ((Number) routes.get(0).get("distance")).doubleValue();
                    return distanceMeters / 1000.0;
                }
            }
        } catch (Exception e) {
            System.err.println("OSRM distance error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Geocode using Nominatim (free, no key, but rate limited).
     */
    private double[] geocodeWithNominatim(String address) {
        try {
            String url = String.format("https://nominatim.openstreetmap.org/search?format=json&q=%s&limit=1",
                    java.net.URLEncoder.encode(address, "UTF-8"));
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "QuickParcel/1.0"); // Required by Nominatim
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map[].class);
            Map[] body = response.getBody();
            if (body != null && body.length > 0) {
                double lat = Double.parseDouble(body[0].get("lat").toString());
                double lon = Double.parseDouble(body[0].get("lon").toString());
                return new double[]{ lon, lat };
            }
        } catch (Exception e) {
            System.err.println("Nominatim geocoding failed: " + address + " - " + e.getMessage());
        }
        return null;
    }

    private double calculateFallbackDistance(String origin, String destination) {
        if (origin.equalsIgnoreCase(destination)) return 0.5;
        int diff = Math.abs(origin.length() - destination.length());
        return Math.min(10.0, 1.0 + (diff / 20.0));
    }

    private static class CacheEntry {
        double distanceKm;
        long timestamp;
        CacheEntry(double distanceKm, long timestamp) {
            this.distanceKm = distanceKm;
            this.timestamp = timestamp;
        }
        boolean isExpired() {
            return (System.currentTimeMillis() - timestamp) > (CACHE_TTL_SECONDS * 1000);
        }
    }
}