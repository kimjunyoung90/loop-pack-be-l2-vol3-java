package com.loopers.application.payment;

import com.loopers.application.payment.command.PaymentCallbackCommand;
import com.loopers.application.payment.command.PaymentCreateCommand;
import com.loopers.application.payment.result.PaymentResult;
import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResult requestPayment(PaymentCreateCommand command) {
        Payment payment = Payment.builder()
                .orderId(command.orderId())
                .userId(command.userId())
                .cardType(CardType.valueOf(command.cardType()))
                .cardNo(command.cardNo())
                .amount(command.amount())
                .build();
        paymentRepository.save(payment);
        return PaymentResult.from(payment);
    }

    @Transactional
    public PaymentResult completePaymentRequest(Long paymentId, String transactionKey) {
        Payment payment = paymentRepository.findByIdAndDeletedAtIsNull(paymentId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 정보를 찾을 수 없습니다."));
        payment.assignTransactionKey(transactionKey);
        return PaymentResult.from(payment);
    }

    @Transactional
    public PaymentResult failPaymentApproval(Long paymentId, String reason) {
        Payment payment = paymentRepository.findByIdAndDeletedAtIsNull(paymentId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 정보를 찾을 수 없습니다."));
        payment.reject(reason);
        return PaymentResult.from(payment);
    }

    @Transactional
    public PaymentResult suspendPaymentByTimeout(Long paymentId) {
        Payment payment = paymentRepository.findByIdAndDeletedAtIsNull(paymentId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 정보를 찾을 수 없습니다."));
        payment.unknown();
        return PaymentResult.from(payment);
    }

    @Transactional
    public PaymentResult handleCallback(PaymentCallbackCommand command) {
        Payment payment = paymentRepository.findByTransactionKeyAndDeletedAtIsNull(command.transactionKey())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 정보를 찾을 수 없습니다."));

        if (command.status() == PaymentStatus.APPROVED) {
            payment.approve();
        } else {
            payment.reject(command.reason());
        }

        return PaymentResult.from(payment);
    }
}
