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

    @Column
    private Long userCouponId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false)
    private int totalAmount;

    @Column(nullable = false)
    private int discountAmount;

    @Column(nullable = false)
    private int finalAmount;

    @Builder
    private Order(Long userId, Long userCouponId) {
        this.userId = userId;
        this.userCouponId = userCouponId;
        this.status = OrderStatus.COMPLETED;
        this.totalAmount = 0;
        this.discountAmount = 0;
        this.finalAmount = 0;
        guard();
    }

    @Override
    protected void guard() {
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문자 ID는 필수입니다.");
        }
        if (status == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 상태는 필수입니다.");
        }
        if (totalAmount < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "총 주문 금액은 0 이상이어야 합니다.");
        }
        if (discountAmount < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "할인 금액은 0 이상이어야 합니다.");
        }
        if (finalAmount < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "최종 결제 금액은 0 이상이어야 합니다.");
        }
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItem.assignOrder(this);
        this.orderItems.add(orderItem);
        this.totalAmount += orderItem.getTotalPrice();
        this.finalAmount = this.totalAmount - this.discountAmount;
    }

    public void applyDiscount(int discountAmount) {
        this.discountAmount = discountAmount;
        this.finalAmount = Math.max(this.totalAmount - this.discountAmount, 0);
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

    public boolean hasCoupon() {
        return this.userCouponId != null;
    }
}
