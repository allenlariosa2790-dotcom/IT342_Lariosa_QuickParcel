package edu.cit.lariosa.quickparcel.distance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ChainedDistanceProvider implements DistanceProvider {

    private final List<DistanceProvider> providers;

    @Autowired
    public ChainedDistanceProvider(OpenRouteServiceAdapter ors, OsrmAdapter osrm) {
        this.providers = List.of(ors, osrm);
    }

    @Override
    public double getDistanceInKm(String origin, String destination) {
        for (DistanceProvider provider : providers) {
            double distance = provider.getDistanceInKm(origin, destination);
            if (distance > 0) {
                return distance;
            }
        }
        return -1;
    }
}