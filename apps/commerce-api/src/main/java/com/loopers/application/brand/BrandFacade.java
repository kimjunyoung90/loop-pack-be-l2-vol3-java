package com.loopers.application.brand;

import com.loopers.application.like.LikeService;
import com.loopers.application.product.ProductService;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;
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
        Brand brand = brandService.findBrand(brandId);
        List<Product> products = productService.findProductsByBrand(brand);
        products.forEach(product -> likeService.deleteLikesByProductId(product.getId()));
        productService.deleteProductsByBrand(brand);
        brandService.deleteBrand(brandId);
    }
}
