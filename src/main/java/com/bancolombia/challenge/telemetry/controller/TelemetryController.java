package com.bancolombia.challenge.telemetry.controller;

import com.bancolombia.challenge.telemetry.dto.TelemetryResponse;
import com.bancolombia.challenge.telemetry.dto.TransactionTelemetryRequest;
import com.bancolombia.challenge.telemetry.service.ITelemetryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Telemetry", description = "Telemetría transaccional")
@RestController
@RequestMapping("/api/v1/telemetry")
@RequiredArgsConstructor
public class TelemetryController {

    private final ITelemetryService service;

    @Operation(summary = "Enter telemetry", description = "Processes and stores the telemetry of a transaction")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Telemetry successfully saved"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TelemetryResponse> ingestTelemetry(@Valid @RequestBody TransactionTelemetryRequest request) {
        return service.processAndSave(request);
    }

    @Operation(summary = "Get events per account", description = "List telemetry events for an account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of telemetry events")
    })
    @GetMapping("/account/{accountId}")
    public Flux<TelemetryResponse> getByAccount(
            @PathVariable String accountId,
            @RequestParam(defaultValue = "false") boolean onlyHighRisk,
        @RequestParam(defaultValue = "20") int limit,
        @RequestParam(required = false) String lastKey) {
        return service.getEventsByAccount(accountId, onlyHighRisk);
    }

    @Operation(summary = "Get event detail", description = "View an event per account and timestamp")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event found"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @GetMapping("/account/{accountId}/{timestamp}")
    public Mono<TelemetryResponse> getDetail(
            @PathVariable String accountId,
            @PathVariable String timestamp) {
        return service.getEventDetail(accountId, timestamp);
    }
}