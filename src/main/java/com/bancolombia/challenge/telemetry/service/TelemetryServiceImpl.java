package com.bancolombia.challenge.telemetry.service;

import com.bancolombia.challenge.telemetry.dto.TelemetryResponse;
import com.bancolombia.challenge.telemetry.dto.TransactionTelemetryRequest;
import com.bancolombia.challenge.telemetry.model.TransactionTelemetry;
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
public class TelemetryServiceImpl implements ITelemetryService {

    private final TelemetryRepository repository;

    @Override
    public Mono<TelemetryResponse> processAndSave(TransactionTelemetryRequest request) {
        return Mono.just(request)
                .map(this::enrichRequest)
                .map(this::mapToEntity)
                .flatMap(repository::save)
                .retry(2)
                .map(this::mapToResponse)
                .doOnSuccess(saved -> log.info("Transaction telemetry saved for account: {}, transactionId: {}", saved.accountId(), saved.transactionId()))
                .onErrorResume(ex -> {
                    log.error("Error processing telemetry for account {}: {}", request.accountId(), ex.getMessage());
                    return Mono.error(new RuntimeException("Error processing reactive transaction telemetry", ex));
                });
    }

    @Override
    public Flux<TelemetryResponse> getEventsByAccount(String accountId, boolean onlyHighRisk) {
        return repository.findByAccountId(accountId)
                .map(this::mapToResponse)
                .filter(response -> !onlyHighRisk || Boolean.TRUE.equals(response.isHighRisk()))
                .onErrorResume(ex -> {
                    log.error("Error consulting transaction telemetry for account {}: {}", accountId, ex.getMessage());
                    return Flux.empty();
                });
    }

    @Override
    public Mono<TelemetryResponse> getEventDetail(String accountId, String timestamp) {
        return repository.findById(accountId, timestamp)
                .map(this::mapToResponse)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Transaction telemetry event not found")));
    }

    private TransactionTelemetryRequest enrichRequest(TransactionTelemetryRequest req) {
        String effectiveTimestamp = (req.timestamp() == null || req.timestamp().isBlank())
                ? Instant.now().toString()
                : req.timestamp();

        String effectiveStatus = (req.status() == null || req.status().isBlank())
                ? "NORMAL"
                : req.status();

        return TransactionTelemetryRequest.builder()
                .transactionId(req.transactionId())
                .accountId(req.accountId())
                .timestamp(effectiveTimestamp)
                .amount(req.amount())
                .channel(req.channel())
                .paymentProvider(req.paymentProvider())
                .status(effectiveStatus)
                .build();
    }

    private TransactionTelemetry mapToEntity(TransactionTelemetryRequest req) {
        // Cálculo temporal de regla de comisión y riesgo (se sustituirá con Factory y Strategy)
        double fee = req.amount() * 0.015; // 1.5% tarifa base
        boolean highRisk = req.amount() > 10000000.0 || "CRITICAL".equalsIgnoreCase(req.status());

        return TransactionTelemetry.builder()
                .transactionId(req.transactionId())
                .accountId(req.accountId())
                .timestamp(req.timestamp())
                .amount(req.amount())
                .calculatedFee(fee)
                .channel(req.channel())
                .paymentProvider(req.paymentProvider())
                .isHighRisk(highRisk)
                .status(req.status())
                .build();
    }

    private TelemetryResponse mapToResponse(TransactionTelemetry entity) {
        return TelemetryResponse.builder()
                .transactionId(entity.getTransactionId())
                .accountId(entity.getAccountId())
                .timestamp(entity.getTimestamp())
                .amount(entity.getAmount())
                .calculatedFee(entity.getCalculatedFee())
                .channel(entity.getChannel())
                .paymentProvider(entity.getPaymentProvider())
                .status(entity.getStatus())
                .isHighRisk(entity.getIsHighRisk())
                .build();
    }
}