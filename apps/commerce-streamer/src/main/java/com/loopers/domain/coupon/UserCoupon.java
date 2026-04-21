package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "user_coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCoupon extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long couponId;

    @Column(nullable = false)
    private String couponName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    private int discountValue;

    private Integer minOrderAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponStatus status;

    @Column(nullable = false)
    private LocalDate expiredAt;

    @Builder(access = AccessLevel.PRIVATE)
    private UserCoupon(Long userId, Long couponId, String couponName,
                       DiscountType discountType, int discountValue, Integer minOrderAmount,
                       LocalDate expiredAt) {
        this.userId = userId;
        this.couponId = couponId;
        this.couponName = couponName;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minOrderAmount = minOrderAmount;
        this.status = CouponStatus.AVAILABLE;
        this.expiredAt = expiredAt;
    }

    static UserCoupon create(Long userId, Long couponId, String couponName,
                             DiscountType discountType, int discountValue, Integer minOrderAmount,
                             LocalDate expiredAt) {
        return UserCoupon.builder()
                .userId(userId)
                .couponId(couponId)
                .couponName(couponName)
                .discountType(discountType)
                .discountValue(discountValue)
                .minOrderAmount(minOrderAmount)
                .expiredAt(expiredAt)
                .build();
    }
}
