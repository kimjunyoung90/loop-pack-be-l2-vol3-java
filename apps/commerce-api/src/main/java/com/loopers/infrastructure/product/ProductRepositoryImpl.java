package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ProductRepositoryImpl implements ProductRepository {
    private final ProductJpaRepository productJpaRepository;

    @Override
    public Page<Product> findAllByDeletedAtIsNull(Pageable pageable) {
        return productJpaRepository.findAllByDeletedAtIsNull(pageable);
    }

    @Override
    public List<Product> findAllByBrandId(Long brandId) {
        return productJpaRepository.findAllByBrandId(brandId);
    }

    @Override
    public List<Product> findAllByBrandIdAndDeletedAtIsNull(Long brandId) {
        return productJpaRepository.findAllByBrandIdAndDeletedAtIsNull(brandId);
    }

    @Override
    public Optional<Product> findById(Long productId) {
        return productJpaRepository.findById(productId);
    }

    @Override
    public Optional<Product> findByIdAndDeletedAtIsNull(Long productId) {
        return productJpaRepository.findByIdAndDeletedAtIsNull(productId);
    }

    @Override
    public Optional<Product> findByIdWithLockAndDeletedAtIsNull(Long productId) {
        return productJpaRepository.findByIdWithLockAndDeletedAtIsNull(productId);
    }

    @Override
    public int incrementLikeCount(Long productId) {
        return productJpaRepository.incrementLikeCount(productId);
    }

    @Override
    public int decrementLikeCount(Long productId) {
        return productJpaRepository.decrementLikeCount(productId);
    }

    @Override
    public Page<Product> findAllByBrandIdAndDeletedAtIsNull(Long brandId, Pageable pageable) {
        return productJpaRepository.findAllByBrandIdAndDeletedAtIsNull(brandId, pageable);
    }

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(product);
    }
}
