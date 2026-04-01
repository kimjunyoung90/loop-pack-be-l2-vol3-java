package com.loopers.application.coupon.command;

import com.loopers.domain.coupon.DiscountType;

import java.time.LocalDate;

public record CouponUpdateCommand(
        String name,
        DiscountType discountType,
        int discountValue,
        Integer minOrderAmount,
        LocalDate expiredAt,
        int totalQuantity
) {
}
