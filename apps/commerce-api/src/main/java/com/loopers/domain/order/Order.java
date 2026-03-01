package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false)
    private int totalPrice;

    @Builder
    private Order(Long userId) {
        this.userId = userId;
        this.status = OrderStatus.COMPLETED;
        this.totalPrice = 0;
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItem.assignOrder(this);
        this.orderItems.add(orderItem);
        this.totalPrice += orderItem.getTotalPrice();
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }

    public boolean isCancelled() {
        return this.status == OrderStatus.CANCELLED;
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }
}
