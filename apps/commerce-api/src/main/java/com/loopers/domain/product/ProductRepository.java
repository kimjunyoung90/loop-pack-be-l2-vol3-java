package com.loopers.domain.product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    List<Product> findAllByDeletedAtIsNull();

    Optional<Product> findById(Long productId);

    Product save(Product product);
}
