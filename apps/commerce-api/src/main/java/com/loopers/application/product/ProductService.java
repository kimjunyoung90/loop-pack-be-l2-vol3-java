package com.loopers.application.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductInfo createProduct(CreateProductCommand command) {
        Product product = Product.builder()
                .brandId(command.brandId())
                .name(command.name())
                .price(command.price())
                .stock(command.stock())
                .build();

        return ProductInfo.from(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public Page<ProductInfo> getProducts(Pageable pageable) {
        return productRepository.findAllByDeletedAtIsNull(pageable)
                .map(ProductInfo::from);
    }

    @Transactional(readOnly = true)
    public ProductInfo getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));

        return ProductInfo.from(product);
    }

    @Transactional
    public ProductInfo updateProduct(Long productId, UpdateProductCommand command) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));

        product.update(command.brandId(), command.name(), command.price(), command.stock());

        return ProductInfo.from(product);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));

        product.delete();
    }
}
