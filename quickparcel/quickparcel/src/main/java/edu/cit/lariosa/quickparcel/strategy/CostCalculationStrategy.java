package edu.cit.lariosa.quickparcel.strategy;

public interface CostCalculationStrategy {
    double calculate(double distanceKm, double weightKg);
}