package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductService;
import com.loopers.application.product.result.ProductResult;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.product.response.GetProductResponse;
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
    public ApiResponse<Page<GetProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<GetProductResponse> products = productService.getProducts(PageRequest.of(page, size))
                .map(GetProductResponse::from);
        return ApiResponse.success(products);
    }

    @GetMapping("/{productId}")
    @Override
    public ApiResponse<GetProductResponse> getProduct(@PathVariable Long productId) {
        ProductResult productResult = productService.getProduct(productId);
        return ApiResponse.success(GetProductResponse.from(productResult));
    }
}
