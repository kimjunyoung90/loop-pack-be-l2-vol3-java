package com.loopers.interfaces.api.coupon;

import com.loopers.application.coupon.UserCouponInfo;
import com.loopers.domain.coupon.DiscountType;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public class CouponV1Dto {

    public record IssueCouponResponse(
            Long id,
            String couponName,
            DiscountType discountType,
            int discountValue,
            String status,
            LocalDate expiredAt,
            ZonedDateTime createdAt
    ) {
        public static IssueCouponResponse from(UserCouponInfo info) {
            return new IssueCouponResponse(
                    info.id(),
                    info.couponName(),
                    info.discountType(),
                    info.discountValue(),
                    info.status(),
                    info.expiredAt(),
                    info.createdAt()
            );
        }
    }

    public record GetMyCouponResponse(
            Long id,
            Long couponId,
            String couponName,
            DiscountType discountType,
            int discountValue,
            Integer minOrderAmount,
            String status,
            LocalDate expiredAt,
            ZonedDateTime createdAt
    ) {
        public static GetMyCouponResponse from(UserCouponInfo info) {
            return new GetMyCouponResponse(
                    info.id(),
                    info.couponId(),
                    info.couponName(),
                    info.discountType(),
                    info.discountValue(),
                    info.minOrderAmount(),
                    info.status(),
                    info.expiredAt(),
                    info.createdAt()
            );
        }
    }
}
