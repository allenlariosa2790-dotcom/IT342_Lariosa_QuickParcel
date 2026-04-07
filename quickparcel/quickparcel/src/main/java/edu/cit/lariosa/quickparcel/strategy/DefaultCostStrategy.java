package edu.cit.lariosa.quickparcel.strategy;

import org.springframework.stereotype.Component;

@Component
public class DefaultCostStrategy implements CostCalculationStrategy {
    @Override
    public double calculate(double distanceKm, double weightKg) {
        double baseFare = 50.0;
        double perKmRate = 20.0;
        double weightSurcharge = Math.max(0, (weightKg - 2) * 10);
        return baseFare + (distanceKm * perKmRate) + weightSurcharge;
    }
}