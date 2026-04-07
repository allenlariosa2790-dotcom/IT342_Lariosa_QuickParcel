package edu.cit.lariosa.quickparcel.service;

import edu.cit.lariosa.quickparcel.distance.DistanceProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class DistanceService {

    private final DistanceProvider distanceProvider;

    public DistanceService(@Qualifier("cachingDistanceProxy") DistanceProvider distanceProvider) {
        this.distanceProvider = distanceProvider;
    }

    public double calculateDistanceInKm(String origin, String destination) {
        double distance = distanceProvider.getDistanceInKm(origin, destination);
        if (distance < 0) {
            // ultimate fallback heuristic
            return fallbackDistance(origin, destination);
        }
        return distance;
    }

    private double fallbackDistance(String origin, String destination) {
        if (origin.equalsIgnoreCase(destination)) return 0.5;
        int diff = Math.abs(origin.length() - destination.length());
        return Math.min(10.0, 1.0 + (diff / 20.0));
    }
}