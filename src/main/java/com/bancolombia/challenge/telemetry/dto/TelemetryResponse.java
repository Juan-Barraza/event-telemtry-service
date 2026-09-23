package com.bancolombia.challenge.telemetry.dto;

import lombok.Builder;

@Builder
public record TelemetryResponse(
        String transactionId,
        String accountId,
        String timestamp,
        Double amount,
        Double calculatedFee,
        String channel,
        String paymentProvider,
        String status,
        Boolean isHighRisk
) {
}