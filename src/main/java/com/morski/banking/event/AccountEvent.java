package com.morski.banking.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccountEvent(
        String eventType,
        UUID accountId,
        UUID relatedAccountId,
        Object data,
        LocalDateTime timestamp
) {}
