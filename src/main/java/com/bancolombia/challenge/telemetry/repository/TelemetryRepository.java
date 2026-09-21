package com.bancolombia.challenge.telemetry.repository;


import com.bancolombia.challenge.telemetry.model.TelemetryEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
public class TelemetryRepository {
    private final DynamoDbAsyncTable<TelemetryEvent> table;

    public TelemetryRepository (DynamoDbEnhancedAsyncClient enhancedAsyncClient,
                                @Value("${dynamodb.table-name:telemetry_events}") String tableName) {
        this.table = enhancedAsyncClient.table(tableName, TableSchema.fromBean(TelemetryEvent.class));
    }

    public Mono<TelemetryEvent> save(TelemetryEvent event) {
        return Mono.fromFuture(table.putItem(event))
                .then(Mono.just(event));
    }

    public Mono<TelemetryEvent> findById(String deviceId, String timestamp) {
        Key key = Key.builder()
                .partitionValue(deviceId)
                .sortValue(timestamp)
                .build();

        return Mono.fromFuture(table.getItem(key));
    }

    public Flux<TelemetryEvent> findByDeviceId(String deviceId) {
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(deviceId).build());

        return Flux.from(table.query(queryConditional).items());
    }
}
