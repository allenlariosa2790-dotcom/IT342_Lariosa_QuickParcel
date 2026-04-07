package edu.cit.lariosa.quickparcel.distance;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class OsrmAdapter implements DistanceProvider {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public double getDistanceInKm(String origin, String destination) {
        double[] originCoords = geocodeWithNominatim(origin);
        double[] destCoords = geocodeWithNominatim(destination);
        if (originCoords == null || destCoords == null) {
            return -1;
        }

        try {
            String url = String.format("http://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=false",
                    originCoords[0], originCoords[1], destCoords[0], destCoords[1]);
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            var routes = (java.util.List<Map<String, Object>>) response.getBody().get("routes");
            if (!routes.isEmpty()) {
                double meters = ((Number) routes.get(0).get("distance")).doubleValue();
                return meters / 1000.0;
            }
        } catch (Exception e) {
            System.err.println("OSRM error: " + e.getMessage());
        }
        return -1;
    }

    private double[] geocodeWithNominatim(String address) {
        try {
            String url = String.format("https://nominatim.openstreetmap.org/search?format=json&q=%s&limit=1",
                    java.net.URLEncoder.encode(address, "UTF-8"));
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "QuickParcel/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map[].class);
            if (response.getBody() != null && response.getBody().length > 0) {
                double lat = Double.parseDouble(response.getBody()[0].get("lat").toString());
                double lon = Double.parseDouble(response.getBody()[0].get("lon").toString());
                return new double[]{ lon, lat };
            }
        } catch (Exception e) {
            System.err.println("Nominatim error: " + e.getMessage());
        }
        return null;
    }
}