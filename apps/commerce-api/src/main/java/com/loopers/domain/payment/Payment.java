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
        if (this.status == PaymentStatus.SUCCESS || this.status == PaymentStatus.FAILED) {
            throw new CoreException(ErrorType.CONFLICT, "이미 최종 처리된 결제입니다.");
        }
        this.status = PaymentStatus.SUCCESS;
    }

    public void reject(String reason) {
        if (this.status == PaymentStatus.SUCCESS || this.status == PaymentStatus.FAILED) {
            throw new CoreException(ErrorType.CONFLICT, "이미 최종 처리된 결제입니다.");
        }
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
    }

    public void unknown() {
        this.status = PaymentStatus.UNKNOWN;
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
