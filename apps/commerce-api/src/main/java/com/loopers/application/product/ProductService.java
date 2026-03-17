package com.loopers.application.product;

import com.loopers.application.product.command.ProductCreateCommand;
import com.loopers.application.product.command.ProductUpdateCommand;
import com.loopers.application.product.result.ProductResult;
import com.loopers.application.product.result.ProductWithLikeCountResult;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductCacheRepository;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductWithLikeCount;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCacheRepository productCacheRepository;

    @Transactional
    public ProductResult registerProduct(Long brandId, ProductCreateCommand command) {
        Product product = Product.builder()
                .brandId(brandId)
                .name(command.name())
                .price(command.price())
                .stock(command.stock())
                .build();

        Product saved = productRepository.save(product);
        productCacheRepository.evictAllProductsCache();
        return ProductResult.from(saved);
    }

    @Transactional(readOnly = true)
    public ProductResult getProduct(Long productId) {
        Product product = productCacheRepository.getProduct(productId)
                .orElseGet(() -> {
                    Product origin = productRepository.findByIdAndDeletedAtIsNull(productId)
                            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
                    productCacheRepository.putProduct(productId, origin);
                    return origin;
                });
        return ProductResult.from(product);
    }

    @Transactional(readOnly = true)
    public ProductWithLikeCountResult getProductWithLikeCount(Long productId) {
        ProductWithLikeCount productWithLikeCount = productCacheRepository.getProductWithLikeCount(productId)
                .orElseGet(() -> {
                    ProductWithLikeCount origin = productRepository.findWithLikeCountByIdAndDeletedAtIsNull(productId)
                            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
                    productCacheRepository.putProductWithLikeCount(productId, origin);
                    return origin;
                });
        return ProductWithLikeCountResult.from(productWithLikeCount);
    }

    @Transactional(readOnly = true)
    public Page<ProductResult> getProducts(Pageable pageable) {
        if (pageable.getPageNumber() == 0) {
            return productCacheRepository.getProducts(pageable)
                    .orElseGet(() -> {
                        Page<Product> origin = productRepository.findAllByDeletedAtIsNull(pageable);
                        productCacheRepository.putProducts(pageable, origin);
                        return origin;
                    })
                    .map(ProductResult::from);
        }

        return productRepository.findAllByDeletedAtIsNull(pageable)
                .map(ProductResult::from);
    }

    @Transactional(readOnly = true)
    public Page<ProductWithLikeCountResult> getProductsWithLikeCount(Pageable pageable) {
        if (pageable.getPageNumber() == 0) {
            return productCacheRepository.getProductsWithLikeCount(pageable)
                    .orElseGet(() -> {
                        Page<ProductWithLikeCount> origin = productRepository.findAllWithLikeCountByDeletedAtIsNull(pageable);
                        productCacheRepository.putProductsWithLikeCount(pageable, origin);
                        return origin;
                    })
                    .map(ProductWithLikeCountResult::from);
        }

        return productRepository.findAllWithLikeCountByDeletedAtIsNull(pageable)
                .map(ProductWithLikeCountResult::from);
    }

    @Transactional(readOnly = true)
    public Page<ProductWithLikeCountResult> getProductsWithLikeCount(Long brandId, Pageable pageable) {
        if (pageable.getPageNumber() == 0) {
            return productCacheRepository.getProductsWithLikeCount(brandId, pageable)
                    .orElseGet(() -> {
                        Page<ProductWithLikeCount> origin = productRepository.findAllWithLikeCountByBrandIdAndDeletedAtIsNull(brandId, pageable);
                        productCacheRepository.putProductsWithLikeCount(brandId, pageable, origin);
                        return origin;
                    })
                    .map(ProductWithLikeCountResult::from);
        }

        return productRepository.findAllWithLikeCountByBrandIdAndDeletedAtIsNull(brandId, pageable)
                .map(ProductWithLikeCountResult::from);
    }

    @Transactional(readOnly = true)
    public List<ProductResult> getProducts(Long brandId) {
        return productRepository.findAllByBrandIdAndDeletedAtIsNull(brandId).stream()
                .map(ProductResult::from)
                .toList();
    }

    @Transactional
    public ProductResult modifyProduct(Long productId, Long brandId, ProductUpdateCommand command) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));

        product.changeInfo(brandId, command.name(), command.price(), command.stock());
        productCacheRepository.evictProduct(productId);
        productCacheRepository.evictAllProductsCache();

        return ProductResult.from(product);
    }

    @Transactional
    public ProductResult deductStock(Long productId, int quantity) {
        Product product = productRepository.findByIdWithLockAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
        product.deductStock(quantity);
        productCacheRepository.evictProduct(productId);
        productCacheRepository.evictAllProductsCache();
        return ProductResult.from(product);
    }

    @Transactional
    public void restoreStock(Long productId, int quantity) {
        Product product = productRepository.findByIdWithLockAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
        product.restoreStock(quantity);
        productCacheRepository.evictProduct(productId);
        productCacheRepository.evictAllProductsCache();
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
        product.delete();
        productCacheRepository.evictProduct(productId);
        productCacheRepository.evictAllProductsCache();
    }

    @Transactional
    public void deleteProducts(Long brandId) {
        List<Product> products = productRepository.findAllByBrandId(brandId);
        products.forEach(product -> {
            product.delete();
            productCacheRepository.evictProduct(product.getId());
        });
        productCacheRepository.evictAllProductsCache();
    }
}