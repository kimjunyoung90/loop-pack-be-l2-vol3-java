package com.loopers.application.product;

import com.loopers.application.product.command.ProductCreateCommand;
import com.loopers.application.product.command.ProductUpdateCommand;
import com.loopers.application.product.result.ProductResult;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResult registerProduct(Long brandId, ProductCreateCommand command) {
        Product product = Product.builder()
                .brandId(brandId)
                .name(command.name())
                .price(command.price())
                .stock(command.stock())
                .build();

        return ProductResult.from(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductResult getProduct(Long productId) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));

        return ProductResult.from(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResult> getProducts(Pageable pageable) {
        return productRepository.findAllByDeletedAtIsNull(pageable)
                .map(ProductResult::from);
    }

    @Transactional(readOnly = true)
    public Page<ProductResult> getProducts(Long brandId, Pageable pageable) {
        return productRepository.findAllByBrandIdAndDeletedAtIsNull(brandId, pageable)
                .map(ProductResult::from);
    }

    @Transactional(readOnly = true)
    public List<ProductResult> getProducts(Long brandId) {
        return productRepository.findAllByBrandIdAndDeletedAtIsNull(brandId).stream()
                .map(ProductResult::from)
                .toList();
    }

    @Transactional
    public ProductResult modifyProduct(Long productId, Long brandId, ProductUpdateCommand command) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));

        product.changeInfo(brandId, command.name(), command.price(), command.stock());

        return ProductResult.from(product);
    }

    @Transactional
    public ProductResult deductStock(Long productId, int quantity) {
        int updatedCount = productRepository.deductStock(productId, quantity);

        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));

        if (updatedCount == 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다.");
        }

        return ProductResult.from(product);
    }

    @Transactional
    public void restoreStock(Long productId, int quantity) {
        int updatedCount = productRepository.restoreStock(productId, quantity);
        if (updatedCount == 0) {
            throw new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다.");
        }
    }

    @Transactional
    public void incrementLikeCount(Long productId) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
        product.incrementLikeCount();
    }

    @Transactional
    public void decrementLikeCount(Long productId) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
        product.decrementLikeCount();
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));

        product.delete();
    }

    @Transactional
    public void deleteProducts(Long brandId) {
        List<Product> products = productRepository.findAllByBrandId(brandId);
        products.forEach(Product::delete);
    }

}
