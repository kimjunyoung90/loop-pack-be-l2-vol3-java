package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
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

    public UserCoupon issue(Long userId) {
        if (expiredAt.isBefore(LocalDate.now())) {
            throw new IllegalStateException("만료된 쿠폰은 발급할 수 없습니다.");
        }
        if (issuedQuantity >= totalQuantity) {
            throw new IllegalStateException("쿠폰 발급 수량이 모두 소진되었습니다.");
        }
        this.issuedQuantity++;
        return UserCoupon.create(
                userId,
                this.getId(),
                this.name,
                this.discountType,
                this.discountValue,
                this.minOrderAmount,
                this.expiredAt
        );
    }
}
