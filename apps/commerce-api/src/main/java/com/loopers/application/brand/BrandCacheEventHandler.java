package com.loopers.application.brand;

import com.loopers.domain.brand.BrandCacheRepository;
import com.loopers.domain.brand.event.BrandDeletedEvent;
import com.loopers.domain.brand.event.BrandModifiedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class BrandCacheEventHandler {

	private final BrandCacheRepository brandCacheRepository;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleBrandModified(BrandModifiedEvent event) {
		brandCacheRepository.evictBrand(event.brandId());
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleBrandDeleted(BrandDeletedEvent event) {
		brandCacheRepository.evictBrand(event.brandId());
	}
}
