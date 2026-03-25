package com.loopers.interfaces.api.payment.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentCreateRequest(
        @NotNull Long orderId,
        @NotNull String cardType,
        @NotBlank String cardNo,
        @NotNull @Min(1) Long amount
) {
}
