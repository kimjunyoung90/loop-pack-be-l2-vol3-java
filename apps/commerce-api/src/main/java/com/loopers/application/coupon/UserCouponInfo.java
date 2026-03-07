package com.loopers.application.coupon;

import com.loopers.domain.coupon.CouponStatus;
import com.loopers.domain.coupon.DiscountType;
import com.loopers.domain.coupon.UserCoupon;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record UserCouponInfo(
        Long id,
        Long userId,
        Long couponId,
        String couponName,
        DiscountType discountType,
        int discountValue,
        Integer minOrderAmount,
        String status,
        LocalDate expiredAt,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static UserCouponInfo from(UserCoupon userCoupon) {
        String resolvedStatus;
        if (userCoupon.getStatus() == CouponStatus.USED) {
            resolvedStatus = "USED";
        } else if (userCoupon.isExpired()) {
            resolvedStatus = "EXPIRED";
        } else {
            resolvedStatus = "AVAILABLE";
        }

        return new UserCouponInfo(
                userCoupon.getId(),
                userCoupon.getUserId(),
                userCoupon.getCouponId(),
                userCoupon.getCouponName(),
                userCoupon.getDiscountType(),
                userCoupon.getDiscountValue(),
                userCoupon.getMinOrderAmount(),
                resolvedStatus,
                userCoupon.getExpiredAt(),
                userCoupon.getCreatedAt(),
                userCoupon.getUpdatedAt()
        );
    }
}
