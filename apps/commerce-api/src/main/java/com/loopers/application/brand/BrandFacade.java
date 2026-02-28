package com.loopers.application.brand;

import com.loopers.application.like.LikeService;
import com.loopers.application.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class BrandFacade {

    private final BrandService brandService;
    private final ProductService productService;
    private final LikeService likeService;

    @Transactional
    public void deleteBrand(Long brandId) {
        brandService.validateBrandExists(brandId);
        List<Long> productIds = productService.getProductIdsByBrandId(brandId);
        productIds.forEach(likeService::deleteLikesByProductId);
        productService.deleteProductsByBrandId(brandId);
        brandService.deleteBrand(brandId);
    }
}
