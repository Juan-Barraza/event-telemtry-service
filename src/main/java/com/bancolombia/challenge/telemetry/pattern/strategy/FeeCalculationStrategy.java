package com.bancolombia.challenge.telemetry.pattern.strategy;

public interface FeeCalculationStrategy {
    double calculateFee(double amount);
    String getChannelType();
}
