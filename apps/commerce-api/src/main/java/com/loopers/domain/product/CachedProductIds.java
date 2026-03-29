package com.loopers.domain.product;

import java.util.List;

public record CachedProductIds(List<Long> productIds, long totalElements) {
}
