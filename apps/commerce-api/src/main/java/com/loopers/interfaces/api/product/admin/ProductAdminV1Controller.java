package com.loopers.interfaces.api.product.admin;

import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.ProductService;
import com.loopers.application.product.command.ProductCreateCommand;
import com.loopers.application.product.command.ProductUpdateCommand;
import com.loopers.application.product.result.ProductResult;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.product.admin.request.ProductCreateRequest;
import com.loopers.interfaces.api.product.admin.request.ProductUpdateRequest;
import com.loopers.interfaces.api.product.admin.response.ProductCreateResponse;
import com.loopers.interfaces.api.product.admin.response.ProductGetResponse;
import com.loopers.interfaces.api.product.admin.response.ProductUpdateResponse;
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
    public ApiResponse<ProductCreateResponse> createProduct(
            @Valid @RequestBody ProductCreateRequest request
    ) {
        ProductResult productResult = productFacade.createProduct(
                new ProductCreateCommand(request.brandId(), request.name(), request.price(), request.stock())
        );
        return ApiResponse.success(ProductCreateResponse.from(productResult));
    }

    @GetMapping
    @Override
    public ApiResponse<Page<ProductGetResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<ProductGetResponse> products = productService.getProducts(PageRequest.of(page, size))
                .map(ProductGetResponse::from);
        return ApiResponse.success(products);
    }

    @GetMapping("/{productId}")
    @Override
    public ApiResponse<ProductGetResponse> getProduct(
            @PathVariable Long productId
    ) {
        ProductResult productResult = productService.getProduct(productId);
        return ApiResponse.success(ProductGetResponse.from(productResult));
    }

    @PutMapping("/{productId}")
    @Override
    public ApiResponse<ProductUpdateResponse> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        ProductResult productResult = productFacade.updateProduct(
                productId,
                new ProductUpdateCommand(request.brandId(), request.name(), request.price(), request.stock())
        );
        return ApiResponse.success(ProductUpdateResponse.from(productResult));
    }

    @DeleteMapping("/{productId}")
    @Override
    public ApiResponse<Object> deleteProduct(
            @PathVariable Long productId
    ) {
        productFacade.deleteProduct(productId);
        return ApiResponse.success();
    }
}
