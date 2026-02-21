package com.loopers.interfaces.api.product;

import com.loopers.application.product.CreateProductCommand;
import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductService;
import com.loopers.application.product.UpdateProductCommand;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
public class ProductV1Controller implements ProductV1ApiSpec {

    private final ProductService productService;

    @GetMapping
    @Override
    public ApiResponse<List<ProductV1Dto.GetProductResponse>> getProducts() {
        List<ProductV1Dto.GetProductResponse> products = productService.getProducts().stream()
                .map(ProductV1Dto.GetProductResponse::from)
                .toList();
        return ApiResponse.success(products);
    }

    @GetMapping("/{productId}")
    @Override
    public ApiResponse<ProductV1Dto.GetProductResponse> getProduct(@PathVariable Long productId) {
        ProductInfo productInfo = productService.getProduct(productId);
        return ApiResponse.success(ProductV1Dto.GetProductResponse.from(productInfo));
    }

    @PostMapping
    @Override
    public ApiResponse<ProductV1Dto.CreateProductResponse> createProduct(
            @Valid @RequestBody ProductV1Dto.CreateProductRequest request
    ) {
        ProductInfo productInfo = productService.createProduct(
                new CreateProductCommand(request.brandId(), request.name(), request.price(), request.stock())
        );
        return ApiResponse.success(ProductV1Dto.CreateProductResponse.from(productInfo));
    }

    @PutMapping("/{productId}")
    @Override
    public ApiResponse<ProductV1Dto.UpdateProductResponse> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductV1Dto.UpdateProductRequest request
    ) {
        ProductInfo productInfo = productService.updateProduct(
                productId,
                new UpdateProductCommand(request.brandId(), request.name(), request.price(), request.stock())
        );
        return ApiResponse.success(ProductV1Dto.UpdateProductResponse.from(productInfo));
    }

    @DeleteMapping("/{productId}")
    @Override
    public ApiResponse<Object> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ApiResponse.success();
    }
}
