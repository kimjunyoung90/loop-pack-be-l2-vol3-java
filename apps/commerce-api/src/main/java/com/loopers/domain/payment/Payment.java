package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardType cardType;

    @Column(nullable = false)
    private String cardNo;

    @Column(nullable = false)
    private Long amount;

    @Column
    private String transactionKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column
    private String failureReason;

    @Builder
    private Payment(Long orderId, Long userId, CardType cardType, String cardNo, Long amount) {
        this.orderId = orderId;
        this.userId = userId;
        this.cardType = cardType;
        this.cardNo = cardNo;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
        guard();
    }

    public void assignTransactionKey(String transactionKey) {
        this.transactionKey = transactionKey;
    }

    public void approve() {
        if (this.status == PaymentStatus.APPROVED) {
            return;
        }
        if (this.status == PaymentStatus.REJECTED) {
            throw new CoreException(ErrorType.CONFLICT, "이미 거절된 결제는 승인할 수 없습니다.");
        }
        this.status = PaymentStatus.APPROVED;
    }

    public void reject(String reason) {
        if (this.status == PaymentStatus.REJECTED) {
            return;
        }
        if (this.status == PaymentStatus.APPROVED) {
            throw new CoreException(ErrorType.CONFLICT, "이미 승인된 결제는 거절할 수 없습니다.");
        }
        this.status = PaymentStatus.REJECTED;
        this.failureReason = reason;
    }

    public void unknown() {
        this.status = PaymentStatus.UNKNOWN;
    }

    public void abandon(String reason) {
        // TODO(human): 장시간 미확정 결제의 "최종 포기" 도메인 메서드 구현
        //
        // 호출 맥락:
        //   - Reconciliation 스케줄러가 생성 후 30분 이상 PENDING/UNKNOWN에 머문 결제를 감지하여 호출
        //   - 이 시점에서는 PG와의 상태 확정을 포기하고 결제를 종결 상태로 마감해야 함
        //
        // 고민 포인트:
        //   1. "포기"를 어떤 상태로 표현할 것인가?
        //      - 기존 REJECTED를 재활용 (상태 머신 단순 유지) vs 새 상태(예: ABANDONED) 도입 (명시적 구분)
        //      - 선택에 따라 PaymentStatus enum도 수정 필요할 수 있음
        //   2. 이미 종결된 결제(APPROVED/REJECTED)에 abandon 호출되면 어떻게 처리할 것인가?
        //      - 경합 상황에서 뒤늦게 도착하는 호출이 가능 (§1-4 "충돌 시나리오" A/B/C 참고)
        //      - "조용히 넘어가기(no-op)" vs 예외
        //   3. reason 파라미터는 어디에 보존할 것인가?
        //      - failureReason에 기록? 별도 필드?
    }

    @Override
    protected void guard() {
        if (orderId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 ID는 필수입니다.");
        }
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
        }
        if (cardType == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "카드 종류는 필수입니다.");
        }
        if (cardNo == null || cardNo.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "카드 번호는 필수입니다.");
        }
        if (amount == null || amount <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 금액은 0보다 커야 합니다.");
        }
        if (status == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "결제 상태는 필수입니다.");
        }
    }
}
