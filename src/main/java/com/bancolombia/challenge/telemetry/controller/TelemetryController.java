package com.bancolombia.challenge.telemetry.controller;

import com.bancolombia.challenge.telemetry.dto.TelemetryRequest;
import com.bancolombia.challenge.telemetry.dto.TelemetryResponse;
import com.bancolombia.challenge.telemetry.service.ITelemetryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/telemetry")
@RequiredArgsConstructor
public class TelemetryController {
    private final ITelemetryService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TelemetryResponse> ingestTelemetry(@Valid @RequestBody TelemetryRequest request) {
        return service.processAndSave(request);
    }

    @GetMapping("/device/{deviceId}")
    public Flux<TelemetryResponse> getByDevice(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "false") boolean onlyCritical) {
        return  service.getEventsByDevice(deviceId, onlyCritical);
    }

    @GetMapping("/device/{deviceId}/{timestamp}")
    public Mono<TelemetryResponse> getDetail(
            @PathVariable String deviceId,
            @PathVariable String timestamp) {
        return service.getEventDetail(deviceId, timestamp);
    }
}
