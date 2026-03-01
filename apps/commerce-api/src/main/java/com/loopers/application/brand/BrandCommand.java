package com.loopers.application.brand;

public class BrandCommand {

    public record Create(String name) {
    }

    public record Update(String name) {
    }
}
