package com.bancolombia.challenge.telemetry.repository;


import com.bancolombia.challenge.telemetry.model.TransactionTelemetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
public class TelemetryRepository {
    private final DynamoDbAsyncTable<TransactionTelemetry> table;

    public TelemetryRepository (DynamoDbEnhancedAsyncClient enhancedAsyncClient,
                                @Value("${dynamodb.table-name:telemetry_events}") String tableName) {
        this.table = enhancedAsyncClient.table(tableName, TableSchema.fromBean(TransactionTelemetry.class));
    }

    public Mono<TransactionTelemetry> save(TransactionTelemetry event) {
        return Mono.fromFuture(table.putItem(event))
                .then(Mono.just(event));
    }


    public Flux<TransactionTelemetry> findByAccountId(String accountId) {
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(accountId).build());

        return Flux.from(table.query(queryConditional))
                .flatMapIterable(Page::items);
    }

    public Mono<TransactionTelemetry> findById(String accountId, String timestamp) {
        Key key = Key.builder()
                .partitionValue(accountId)
                .sortValue(timestamp)
                .build();

        return Mono.fromFuture(() -> table.getItem(key));
    }


}
