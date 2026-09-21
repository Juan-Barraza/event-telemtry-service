package com.bancolombia.challenge.telemetry.dto;

import lombok.Builder;

@Builder
public record TelemetryResponse(
        String deviceId,
        String timestamp,
        Double temperature,
        Double humidity,
        String status,
        Boolean isCritical
) {
}
