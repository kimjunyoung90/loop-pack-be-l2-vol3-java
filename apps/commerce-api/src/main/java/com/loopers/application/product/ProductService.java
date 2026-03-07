package com.loopers.application.product;

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
    public ProductInfo createProduct(Long brandId, CreateProductCommand command) {
        Product product = Product.builder()
                .brandId(brandId)
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
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));

        return ProductInfo.from(product);
    }

    @Transactional
    public ProductInfo updateProduct(Long productId, Long brandId, UpdateProductCommand command) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));

        product.update(brandId, command.name(), command.price(), command.stock());

        return ProductInfo.from(product);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));

        product.delete();
    }

    @Transactional(readOnly = true)
    public List<Product> findProductsByBrandId(Long brandId) {
        return productRepository.findAllByBrandIdAndDeletedAtIsNull(brandId);
    }

    @Transactional
    public void deleteProductsByBrandId(Long brandId) {
        List<Product> products = productRepository.findAllByBrandId(brandId);
        products.forEach(Product::delete);
    }

    @Transactional
    public ProductInfo deductStock(Long productId, int quantity) {
        int updatedCount = productRepository.deductStock(productId, quantity);

		Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));

		if (updatedCount == 0) {
			throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다.");
		}

        return ProductInfo.from(product);
    }

    @Transactional
    public void restoreStock(Long productId, int quantity) {
        int updatedCount = productRepository.restoreStock(productId, quantity);
        if (updatedCount == 0) {
            throw new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다.");
        }
    }

    @Transactional(readOnly = true)
    public Product findProduct(Long productId) {
        return productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));
    }

}
