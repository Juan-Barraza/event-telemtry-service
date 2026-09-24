package com.bancolombia.challenge.telemetry.pattern.strategy;

import org.springframework.stereotype.Component;

@Component
public class DefaultFeeStrategy implements FeeCalculationStrategy {
    @Override
    public double calculateFee(double amount) {
        return amount * 0.005;
    }

    @Override
    public String getChannelType() {
        return "DEFAULT";
    }
}
