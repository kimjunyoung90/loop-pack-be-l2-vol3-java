package com.loopers.application.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class BrandService {

    private final BrandRepository brandRepository;

    @Transactional
    public BrandInfo createBrand(BrandCommand.Create command) {
        Brand brand = Brand.builder()
                .name(command.name())
                .build();

        return BrandInfo.from(brandRepository.save(brand));
    }

    @Transactional(readOnly = true)
    public void validateBrandExists(Long brandId) {
        if (!brandRepository.existsById(brandId)) {
            throw new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다.");
        }
    }

    @Transactional(readOnly = true)
    public Brand findBrand(Long brandId) {
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public BrandInfo getBrand(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다."));

        return BrandInfo.from(brand);
    }

    @Transactional(readOnly = true)
    public Page<BrandInfo> getBrands(Pageable pageable) {
        return brandRepository.findAll(pageable)
                .map(BrandInfo::from);
    }

    @Transactional
    public BrandInfo updateBrand(Long brandId, BrandCommand.Update command) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다."));

        brand.update(command.name());

        return BrandInfo.from(brand);
    }

    @Transactional
    public void deleteBrand(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다."));

        brand.delete();
    }
}
