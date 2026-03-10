package com.loopers.application.brand;

import com.loopers.application.like.LikeService;
import com.loopers.application.product.ProductService;
import com.loopers.application.product.result.ProductResult;
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
        List<ProductResult> products = productService.getProducts(brandId);
        products.forEach(product -> likeService.deleteLikes(product.id()));
		productService.deleteProducts(brandId);
		brandService.deleteBrand(brandId);
	}
}
