package com.loopers.application.product;

import com.loopers.domain.product.ProductMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@RequiredArgsConstructor
@Service
public class ProductMetricsService {

    private final ProductMetricsRepository productMetricsRepository;

    @Transactional
    public void incrementLikeCount(Long productId) {
        productMetricsRepository.upsertLikeCount(productId, LocalDate.now(), 1);
    }

    @Transactional
    public void decrementLikeCount(Long productId) {
        productMetricsRepository.upsertLikeCount(productId, LocalDate.now(), -1);
    }

    @Transactional
    public void incrementViewCount(Long productId) {
        productMetricsRepository.upsertViewCount(productId, LocalDate.now(), 1);
    }

    @Transactional
    public void incrementSalesCount(Long productId, int quantity) {
        productMetricsRepository.upsertSalesCount(productId, LocalDate.now(), quantity);
    }
}
