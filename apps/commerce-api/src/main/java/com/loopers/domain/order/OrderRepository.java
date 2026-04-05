package com.loopers.domain.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findByIdAndDeletedAtIsNull(Long orderId);

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    Page<Order> findAllByUserId(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
