package com.loopers.application.brand;

import com.loopers.application.product.ProductService;
import com.loopers.domain.brand.Brand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class BrandFacade {

    private final BrandService brandService;
    private final ProductService productService;

    @Transactional
    public void deleteBrand(Long brandId) {
        Brand brand = brandService.findBrand(brandId);
        productService.deleteProductsByBrand(brand);
        brandService.deleteBrand(brandId);
    }
}
