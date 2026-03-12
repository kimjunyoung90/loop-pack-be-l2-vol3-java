package com.loopers.interfaces.api.product.admin;

import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.ProductService;
import com.loopers.application.product.command.ProductCreateCommand;
import com.loopers.application.product.command.ProductUpdateCommand;
import com.loopers.application.product.result.ProductResult;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.PageResponse;
import com.loopers.interfaces.api.product.admin.request.ProductCreateRequest;
import com.loopers.interfaces.api.product.admin.request.ProductUpdateRequest;
import com.loopers.interfaces.api.product.admin.response.ProductCreateResponse;
import com.loopers.interfaces.api.product.admin.response.ProductDetailResponse;
import com.loopers.interfaces.api.product.admin.response.ProductUpdateResponse;
import com.loopers.support.auth.AdminOnly;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    public ApiResponse<ProductCreateResponse> registerProduct(
            @Valid @RequestBody ProductCreateRequest request
    ) {
        ProductResult productResult = productFacade.registerProduct(
                new ProductCreateCommand(request.brandId(), request.name(), request.price(), request.stock())
        );
        return ApiResponse.success(ProductCreateResponse.from(productResult));
    }

    @GetMapping
    @Override
    public ApiResponse<PageResponse<ProductDetailResponse>> getProducts(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ProductDetailResponse> products = productService.getProducts(pageable)
                .map(ProductDetailResponse::from);
        return ApiResponse.success(PageResponse.from(products));
    }

    @GetMapping("/{productId}")
    @Override
    public ApiResponse<ProductDetailResponse> getProduct(
            @PathVariable Long productId
    ) {
        ProductResult productResult = productService.getProduct(productId);
        return ApiResponse.success(ProductDetailResponse.from(productResult));
    }

    @PutMapping("/{productId}")
    @Override
    public ApiResponse<ProductUpdateResponse> modifyProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        ProductResult productResult = productFacade.modifyProduct(
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
