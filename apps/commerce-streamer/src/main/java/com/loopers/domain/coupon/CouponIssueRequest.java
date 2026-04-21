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

@Entity
@Table(name = "coupon_issue_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponIssueRequest extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long couponId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponRequestStatus status;

    private String failReason;

    public void approve() {
        if (this.status != CouponRequestStatus.PENDING) {
            throw new IllegalStateException("대기 상태의 요청만 승인할 수 있습니다.");
        }
        this.status = CouponRequestStatus.SUCCESS;
    }

    public void reject(String reason) {
        if (this.status != CouponRequestStatus.PENDING) {
            throw new IllegalStateException("대기 상태의 요청만 거절할 수 있습니다.");
        }
        this.status = CouponRequestStatus.FAILED;
        this.failReason = reason;
    }

    public boolean isPending() {
        return this.status == CouponRequestStatus.PENDING;
    }
}
