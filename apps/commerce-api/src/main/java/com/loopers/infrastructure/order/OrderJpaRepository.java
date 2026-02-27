package com.loopers.infrastructure.order;

import com.loopers.domain.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndDeletedAtIsNull(Long id);

    @Query("SELECT o FROM Order o WHERE o.userId = :userId " +
           "AND o.createdAt >= :startDateTime AND o.createdAt < :endDateTime " +
           "AND o.deletedAt IS NULL")
    Page<Order> findAllByUserIdAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("startDateTime") ZonedDateTime startDateTime,
            @Param("endDateTime") ZonedDateTime endDateTime,
            Pageable pageable);
}
