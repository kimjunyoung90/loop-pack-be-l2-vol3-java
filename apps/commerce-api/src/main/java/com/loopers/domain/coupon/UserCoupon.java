package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
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

    @Builder(access = AccessLevel.PACKAGE)
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

    public void use() {
        if (isExpired()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "만료된 쿠폰은 사용할 수 없습니다.");
        }
        if (this.status == CouponStatus.USED) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 사용된 쿠폰입니다.");
        }
        this.status = CouponStatus.USED;
    }

    public void restore() {
        this.status = CouponStatus.AVAILABLE;
    }

    public boolean isExpired() {
        return expiredAt.isBefore(LocalDate.now());
    }

    public boolean isAvailable() {
        return status == CouponStatus.AVAILABLE && !isExpired();
    }
}
