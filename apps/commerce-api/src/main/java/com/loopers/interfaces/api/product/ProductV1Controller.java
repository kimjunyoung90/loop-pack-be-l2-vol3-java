package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.result.ProductWithBrandResult;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.PageResponse;
import com.loopers.interfaces.api.product.response.ProductWithBrandDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
public class ProductV1Controller implements ProductV1ApiSpec {

    private final ProductFacade productFacade;

    @GetMapping
    @Override
    public ApiResponse<PageResponse<ProductWithBrandDetailResponse>> getProducts(
            @PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable,
            @RequestParam(required = false) Long brandId
    ) {
        Page<ProductWithBrandDetailResponse> products = productFacade.getProducts(brandId, pageable)
                .map(ProductWithBrandDetailResponse::from);
        return ApiResponse.success(PageResponse.from(products));
    }

    @GetMapping("/{productId}")
    @Override
    public ApiResponse<ProductWithBrandDetailResponse> getProduct(@PathVariable Long productId) {
        ProductWithBrandResult productResult = productFacade.getProduct(productId);
        return ApiResponse.success(ProductWithBrandDetailResponse.from(productResult));
    }
}