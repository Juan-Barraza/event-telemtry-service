package com.bancolombia.challenge.telemetry.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record TransactionTelemetryRequest(
        @NotBlank(message = "transactionId must not be empty")
        String transactionId,

        @NotBlank(message = "accountId must not be empty")
        String accountId,

        String timestamp,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than zero")
        Double amount,

        @NotBlank(message = "channel must not be empty")
        String channel,

        @NotBlank(message = "paymentProvider must not be empty")
        String paymentProvider,

        String status
) {
}