package com.bancolombia.challenge.telemetry.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class TransactionTelemetry {

    private String accountId;
    private String timestamp;
    private String transactionId;
    private Double amount;
    private Double calculatedFee;
    private String channel;          //  "WEB", "ATM", "MOBILE"
    private String paymentProvider;  // "VISA", "MASTERCARD", "PSE"
    private Boolean isHighRisk;
    private String status;           // "NORMAL", "FLAGGED", "CRITICAL"

    @DynamoDbPartitionKey
    public String getAccountId() {
        return this.accountId;
    }

    @DynamoDbSortKey
    public String getTimestamp() {
        return this.timestamp;
    }
}