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
public class TelemetryEvent {
    private  String deviceId;
    private String timestamp;
    private Double temperature;
    private Double humidity;
    private String status;

    @DynamoDbPartitionKey
    public String getDeviceId() {
        return this.deviceId;
    }

    @DynamoDbSortKey
    public String getTimestamp() {
        return this.timestamp;
    }
}
