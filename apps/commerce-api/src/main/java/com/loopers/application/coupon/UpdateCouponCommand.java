package com.loopers.application.coupon;

import com.loopers.domain.coupon.DiscountType;

import java.time.LocalDate;

public record UpdateCouponCommand(
        String name,
        DiscountType discountType,
        int discountValue,
        Integer minOrderAmount,
        LocalDate expiredAt
) {
}
