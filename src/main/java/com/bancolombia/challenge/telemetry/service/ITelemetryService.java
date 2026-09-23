package com.bancolombia.challenge.telemetry.service;

import com.bancolombia.challenge.telemetry.dto.TelemetryResponse;
import com.bancolombia.challenge.telemetry.dto.TransactionTelemetryRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ITelemetryService {
    Mono<TelemetryResponse> processAndSave(TransactionTelemetryRequest request);
    Flux<TelemetryResponse> getEventsByAccount(String accountId, boolean onlyHighRisk);
    Mono<TelemetryResponse> getEventDetail(String accountId, String timestamp);
}