package edu.cit.lariosa.quickparcel.distance;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component

// @Primary
public class CachingDistanceProxy implements DistanceProvider {

    private final DistanceProvider delegate;
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long TTL_MS = 24 * 60 * 60 * 1000; // 24 hours

    public CachingDistanceProxy(@Qualifier("chainedDistanceProvider") DistanceProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public double getDistanceInKm(String origin, String destination) {
        String key = origin.toLowerCase().trim() + "|" + destination.toLowerCase().trim();
        CacheEntry entry = cache.get(key);
        if (entry != null && (System.currentTimeMillis() - entry.timestamp) < TTL_MS) {
            return entry.distance;
        }
        double distance = delegate.getDistanceInKm(origin, destination);
        if (distance > 0) {
            cache.put(key, new CacheEntry(distance, System.currentTimeMillis()));
        }
        return distance;
    }

    private record CacheEntry(double distance, long timestamp) {}
}