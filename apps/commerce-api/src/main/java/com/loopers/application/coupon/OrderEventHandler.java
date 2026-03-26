package com.loopers.application.coupon;

import com.loopers.domain.order.event.OrderCancelledEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class OrderEventHandler {

    private final CouponService couponService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleOrderCancelled(OrderCancelledEvent event) {
        if (event.userCouponId() != null) {
            couponService.restoreCoupon(event.userCouponId());
        }
    }
}
