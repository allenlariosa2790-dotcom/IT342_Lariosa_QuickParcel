package edu.cit.lariosa.quickparcel.features.rider;

import edu.cit.lariosa.quickparcel.features.delivery.DeliveryService;
import edu.cit.lariosa.quickparcel.features.shared.entity.Delivery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RiderDeliveryService {

    @Autowired
    private DeliveryService deliveryService;

    public List<Delivery> getAvailableDeliveries() {
        return deliveryService.getAvailableDeliveries();
    }

    public Delivery acceptDelivery(Long deliveryId, Long riderId) {
        return deliveryService.acceptDelivery(deliveryId, riderId);
    }

    public Delivery updateDeliveryStatus(Long deliveryId, String status, String location) {
        return deliveryService.updateDeliveryStatus(deliveryId, status, location);
    }

    public List<Delivery> getMyActiveDeliveries(Long riderId) {
        List<Delivery> allDeliveries = deliveryService.getDeliveriesByRiderId(riderId);
        return allDeliveries.stream()
                .filter(d -> !d.getStatus().equals("DELIVERED") && !d.getStatus().equals("CANCELLED"))
                .collect(Collectors.toList());
    }

    public Delivery getFirstActiveDelivery(Long riderId) {
        List<Delivery> active = getMyActiveDeliveries(riderId);
        return active.isEmpty() ? null : active.get(0);
    }
}