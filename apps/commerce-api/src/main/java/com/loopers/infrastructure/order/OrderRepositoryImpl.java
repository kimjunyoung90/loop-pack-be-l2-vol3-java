package com.loopers.infrastructure.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Optional<Order> findById(Long orderId) {
        return orderJpaRepository.findByIdAndDeletedAtIsNull(orderId);
    }

    @Override
    public Page<Order> findAllByUserId(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        ZonedDateTime startDateTime = startDate.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime endDateTime = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault());
        return orderJpaRepository.findAllByUserIdAndCreatedAtBetween(userId, startDateTime, endDateTime, pageable);
    }
}
