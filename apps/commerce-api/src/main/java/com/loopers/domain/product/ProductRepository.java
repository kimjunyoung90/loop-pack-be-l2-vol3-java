package com.loopers.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductRepository {
    Page<Product> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<Product> findById(Long productId);

    Product save(Product product);
}
