package com.loopers.application.product;

import com.loopers.domain.product.ProductCacheRepository;
import com.loopers.domain.product.event.ProductDeletedEvent;
import com.loopers.domain.product.event.ProductModifiedEvent;
import com.loopers.domain.product.event.ProductStockChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class ProductCacheEventHandler {

    private final ProductCacheRepository productCacheRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductModified(ProductModifiedEvent event) {
        productCacheRepository.evictProduct(event.productId());
        productCacheRepository.evictAllProductsCache();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductDeleted(ProductDeletedEvent event) {
        event.productIds().forEach(productCacheRepository::evictProduct);
        productCacheRepository.evictAllProductsCache();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductStockChanged(ProductStockChangedEvent event) {
        productCacheRepository.evictProduct(event.productId());
        productCacheRepository.evictAllProductsCache();
    }
}
