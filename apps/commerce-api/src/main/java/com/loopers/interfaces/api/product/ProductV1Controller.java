package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
public class ProductV1Controller implements ProductV1ApiSpec {

    private final ProductService productService;

    @GetMapping
    @Override
    public ApiResponse<Page<ProductV1Dto.GetProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<ProductV1Dto.GetProductResponse> products = productService.getProducts(PageRequest.of(page, size))
                .map(ProductV1Dto.GetProductResponse::from);
        return ApiResponse.success(products);
    }

    @GetMapping("/{productId}")
    @Override
    public ApiResponse<ProductV1Dto.GetProductResponse> getProduct(@PathVariable Long productId) {
        ProductInfo productInfo = productService.getProduct(productId);
        return ApiResponse.success(ProductV1Dto.GetProductResponse.from(productInfo));
    }
}
