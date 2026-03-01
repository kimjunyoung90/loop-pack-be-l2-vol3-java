package com.loopers.application.product;

public class ProductCommand {

    public record Create(
            Long brandId,
            String name,
            int price,
            int stock
    ) {
    }

    public record Update(
            Long brandId,
            String name,
            int price,
            int stock
    ) {
    }
}
