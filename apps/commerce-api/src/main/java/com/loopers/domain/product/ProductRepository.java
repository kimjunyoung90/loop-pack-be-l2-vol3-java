package com.loopers.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Page<Product> findAll(Pageable pageable);

    List<Product> findAllByBrandId(Long brandId);

    Optional<Product> findById(Long productId);

    Product save(Product product);

    boolean existsById(Long productId);
}
