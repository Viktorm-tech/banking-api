package com.morski.banking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record DepositWithdrawRequest(
        @NotNull @Positive BigDecimal amount
) {}
