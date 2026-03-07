package com.loopers.application.coupon;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.DiscountType;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record CouponInfo(
        Long id,
        String name,
        DiscountType discountType,
        int discountValue,
        Integer minOrderAmount,
        LocalDate expiredAt,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static CouponInfo from(Coupon coupon) {
        return new CouponInfo(
                coupon.getId(),
                coupon.getName(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                coupon.getMinOrderAmount(),
                coupon.getExpiredAt(),
                coupon.getCreatedAt(),
                coupon.getUpdatedAt()
        );
    }
}
