package com.loopers.application.product;

import com.loopers.application.brand.BrandService;
import com.loopers.application.like.LikeService;
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
    public ProductInfo createProduct(ProductCommand.Create command) {
        brandService.validateBrandExists(command.brandId());
        return productService.createProduct(command);
    }

    @Transactional
    public ProductInfo updateProduct(Long productId, ProductCommand.Update command) {
        brandService.validateBrandExists(command.brandId());
        return productService.updateProduct(productId, command);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        likeService.deleteLikesByProductId(productId);
        productService.deleteProduct(productId);
    }
}
