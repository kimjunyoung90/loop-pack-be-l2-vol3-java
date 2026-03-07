package com.loopers.application.product.command;

public record ProductUpdateCommand(
        Long brandId,
        String name,
        int price,
        int stock
) {
}
