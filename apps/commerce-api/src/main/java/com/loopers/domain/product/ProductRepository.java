package com.loopers.domain.product;

import com.loopers.domain.brand.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Page<Product> findAll(Pageable pageable);

    List<Product> findAllByBrand(Brand brand);

    Optional<Product> findById(Long productId);

    Product save(Product product);
}
