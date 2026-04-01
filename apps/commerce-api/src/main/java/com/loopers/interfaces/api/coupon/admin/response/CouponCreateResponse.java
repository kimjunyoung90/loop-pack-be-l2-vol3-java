package com.loopers.interfaces.api.coupon.admin.response;

import com.loopers.application.coupon.result.CouponResult;
import com.loopers.domain.coupon.DiscountType;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record CouponCreateResponse(
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
    public static CouponCreateResponse from(CouponResult result) {
        return new CouponCreateResponse(
                result.id(),
                result.name(),
                result.discountType(),
                result.discountValue(),
                result.minOrderAmount(),
                result.expiredAt(),
                result.totalQuantity(),
                result.issuedQuantity(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
