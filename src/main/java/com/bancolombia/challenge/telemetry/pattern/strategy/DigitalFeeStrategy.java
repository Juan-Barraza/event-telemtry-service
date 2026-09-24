package com.bancolombia.challenge.telemetry.pattern.strategy;

import org.springframework.stereotype.Component;

@Component
public class DigitalFeeStrategy implements  FeeCalculationStrategy{
    @Override
    public double calculateFee(double amount) {
        return amount * 0.002; // this is preferential to web/mobile
    }

    @Override
    public String getChannelType() {
        return "DIGITAL";
    }
}
