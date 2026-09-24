package com.bancolombia.challenge.telemetry.service;

import com.bancolombia.challenge.telemetry.dto.TelemetryResponse;
import com.bancolombia.challenge.telemetry.dto.TransactionTelemetryRequest;
import com.bancolombia.challenge.telemetry.model.TransactionTelemetry;
import com.bancolombia.challenge.telemetry.pattern.factory.FeeStrategyFactory;
import com.bancolombia.challenge.telemetry.pattern.observer.HighRiskTransactionEvent;
import com.bancolombia.challenge.telemetry.pattern.strategy.FeeCalculationStrategy;
import com.bancolombia.challenge.telemetry.repository.TelemetryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelemetryServiceImplTest {

    @Mock
    private TelemetryRepository repository;

    @Mock
    private FeeStrategyFactory feeStrategyFactory;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private FeeCalculationStrategy feeStrategy;

    @InjectMocks
    private TelemetryServiceImpl telemetryService;

    @Test
    @DisplayName("Should process transaction as LOW RISK when amount is below threshold")
    void processAndSave_ShouldReturnLowRisk_WhenAmountIsLow() {
        
        TransactionTelemetryRequest request = TransactionTelemetryRequest.builder()
                .transactionId("tx-12345")
                .accountId("4380759965")
                .amount(50000.00)
                .channel("APP")
                .paymentProvider("BANCOLOMBIA")
                .build();

        when(feeStrategyFactory.getStrategy("APP")).thenReturn(feeStrategy);
        when(feeStrategy.calculateFee(50000.00)).thenReturn(300.00);

        when(repository.save(any(TransactionTelemetry.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act
        Mono<TelemetryResponse> result = telemetryService.processAndSave(request);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.transactionId().equals("tx-12345") &&
                        response.calculatedFee() == 300.00 &&
                        Boolean.FALSE.equals(response.isHighRisk()) &&
                        "NORMAL".equals(response.status())
                )
                .verifyComplete();

        verify(repository, times(1)).save(any(TransactionTelemetry.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Should process transaction as HIGH RISK and publish event when amount exceeds 10M")
    void processAndSave_ShouldReturnHighRiskAndPublishEvent_WhenAmountIsHigh() {
        
        TransactionTelemetryRequest request = TransactionTelemetryRequest.builder()
                .transactionId("tx-99999")
                .accountId("4380759965")
                .amount(12000000.00)
                .channel("APP")
                .paymentProvider("NEQUI")
                .build();

        when(feeStrategyFactory.getStrategy("APP")).thenReturn(feeStrategy);
        when(feeStrategy.calculateFee(12000000.00)).thenReturn(24000.00);

        when(repository.save(any(TransactionTelemetry.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Act
        Mono<TelemetryResponse> result = telemetryService.processAndSave(request);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.transactionId().equals("tx-99999") &&
                        response.calculatedFee() == 24000.00 &&
                        Boolean.TRUE.equals(response.isHighRisk())
                )
                .verifyComplete();

        verify(repository, times(1)).save(any(TransactionTelemetry.class));
        verify(eventPublisher, times(1)).publishEvent(any(HighRiskTransactionEvent.class));
    }

    @Test
    @DisplayName("Should handle repository errors and wrap in RuntimeException")
    void processAndSave_ShouldHandleRepositoryError() {
        
        TransactionTelemetryRequest request = TransactionTelemetryRequest.builder()
                .transactionId("tx-error-1")
                .accountId("4380759965")
                .amount(100000.00)
                .channel("WEB")
                .paymentProvider("NU")
                .build();

        when(feeStrategyFactory.getStrategy("WEB")).thenReturn(feeStrategy);
        when(feeStrategy.calculateFee(100000.00)).thenReturn(0.0);

        when(repository.save(any(TransactionTelemetry.class)))
                .thenReturn(Mono.error(new RuntimeException("DynamoDB connection timeout")));

        // Act
        Mono<TelemetryResponse> result = telemetryService.processAndSave(request);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                        throwable.getMessage().contains("Error processing reactive transaction telemetry")
                )
                .verify();
    }

    @Test
    @DisplayName("Should filter high risk events when onlyHighRisk parameter is true")
    void getEventsByAccount_ShouldFilterHighRiskEvents() {

        String accountId = "4380759965";

        TransactionTelemetry lowRiskTx = TransactionTelemetry.builder()
                .transactionId("tx-1")
                .accountId(accountId)
                .amount(1000.0)
                .isHighRisk(false)
                .build();

        TransactionTelemetry highRiskTx = TransactionTelemetry.builder()
                .transactionId("tx-2")
                .accountId(accountId)
                .amount(15000000.0)
                .isHighRisk(true)
                .build();

        when(repository.findByAccountId(accountId)).thenReturn(Flux.just(lowRiskTx, highRiskTx));

        // Act
        Flux<TelemetryResponse> result = telemetryService.getEventsByAccount(accountId, true);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.transactionId().equals("tx-2") && Boolean.TRUE.equals(response.isHighRisk()))
                .verifyComplete();
    }
}