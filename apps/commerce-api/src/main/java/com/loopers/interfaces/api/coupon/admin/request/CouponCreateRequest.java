package com.loopers.interfaces.api.coupon.admin.request;

import com.loopers.domain.coupon.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CouponCreateRequest(
        @NotBlank
        String name,
        @NotNull
        DiscountType discountType,
        @NotNull
        Integer discountValue,
        Integer minOrderAmount,
        @NotNull
        LocalDate expiredAt
) {
}
