package com.loopers.application.coupon.result;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.DiscountType;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record CouponResult(
        Long id,
        String name,
        DiscountType discountType,
        int discountValue,
        Integer minOrderAmount,
        LocalDate expiredAt,
        int totalQuantity,
        int issuedQuantity,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static CouponResult from(Coupon coupon) {
        return new CouponResult(
                coupon.getId(),
                coupon.getName(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                coupon.getMinOrderAmount(),
                coupon.getExpiredAt(),
                coupon.getTotalQuantity(),
                coupon.getIssuedQuantity(),
                coupon.getCreatedAt(),
                coupon.getUpdatedAt()
        );
    }
}
