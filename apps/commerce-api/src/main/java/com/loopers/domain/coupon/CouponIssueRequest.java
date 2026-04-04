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

    @Builder(access = AccessLevel.PRIVATE)
    private CouponIssueRequest(Long userId, Long couponId) {
        this.userId = userId;
        this.couponId = couponId;
        this.status = CouponRequestStatus.PENDING;
        guard();
    }

    public static CouponIssueRequest create(Long userId, Long couponId) {
        return CouponIssueRequest.builder()
                .userId(userId)
                .couponId(couponId)
                .build();
    }

    @Override
    protected void guard() {
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
        }
        if (couponId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 ID는 필수입니다.");
        }
        if (status == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "요청 상태는 필수입니다.");
        }
    }

    // --- 비즈니스 규칙 ---

    public void approve() {
        if (this.status != CouponRequestStatus.PENDING) {
            throw new CoreException(ErrorType.BAD_REQUEST, "대기 상태의 요청만 승인할 수 있습니다.");
        }
        this.status = CouponRequestStatus.SUCCESS;
    }

    public void reject(String reason) {
        if (this.status != CouponRequestStatus.PENDING) {
            throw new CoreException(ErrorType.BAD_REQUEST, "대기 상태의 요청만 거절할 수 있습니다.");
        }
        this.status = CouponRequestStatus.FAILED;
        this.failReason = reason;
    }

    public void retry() {
        if (this.status != CouponRequestStatus.FAILED) {
            throw new CoreException(ErrorType.BAD_REQUEST, "실패한 요청만 재시도할 수 있습니다.");
        }
        this.status = CouponRequestStatus.PENDING;
        this.failReason = null;
    }

    public boolean isPending() {
        return this.status == CouponRequestStatus.PENDING;
    }
}
