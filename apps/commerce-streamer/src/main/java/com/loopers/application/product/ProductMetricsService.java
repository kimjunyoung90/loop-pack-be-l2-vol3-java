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
    public void incrementLikeCount(Long productId, LocalDate eventDate) {
        productMetricsRepository.upsertLikeCount(productId, eventDate, 1);
    }

    @Transactional
    public void decrementLikeCount(Long productId, LocalDate eventDate) {
        productMetricsRepository.upsertLikeCount(productId, eventDate, -1);
    }

    @Transactional
    public void incrementViewCount(Long productId, LocalDate eventDate) {
        productMetricsRepository.upsertViewCount(productId, eventDate, 1);
    }

    @Transactional
    public void incrementSalesCount(Long productId, int quantity, LocalDate eventDate) {
        productMetricsRepository.upsertSalesCount(productId, eventDate, quantity);
    }
}
