package com.loopers.application.brand;

import com.loopers.application.brand.command.BrandCreateCommand;
import com.loopers.application.brand.command.BrandUpdateCommand;
import com.loopers.application.brand.result.BrandResult;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandCacheRepository;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.brand.event.BrandDeletedEvent;
import com.loopers.domain.brand.event.BrandModifiedEvent;
import com.loopers.support.cache.CacheLockManager;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class BrandService {

	private final BrandRepository brandRepository;
	private final BrandCacheRepository brandCacheRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final CacheLockManager cacheLockManager;

	@Transactional
	public BrandResult registerBrand(BrandCreateCommand command) {
		Brand brand = Brand.builder()
				.name(command.name())
				.build();

		return BrandResult.from(brandRepository.save(brand));
	}

	@Transactional(readOnly = true)
	public BrandResult getBrand(Long brandId) {
		Brand brand = brandCacheRepository.getBrand(brandId)
				.orElseGet(() -> cacheLockManager.executeWithLock(
						"brand:" + brandId,
						() -> brandCacheRepository.getBrand(brandId),
						() -> {
							Brand origin = brandRepository.findByIdAndDeletedAtIsNull(brandId)
									.orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다."));
							brandCacheRepository.putBrand(brandId, origin);
							return origin;
						}
				));
		return BrandResult.from(brand);
	}

	@Transactional(readOnly = true)
	public Page<BrandResult> getBrands(Pageable pageable) {
		return brandRepository.findAllByDeletedAtIsNull(pageable)
				.map(BrandResult::from);
	}

	@Transactional(readOnly = true)
	public boolean existsBrand(Long brandId) {
		return brandRepository.existsByIdAndDeletedAtIsNull(brandId);
	}

	@Transactional
	public BrandResult modifyBrand(Long brandId, BrandUpdateCommand command) {
		Brand brand = brandRepository.findByIdAndDeletedAtIsNull(brandId)
				.orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다."));

		brand.changeName(command.name());
		eventPublisher.publishEvent(new BrandModifiedEvent(brandId));
		return BrandResult.from(brand);
	}

	@Transactional
	public void deleteBrand(Long brandId) {
		Brand brand = brandRepository.findById(brandId)
				.orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다."));

		brand.delete();
		eventPublisher.publishEvent(new BrandDeletedEvent(brandId));
	}
}
