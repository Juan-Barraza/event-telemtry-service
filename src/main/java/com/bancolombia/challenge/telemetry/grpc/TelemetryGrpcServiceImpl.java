package com.bancolombia.challenge.telemetry.grpc;


import com.bancolombia.challenge.telemetry.dto.TelemetryResponse;
import com.bancolombia.challenge.telemetry.dto.TransactionTelemetryRequest;
import com.bancolombia.challenge.telemetry.service.ITelemetryService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class TelemetryGrpcServiceImpl extends  TelemetryGrpcServiceGrpc.TelemetryGrpcServiceImplBase{
    private final ITelemetryService telemetryService;

    @Override
    public void evaluateTransactionRisk(TransactionGrpcRequest request, StreamObserver<TransactionGrpcResponse> responseObserver) {
        TransactionTelemetryRequest dtoRequest = TransactionTelemetryRequest.builder()
                .transactionId(request.getTransactionId())
                .accountId(request.getAccountId())
                .timestamp(request.getTimestamp())
                .amount(request.getAmount())
                .channel(request.getChannel())
                .paymentProvider(request.getPaymentProvider())
                .status(request.getStatus())
                .build();

        telemetryService.processAndSave(dtoRequest)
                .subscribe(
                        response -> {
                            TransactionGrpcResponse grpcResponse = mapToGrpcResponse(response);
                            responseObserver.onNext(grpcResponse);
                            responseObserver.onCompleted();
                        },
                        error -> {
                            log.error("Error on gRPC evaluateTransactionRisk: {}", error.getMessage());
                            responseObserver.onError(error);
                        }
                );
    }

    @Override
    public void streamTransactionsByAccount(AccountQuery request, StreamObserver<TransactionGrpcResponse> responseObserver) {
        telemetryService.getEventsByAccount(request.getAccountId(), request.getHighRiskOnly())
                .subscribe(
                        response -> responseObserver.onNext(mapToGrpcResponse(response)),
                        error -> {
                            log.error("Error on gRPC streamTransactionsByAccount: {}", error.getMessage());
                            responseObserver.onError(error);
                        },
                        responseObserver::onCompleted
                );
    }

    private TransactionGrpcResponse mapToGrpcResponse(TelemetryResponse dto) {
        return TransactionGrpcResponse.newBuilder()
                .setTransactionId(dto.transactionId() != null ? dto.transactionId() : "")
                .setAccountId(dto.accountId() != null ? dto.accountId() : "")
                .setTimestamp(dto.timestamp() != null ? dto.timestamp() : "")
                .setAmount(dto.amount() != null ? dto.amount() : 0.0)
                .setCalculatedFee(dto.calculatedFee() != null ? dto.calculatedFee() : 0.0)
                .setChannel(dto.channel() != null ? dto.channel() : "")
                .setPaymentProvider(dto.paymentProvider() != null ? dto.paymentProvider() : "")
                .setStatus(dto.status() != null ? dto.status() : "")
                .setIsHighRisk(Boolean.TRUE.equals(dto.isHighRisk()))
                .build();
    }
}
