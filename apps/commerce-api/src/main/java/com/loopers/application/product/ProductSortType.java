package com.loopers.application.product;

import org.springframework.data.domain.Sort;

public enum ProductSortType {

    LATEST(Sort.by(Sort.Direction.DESC, "createdAt")),
    PRICE_ASC(Sort.by(Sort.Direction.ASC, "price")),
    PRICE_DESC(Sort.by(Sort.Direction.DESC, "price")),
    LIKES_DESC(Sort.by(Sort.Direction.DESC, "likeCount"));

    private final Sort sort;

    ProductSortType(Sort sort) {
        this.sort = sort;
    }

    public Sort getSort() {
        return sort;
    }
}