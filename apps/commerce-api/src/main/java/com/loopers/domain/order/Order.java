package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
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

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

    public void cancel(Long userId) {
        if (!isOwnedBy(userId)) {
            throw new CoreException(ErrorType.FORBIDDEN, "본인의 주문만 취소할 수 있습니다.");
        }
        if (this.status == OrderStatus.CANCELLED) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 취소된 주문입니다.");
        }
        this.status = OrderStatus.CANCELLED;
    }
}
