package com.loopers.application.order;

public record OrderItemCommand(
        Long productId,
        String productName,
        int productPrice,
        int quantity
) {}
