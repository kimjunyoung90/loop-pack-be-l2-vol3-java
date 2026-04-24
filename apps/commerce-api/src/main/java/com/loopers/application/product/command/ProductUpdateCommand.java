package com.loopers.application.product.command;

import com.loopers.domain.common.Money;

public record ProductUpdateCommand(
        Long brandId,
        String name,
        Money price,
        int stock
) {
}
