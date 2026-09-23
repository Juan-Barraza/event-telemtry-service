package com.bancolombia.challenge.telemetry.controller;

import com.bancolombia.challenge.telemetry.dto.TransactionTelemetryRequest;
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

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TelemetryResponse> ingestTelemetry(@Valid @RequestBody TransactionTelemetryRequest request) {
        return service.processAndSave(request);
    }

    @GetMapping("/account/{accountId}")
    public Flux<TelemetryResponse> getByAccount(
            @PathVariable String accountId,
            @RequestParam(defaultValue = "false") boolean onlyHighRisk) {
        return service.getEventsByAccount(accountId, onlyHighRisk);
    }

    @GetMapping("/account/{accountId}/{timestamp}")
    public Mono<TelemetryResponse> getDetail(
            @PathVariable String accountId,
            @PathVariable String timestamp) {
        return service.getEventDetail(accountId, timestamp);
    }
}
