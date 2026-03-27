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
        guard();
    }

    // --- 데이터 유효성 검증 ---

    @Override
    protected void guard() {
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
        }
        if (couponId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 ID는 필수입니다.");
        }
        if (couponName == null || couponName.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰명은 필수입니다.");
        }
        if (discountType == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "할인 유형은 필수입니다.");
        }
        if (discountValue <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "할인 값은 0보다 커야 합니다.");
        }
        if (status == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 상태는 필수입니다.");
        }
        if (expiredAt == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유효기간은 필수입니다.");
        }
    }

    // --- 비즈니스 규칙 ---

    public int calculateDiscount(int totalAmount) {
        int discount = switch (discountType) {
            case FIXED -> discountValue;
            case RATE -> totalAmount * discountValue / 100;
        };
        return Math.min(discount, totalAmount);
    }

    public void validateUsable(Long userId, int totalAmount) {
        if (!this.userId.equals(userId)) {
            throw new CoreException(ErrorType.FORBIDDEN, "본인 소유의 쿠폰만 사용할 수 있습니다.");
        }
        if (this.status == CouponStatus.USED) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 사용된 쿠폰입니다.");
        }
        if (isExpired()) {
            expire();
            throw new CoreException(ErrorType.BAD_REQUEST, "만료된 쿠폰은 사용할 수 없습니다.");
        }
        if (minOrderAmount != null && totalAmount < minOrderAmount) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 금액이 쿠폰의 최소 주문 금액에 미달합니다.");
        }
    }

    public void use(Long userId, int totalAmount) {
        validateUsable(userId, totalAmount);
        this.status = CouponStatus.USED;
    }

    public void expire() {
        this.status = CouponStatus.EXPIRED;
    }

    public void restore() {
        if (this.status != CouponStatus.USED) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용된 쿠폰만 복원할 수 있습니다.");
        }
        if (getDeletedAt() != null) {
            throw new CoreException(ErrorType.NOT_FOUND, "삭제된 쿠폰은 복원할 수 없습니다.");
        }
        if (isExpired()) {
            this.status = CouponStatus.EXPIRED;
            return;
        }
        this.status = CouponStatus.AVAILABLE;
    }

    public boolean isExpired() {
        return expiredAt.isBefore(LocalDate.now());
    }

    public boolean isAvailable() {
        return status == CouponStatus.AVAILABLE && !isExpired();
    }
}
