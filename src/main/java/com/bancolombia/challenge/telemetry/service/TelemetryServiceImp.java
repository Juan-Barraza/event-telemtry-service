package com.bancolombia.challenge.telemetry.service;

import com.bancolombia.challenge.telemetry.dto.TelemetryRequest;
import com.bancolombia.challenge.telemetry.dto.TelemetryResponse;
import com.bancolombia.challenge.telemetry.model.TelemetryEvent;
import com.bancolombia.challenge.telemetry.repository.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryServiceImp implements ITelemetryService{
    private final TelemetryRepository repository;

    @Override
    public Mono<TelemetryResponse> processAndSave(TelemetryRequest request) {
        return Mono.just(request)
                .map(this::enrichRequest)
                .map(this::mapToEntity)
                .flatMap(repository::save)
                .retry(2)
                .map(this::mapToResponse)
                .doOnSuccess(saved -> log.info("Event saved successfully to device: {}", saved.deviceId()))
                .onErrorResume(ex -> {
                    log.error("Error to process telemetry to {}: {}", request.deviceId(), ex.getMessage());
                    return Mono.error(new RuntimeException("Error processing reactive telemetry", ex));
                });
    }

    @Override
    public Flux<TelemetryResponse> getEventsByDevice(String deviceId, boolean onlyCritical) {
        return repository.findByDeviceId(deviceId)
                .map(this::mapToResponse)
                .filter(response -> !onlyCritical || Boolean.TRUE.equals(response.isCritical()))
                .onErrorResume(ex -> {
                   log.error("Error to consult events of device {}: {}", deviceId, ex.getMessage());
                   return Flux.empty();
                });
    }

    @Override
    public Mono<TelemetryResponse> getEventDetail(String deviceId, String timestamp) {
        return repository.findById(deviceId, timestamp)
                .map(this::mapToResponse)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Telemetry event not found")));
    }



    private TelemetryRequest enrichRequest(TelemetryRequest req) {
        String effectiveTimestamp = (req.timestamp() == null || req.timestamp().isBlank())
                ? Instant.now().toString()
                : req.timestamp();

        String effectiveStatus = (req.status() == null || req.status().isBlank())
                ? "NORMAL"
                : req.status();

        return TelemetryRequest.builder()
                .deviceId(req.deviceId())
                .timestamp(effectiveTimestamp)
                .temperature(req.temperature())
                .humidity(req.humidity())
                .status(effectiveStatus)
                .build();
    }

    private TelemetryEvent mapToEntity(TelemetryRequest req) {
        return TelemetryEvent.builder()
                .deviceId(req.deviceId())
                .timestamp(req.timestamp())
                .temperature(req.temperature())
                .humidity(req.humidity())
                .status(req.status())
                .build();
    }

    private TelemetryResponse mapToResponse(TelemetryEvent entity) {
        boolean isCritical = entity.getTemperature() > 80.0 || "CRITICAL".equalsIgnoreCase(entity.getStatus());

        return TelemetryResponse.builder()
                .deviceId(entity.getDeviceId())
                .timestamp(entity.getTimestamp())
                .temperature(entity.getTemperature())
                .humidity(entity.getHumidity())
                .status(entity.getStatus())
                .isCritical(isCritical)
                .build();
    }
}
