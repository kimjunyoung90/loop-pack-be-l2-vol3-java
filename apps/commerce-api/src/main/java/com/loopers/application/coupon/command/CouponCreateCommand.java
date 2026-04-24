package com.loopers.application.coupon.command;

import com.loopers.domain.common.Money;
import com.loopers.domain.coupon.DiscountType;

import java.time.LocalDate;

public record CouponCreateCommand(
        String name,
        DiscountType discountType,
        int discountValue,
        Money minOrderAmount,
        LocalDate expiredAt,
        int totalQuantity
) {
}
