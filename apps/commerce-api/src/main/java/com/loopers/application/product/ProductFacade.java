package com.loopers.application.product;

import com.loopers.application.brand.BrandService;
import com.loopers.application.like.LikeService;
import com.loopers.application.product.command.ProductCreateCommand;
import com.loopers.application.product.command.ProductUpdateCommand;
import com.loopers.application.product.result.ProductResult;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
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
    public ProductResult createProduct(ProductCreateCommand command) {
        if (!brandService.existsBrandById(command.brandId())) {
            throw new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다.");
        }
        return productService.createProduct(command.brandId(), command);
    }

    @Transactional
    public ProductResult updateProduct(Long productId, ProductUpdateCommand command) {
        if (!brandService.existsBrandById(command.brandId())) {
            throw new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다.");
        }
        return productService.updateProduct(productId, command.brandId(), command);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        likeService.deleteLikesByProductId(productId);
        productService.deleteProduct(productId);
    }
}
