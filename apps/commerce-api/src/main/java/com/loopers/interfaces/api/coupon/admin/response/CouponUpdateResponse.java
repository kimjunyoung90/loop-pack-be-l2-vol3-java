package com.loopers.interfaces.api.coupon.admin.response;

import com.loopers.application.coupon.result.CouponResult;
import com.loopers.domain.coupon.DiscountType;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record CouponUpdateResponse(
        Long id,
        String name,
        DiscountType discountType,
        int discountValue,
        Integer minOrderAmount,
        LocalDate expiredAt,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static CouponUpdateResponse from(CouponResult result) {
        return new CouponUpdateResponse(
                result.id(),
                result.name(),
                result.discountType(),
                result.discountValue(),
                result.minOrderAmount(),
                result.expiredAt(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
