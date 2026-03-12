package com.loopers.application.product;

import com.loopers.application.brand.BrandService;
import com.loopers.application.brand.result.BrandResult;
import com.loopers.application.like.LikeService;
import com.loopers.application.product.command.ProductCreateCommand;
import com.loopers.application.product.command.ProductUpdateCommand;
import com.loopers.application.product.result.ProductResult;
import com.loopers.application.product.result.ProductWithBrandResult;
import com.loopers.application.product.result.ProductWithLikeCountResult;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class ProductFacade {

    private final ProductService productService;
    private final BrandService brandService;
    private final LikeService likeService;

    @Transactional(readOnly = true)
    public ProductWithBrandResult getProduct(Long productId) {
        ProductResult product = productService.getProduct(productId);
        BrandResult brand = brandService.getBrand(product.brandId());
        int likeCount = likeService.getLikeCount(productId);
        return ProductWithBrandResult.from(product, brand.name(), likeCount);
    }

    @Transactional(readOnly = true)
    public Page<ProductWithBrandResult> getProducts(Long brandId, Pageable pageable) {
        Page<ProductWithLikeCountResult> products = (brandId != null)
                ? productService.getProductsWithLikeCount(brandId, pageable)
                : productService.getProductsWithLikeCount(pageable);

        return products.map(product -> {
            BrandResult brand = brandService.getBrand(product.brandId());
            return ProductWithBrandResult.from(product, brand.name());
        });
    }

    @Transactional
    public ProductResult registerProduct(ProductCreateCommand command) {
        if (!brandService.existsBrand(command.brandId())) {
            throw new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다.");
        }
        return productService.registerProduct(command.brandId(), command);
    }

    @Transactional
    public ProductResult modifyProduct(Long productId, ProductUpdateCommand command) {
        if (!brandService.existsBrand(command.brandId())) {
            throw new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다.");
        }
        return productService.modifyProduct(productId, command.brandId(), command);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        likeService.deleteLikes(productId);
        productService.deleteProduct(productId);
    }
}
