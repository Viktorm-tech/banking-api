package com.morski.banking.dto;

import com.morski.banking.entity.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        BigDecimal amount,
        TransactionType type,
        UUID relatedAccountId,
        String description,
        LocalDateTime timestamp
) {}
