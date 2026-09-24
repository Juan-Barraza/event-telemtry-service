package com.bancolombia.challenge.telemetry.pattern.factory;


import com.bancolombia.challenge.telemetry.pattern.strategy.AtmFeeStrategy;
import com.bancolombia.challenge.telemetry.pattern.strategy.DefaultFeeStrategy;
import com.bancolombia.challenge.telemetry.pattern.strategy.DigitalFeeStrategy;
import com.bancolombia.challenge.telemetry.pattern.strategy.FeeCalculationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeeStrategyFactory {
    private final AtmFeeStrategy atmStrategy;
    private final DigitalFeeStrategy digitalStrategy;
    private final DefaultFeeStrategy defaultStrategy;

    public FeeCalculationStrategy getStrategy(String channel) {
        if (channel == null) {
            return defaultStrategy;
        }

        return switch (channel.toUpperCase()) {
            case "ATM" -> atmStrategy;
            case "WEB", "MOBILE", "APP" ->digitalStrategy;
            default -> defaultStrategy;
        };
    }
}
