package com.loopers.application.product;

import com.loopers.application.product.command.ProductCreateCommand;
import com.loopers.application.product.command.ProductUpdateCommand;
import com.loopers.application.product.result.ProductResult;
import com.loopers.domain.product.*;
import com.loopers.domain.product.event.ProductDeletedEvent;
import com.loopers.domain.product.event.ProductModifiedEvent;
import com.loopers.domain.product.event.ProductStockChangedEvent;
import com.loopers.domain.product.event.ProductViewedEvent;
import com.loopers.support.cache.CacheLockManager;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCacheRepository productCacheRepository;
    private final CacheLockManager cacheLockManager;
    private final ApplicationEventPublisher eventPublisher;

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

    @Transactional
    public ProductResult getProduct(Long productId) {
        Product product = productCacheRepository.getProduct(productId)
                .orElseGet(() -> cacheLockManager.executeWithLock(
                        "product:" + productId,
                        () -> productCacheRepository.getProduct(productId),
                        () -> {
                            Product origin = productRepository.findByIdAndDeletedAtIsNull(productId)
                                    .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
                            productCacheRepository.putProduct(productId, origin);
                            return origin;
                        }
                ));
        eventPublisher.publishEvent(new ProductViewedEvent(productId));
        return ProductResult.from(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResult> getProducts(Pageable pageable) {
        if (pageable.getPageNumber() == 0) {
            Optional<CachedProductIds> productIds = productCacheRepository.getProductIds(pageable);
            if (productIds.isEmpty()) {
                return fetchAndCacheProducts(null, pageable).map(ProductResult::from);
            }
            List<Product> cached = productCacheRepository.multiGetProducts(productIds.get().productIds());
            if (cached.size() == productIds.get().productIds().size()) {
                return new PageImpl<>(cached, pageable, productIds.get().totalElements())
                        .map(ProductResult::from);
            }
            return fetchAndCacheProducts(null, pageable).map(ProductResult::from);
        }
        return productRepository.findAllByDeletedAtIsNull(pageable).map(ProductResult::from);
    }

    @Transactional(readOnly = true)
    public Page<ProductResult> getProducts(Long brandId, Pageable pageable) {
        if (pageable.getPageNumber() == 0) {
            Optional<CachedProductIds> productIds = productCacheRepository.getProductIds(brandId, pageable);
            if (productIds.isEmpty()) {
                return fetchAndCacheProducts(brandId, pageable).map(ProductResult::from);
            }
            List<Product> cached = productCacheRepository.multiGetProducts(productIds.get().productIds());
            if (cached.size() == productIds.get().productIds().size()) {
                return new PageImpl<>(cached, pageable, productIds.get().totalElements())
                        .map(ProductResult::from);
            }
            return fetchAndCacheProducts(brandId, pageable).map(ProductResult::from);
        }
        return productRepository.findAllByBrandIdAndDeletedAtIsNull(brandId, pageable)
                .map(ProductResult::from);
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
        eventPublisher.publishEvent(new ProductModifiedEvent(productId));

        return ProductResult.from(product);
    }

    @Transactional
    public ProductResult deductStock(Long productId, int quantity) {
        Product product = productRepository.findByIdWithLockAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
        product.deductStock(quantity);
        eventPublisher.publishEvent(new ProductStockChangedEvent(productId));
        return ProductResult.from(product);
    }

    @Transactional
    public void restoreStock(Long productId, int quantity) {
        Product product = productRepository.findByIdWithLockAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
        product.restoreStock(quantity);
        eventPublisher.publishEvent(new ProductStockChangedEvent(productId));
    }

    @Transactional
    public void incrementLikeCount(Long productId) {
        Product product = productRepository.findByIdWithLockAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
        product.incrementLikeCount();
    }

    @Transactional
    public void decrementLikeCount(Long productId) {
        Product product = productRepository.findByIdWithLockAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
        product.decrementLikeCount();
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
        product.delete();
        eventPublisher.publishEvent(ProductDeletedEvent.of(productId));
    }

    @Transactional
    public void deleteProducts(Long brandId) {
        List<Product> products = productRepository.findAllByBrandId(brandId);
        List<Long> productIds = products.stream().map(Product::getId).toList();
        products.forEach(Product::delete);
        eventPublisher.publishEvent(new ProductDeletedEvent(productIds));
    }

    private Page<Product> fetchAndCacheProducts(Long brandId, Pageable pageable) {
        Page<Product> page = (brandId != null)
                ? productRepository.findAllByBrandIdAndDeletedAtIsNull(brandId, pageable)
                : productRepository.findAllByDeletedAtIsNull(pageable);

        List<Long> ids = page.getContent().stream()
                .map(Product::getId)
                .toList();

        if (brandId != null) {
            productCacheRepository.putProductIds(brandId, pageable, ids, page.getTotalElements());
        } else {
            productCacheRepository.putProductIds(pageable, ids, page.getTotalElements());
        }

        page.forEach(p -> productCacheRepository.putProduct(p.getId(), p));

        return page;
    }
}
