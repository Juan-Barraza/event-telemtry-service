package com.bancolombia.challenge.telemetry.pattern.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class AuditNotificationObserver {

    @Async
    @EventListener
    public void onHighRiskTransactionDetected(HighRiskTransactionEvent event) {
        log.warn("[OBSERVER - AUDIT - ALERT] Transaction with identify height risk: TXId={}, Account={}, Amount='{}', Channel={}, Timestamp={}",
                event.transactionId(), event.accountId(), event.amount(), event.channel(), event.timestamp());
    }
}
