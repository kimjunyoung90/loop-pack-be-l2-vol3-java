package com.loopers.application.product.command;

import com.loopers.domain.common.Money;

public record ProductCreateCommand(
        Long brandId,
        String name,
        Money price,
        int stock
) {
}
