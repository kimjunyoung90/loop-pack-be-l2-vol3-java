package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.ProductSortType;
import com.loopers.application.product.result.ProductWithBrandResult;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.product.response.ProductWithBrandDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
public class ProductV1Controller implements ProductV1ApiSpec {

    private final ProductFacade productFacade;

    @GetMapping
    @Override
    public ApiResponse<Page<ProductWithBrandDetailResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "LATEST") String sortBy,
            @RequestParam(required = false) Long brandId
    ) {
        ProductSortType sortType = ProductSortType.valueOf(sortBy.toUpperCase());
        Page<ProductWithBrandDetailResponse> products = productFacade.getProducts(brandId, sortType, page, size)
                .map(ProductWithBrandDetailResponse::from);
        return ApiResponse.success(products);
    }

    @GetMapping("/{productId}")
    @Override
    public ApiResponse<ProductWithBrandDetailResponse> getProduct(@PathVariable Long productId) {
        ProductWithBrandResult productResult = productFacade.getProduct(productId);
        return ApiResponse.success(ProductWithBrandDetailResponse.from(productResult));
    }
}