package com.bancolombia.challenge.telemetry.pattern.observer;

public record HighRiskTransactionEvent(
        String transactionId,
        String accountId,
        Double amount,
        String channel,
        String timestamp
) {
}
