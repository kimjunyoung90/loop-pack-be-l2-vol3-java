package com.loopers.application.product;

import com.loopers.application.brand.BrandService;
import com.loopers.application.like.LikeService;
import com.loopers.domain.brand.Brand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class ProductFacade {

    private final ProductService productService;
    private final BrandService brandService;
    private final LikeService likeService;

    @Transactional
    public ProductInfo createProduct(CreateProductCommand command) {
        Brand brand = brandService.findBrand(command.brandId());
        return productService.createProduct(brand, command);
    }

    @Transactional
    public ProductInfo updateProduct(Long productId, UpdateProductCommand command) {
        Brand brand = brandService.findBrand(command.brandId());
        return productService.updateProduct(productId, brand, command);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        likeService.deleteLikesByProductId(productId);
        productService.deleteProduct(productId);
    }
}
