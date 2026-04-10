package com.morski.banking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank String customerId,
        @NotNull @Min(0) BigDecimal initialBalance,
        @NotBlank @Size(min = 3, max = 3) String currency
) {}
