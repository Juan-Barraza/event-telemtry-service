package com.bancolombia.challenge.telemetry.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record TelemetryRequest(
        @NotBlank(message = "deviceId must not be empty")
        String deviceId,

        String timestamp,

        @NotNull(message = "Temperature is required")
        @Min(value = -50, message = "Temperature outside the range min")
        @Max(value = 150, message = "Temperature outside the range max")
        Double temperature,

        @NotNull(message = "humidity is required")
        @Min(value = 0, message = "humidity must not be negative")
        @Max(value = 100, message = "humidity must not exceed of 100%")
        Double humidity,

        String status
) {
}
