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
@Table(name = "coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    private int discountValue;

    private Integer minOrderAmount;

    @Column(nullable = false)
    private LocalDate expiredAt;

    @Column(nullable = false)
    private int totalQuantity;

    @Column(nullable = false)
    private int issuedQuantity;

    @Builder
    private Coupon(String name, DiscountType discountType, int discountValue, Integer minOrderAmount, LocalDate expiredAt, int totalQuantity) {
        this.name = name;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minOrderAmount = minOrderAmount;
        this.expiredAt = expiredAt;
        this.totalQuantity = totalQuantity;
        this.issuedQuantity = 0;
        guard();
        if (expiredAt != null && expiredAt.isBefore(LocalDate.now())) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유효기간은 현재 시점 이후여야 합니다.");
        }
    }

    public void changeInfo(String name, DiscountType discountType, int discountValue, Integer minOrderAmount, LocalDate expiredAt, int totalQuantity) {
        this.name = name;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minOrderAmount = minOrderAmount;
        this.expiredAt = expiredAt;
        this.totalQuantity = totalQuantity;
        guard();
        if (expiredAt != null && expiredAt.isBefore(LocalDate.now())) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유효기간은 현재 시점 이후여야 합니다.");
        }
    }

    @Override
    protected void guard() {
        if (name == null || name.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰명은 필수입니다.");
        }
        if (discountType == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "할인 유형은 필수입니다.");
        }
        if (discountValue <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "할인 값은 0보다 커야 합니다.");
        }
        if (discountType == DiscountType.RATE && 100 < discountValue) {
            throw new CoreException(ErrorType.BAD_REQUEST, "정률 할인 값은 100 이하여야 합니다.");
        }
        if (minOrderAmount != null && minOrderAmount <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "최소 주문 금액은 0보다 커야 합니다.");
        }
        if (expiredAt == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유효기간은 필수입니다.");
        }
        if (totalQuantity <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "발급 수량은 0보다 커야 합니다.");
        }
    }

    public UserCoupon issue(Long userId) {
        if (getId() == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "저장되지 않은 쿠폰은 발급할 수 없습니다.");
        }
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
        }
        if (getDeletedAt() != null) {
            throw new CoreException(ErrorType.NOT_FOUND, "삭제된 쿠폰은 발급할 수 없습니다.");
        }
        if (expiredAt.isBefore(LocalDate.now())) {
            throw new CoreException(ErrorType.BAD_REQUEST, "만료된 쿠폰은 발급할 수 없습니다.");
        }
        if (issuedQuantity >= totalQuantity) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 발급 수량이 모두 소진되었습니다.");
        }
        this.issuedQuantity++;
        return UserCoupon.builder()
                .userId(userId)
                .couponId(this.getId())
                .couponName(this.name)
                .discountType(this.discountType)
                .discountValue(this.discountValue)
                .minOrderAmount(this.minOrderAmount)
                .expiredAt(this.expiredAt)
                .build();
    }
}
