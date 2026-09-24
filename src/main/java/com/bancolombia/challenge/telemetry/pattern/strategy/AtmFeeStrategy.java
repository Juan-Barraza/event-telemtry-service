package com.bancolombia.challenge.telemetry.pattern.strategy;

import org.springframework.stereotype.Component;

@Component
public class AtmFeeStrategy  implements FeeCalculationStrategy{
    @Override
    public double calculateFee(double amount) {
        return 2500.0 + (amount *0.01);
    }

    @Override
    public String getChannelType() {
        return "ATM";
    }
}
