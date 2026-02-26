package com.loopers.interfaces.api.product.admin;

import com.loopers.application.product.CreateProductCommand;
import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductService;
import com.loopers.application.product.UpdateProductCommand;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.auth.AdminOnly;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@AdminOnly
@RequiredArgsConstructor
@RestController
@RequestMapping("/api-admin/v1/products")
public class ProductAdminV1Controller implements ProductAdminV1ApiSpec {

    private final ProductService productService;
    private final ProductFacade productFacade;

    @PostMapping
    @Override
    public ApiResponse<ProductAdminV1Dto.CreateProductResponse> createProduct(
            @Valid @RequestBody ProductAdminV1Dto.CreateProductRequest request
    ) {
        ProductInfo productInfo = productFacade.createProduct(
                new CreateProductCommand(request.brandId(), request.name(), request.price(), request.stock())
        );
        return ApiResponse.success(ProductAdminV1Dto.CreateProductResponse.from(productInfo));
    }

    @GetMapping
    @Override
    public ApiResponse<Page<ProductAdminV1Dto.GetProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<ProductAdminV1Dto.GetProductResponse> products = productService.getProducts(PageRequest.of(page, size))
                .map(ProductAdminV1Dto.GetProductResponse::from);
        return ApiResponse.success(products);
    }

    @GetMapping("/{productId}")
    @Override
    public ApiResponse<ProductAdminV1Dto.GetProductResponse> getProduct(
            @PathVariable Long productId
    ) {
        ProductInfo productInfo = productService.getProduct(productId);
        return ApiResponse.success(ProductAdminV1Dto.GetProductResponse.from(productInfo));
    }

    @PutMapping("/{productId}")
    @Override
    public ApiResponse<ProductAdminV1Dto.UpdateProductResponse> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductAdminV1Dto.UpdateProductRequest request
    ) {
        ProductInfo productInfo = productFacade.updateProduct(
                productId,
                new UpdateProductCommand(request.brandId(), request.name(), request.price(), request.stock())
        );
        return ApiResponse.success(ProductAdminV1Dto.UpdateProductResponse.from(productInfo));
    }

    @DeleteMapping("/{productId}")
    @Override
    public ApiResponse<Object> deleteProduct(
            @PathVariable Long productId
    ) {
        productService.deleteProduct(productId);
        return ApiResponse.success();
    }
}
