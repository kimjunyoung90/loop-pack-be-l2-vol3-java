package com.loopers.application.product;

import com.loopers.application.brand.BrandService;
import com.loopers.application.like.LikeService;
import com.loopers.application.product.command.CreateProductCommand;
import com.loopers.application.product.command.UpdateProductCommand;
import com.loopers.application.product.result.ProductResult;
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
    public ProductResult createProduct(CreateProductCommand command) {
        brandService.findBrand(command.brandId());
        return productService.createProduct(command.brandId(), command);
    }

    @Transactional
    public ProductResult updateProduct(Long productId, UpdateProductCommand command) {
        brandService.findBrand(command.brandId());
        return productService.updateProduct(productId, command.brandId(), command);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        likeService.deleteLikesByProductId(productId);
        productService.deleteProduct(productId);
    }
}
