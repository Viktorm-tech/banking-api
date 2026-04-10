package com.morski.banking.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String customerId,
        BigDecimal balance,
        String currency,
        String status
) {}
