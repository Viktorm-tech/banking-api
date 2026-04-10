package com.morski.banking.service;

import com.morski.banking.event.AccountEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class KafkaEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);
    private final KafkaTemplate<String, AccountEvent> kafkaTemplate;
    private static final String TOPIC = "account-events";

    @Autowired
    public KafkaEventPublisher(KafkaTemplate<String, AccountEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishAccountCreated(UUID accountId, Object data) {
        AccountEvent event = new AccountEvent("ACCOUNT_CREATED", accountId, null, data, LocalDateTime.now());
        kafkaTemplate.send(TOPIC, accountId.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Event published: {}", event);
                    } else {
                        log.error("Failed to publish event: {}", event, ex);
                    }
                });
    }

    public void publishTransferCompleted(UUID fromAccountId, UUID toAccountId, Object data) {
        AccountEvent event = new AccountEvent("TRANSFER_COMPLETED", fromAccountId, toAccountId, data, LocalDateTime.now());
        kafkaTemplate.send(TOPIC, fromAccountId.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Event published: {}", event);
                    } else {
                        log.error("Failed to publish event: {}", event, ex);
                    }
                });
    }
}
