package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.common.Money;
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

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Column
    private Long userCouponId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false)
    private Money totalAmount;

    @Column(nullable = false)
    private Money discountAmount;

    @Column(nullable = false)
    private Money finalAmount;

    @Builder
    private Order(Long userId, String idempotencyKey, Long userCouponId) {
        this.userId = userId;
        this.idempotencyKey = idempotencyKey;
        this.userCouponId = userCouponId;
        this.status = OrderStatus.PENDING;
        this.totalAmount = Money.ZERO;
        this.discountAmount = Money.ZERO;
        this.finalAmount = Money.ZERO;
        guard();
    }

    @Override
    protected void guard() {
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문자 ID는 필수입니다.");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "멱등성 키는 필수입니다.");
        }
        if (status == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 상태는 필수입니다.");
        }
        if (totalAmount == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "총 주문 금액은 필수입니다.");
        }
        if (discountAmount == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "할인 금액은 필수입니다.");
        }
        if (finalAmount == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "최종 결제 금액은 필수입니다.");
        }
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItem.assignOrder(this);
        this.orderItems.add(orderItem);
        refreshAmounts();
    }

    public void applyDiscount(Money discountAmount) {
        this.discountAmount = discountAmount;
        refreshAmounts();
    }

    private void refreshAmounts() {
        this.totalAmount = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(Money.ZERO, Money::add);
        this.finalAmount = this.totalAmount.subtract(this.discountAmount);
    }

    public void complete() {
		if(this.status != OrderStatus.PENDING) {
			throw new CoreException(ErrorType.BAD_REQUEST, "결제 대기 중인 주문만 완료할 수 있습니다.");
		}
		this.status = OrderStatus.COMPLETED;
    }

    public void cancel(Long userId) {
        if (!isOwnedBy(userId)) {
            throw new CoreException(ErrorType.FORBIDDEN, "본인의 주문만 취소할 수 있습니다.");
        }
        if (isCancelled()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 취소된 주문입니다.");
        }
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
