package com.bancolombia.challenge.telemetry.service;

import com.bancolombia.challenge.telemetry.dto.TelemetryRequest;
import com.bancolombia.challenge.telemetry.dto.TelemetryResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ITelemetryService {
    Mono<TelemetryResponse> processAndSave(TelemetryRequest request);
    Flux<TelemetryResponse> getEventsByDevice(String deviceId, boolean onlyCritical);
    Mono<TelemetryResponse> getEventDetail(String deviceId, String timestamp);
}
