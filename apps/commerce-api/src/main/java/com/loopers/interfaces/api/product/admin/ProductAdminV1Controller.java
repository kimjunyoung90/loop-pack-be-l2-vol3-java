package com.loopers.interfaces.api.product.admin;

import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.ProductService;
import com.loopers.application.product.command.CreateProductCommand;
import com.loopers.application.product.command.UpdateProductCommand;
import com.loopers.application.product.result.ProductResult;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.product.admin.request.CreateProductRequest;
import com.loopers.interfaces.api.product.admin.request.UpdateProductRequest;
import com.loopers.interfaces.api.product.admin.response.CreateProductResponse;
import com.loopers.interfaces.api.product.admin.response.GetProductResponse;
import com.loopers.interfaces.api.product.admin.response.UpdateProductResponse;
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
    public ApiResponse<CreateProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request
    ) {
        ProductResult productResult = productFacade.createProduct(
                new CreateProductCommand(request.brandId(), request.name(), request.price(), request.stock())
        );
        return ApiResponse.success(CreateProductResponse.from(productResult));
    }

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
    public ApiResponse<GetProductResponse> getProduct(
            @PathVariable Long productId
    ) {
        ProductResult productResult = productService.getProduct(productId);
        return ApiResponse.success(GetProductResponse.from(productResult));
    }

    @PutMapping("/{productId}")
    @Override
    public ApiResponse<UpdateProductResponse> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        ProductResult productResult = productFacade.updateProduct(
                productId,
                new UpdateProductCommand(request.brandId(), request.name(), request.price(), request.stock())
        );
        return ApiResponse.success(UpdateProductResponse.from(productResult));
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
