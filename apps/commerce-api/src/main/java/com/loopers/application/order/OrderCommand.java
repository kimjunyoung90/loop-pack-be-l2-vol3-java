package com.loopers.application.order;

import java.util.List;

public class OrderCommand {

    public record Create(
            Long userId,
            List<CreateItem> orderItems
    ) {
    }

    public record CreateItem(
            Long productId,
            int quantity
    ) {
    }

    public record Item(
            Long productId,
            String productName,
            int productPrice,
            int quantity
    ) {
    }
}
