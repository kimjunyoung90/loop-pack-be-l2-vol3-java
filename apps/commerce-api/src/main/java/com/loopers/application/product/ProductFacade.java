package com.loopers.application.product;

import com.loopers.application.brand.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class ProductFacade {

    private final ProductService productService;
    private final BrandService brandService;

    @Transactional
    public ProductInfo createProduct(CreateProductCommand command) {
        brandService.getBrand(command.brandId());
        return productService.createProduct(command);
    }

    @Transactional
    public ProductInfo updateProduct(Long productId, UpdateProductCommand command) {
        brandService.getBrand(command.brandId());
        return productService.updateProduct(productId, command);
    }
}
