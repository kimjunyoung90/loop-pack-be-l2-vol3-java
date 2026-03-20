package com.loopers.application.payment;

import com.loopers.application.payment.command.PaymentCallbackCommand;
import com.loopers.application.payment.command.PaymentCreateCommand;
import com.loopers.application.payment.result.PaymentResult;
import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
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
    public Payment createPendingPayment(PaymentCreateCommand command) {
        Payment payment = Payment.builder()
                .orderId(command.orderId())
                .userId(command.userId())
                .cardType(CardType.valueOf(command.cardType()))
                .cardNo(command.cardNo())
                .amount(command.amount())
                .build();
        return paymentRepository.save(payment);
    }

    @Transactional
    public PaymentResult completePaymentRequest(Long paymentId, String transactionKey) {
        Payment payment = paymentRepository.findByIdAndDeletedAtIsNull(paymentId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 정보를 찾을 수 없습니다."));
        payment.assignTransactionKey(transactionKey);
        return PaymentResult.from(payment);
    }

    @Transactional
    public PaymentResult failPayment(Long paymentId, String reason) {
        Payment payment = paymentRepository.findByIdAndDeletedAtIsNull(paymentId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 정보를 찾을 수 없습니다."));
        payment.reject(reason);
        return PaymentResult.from(payment);
    }

    @Transactional
    public PaymentResult unknownPayment(Long paymentId) {
        Payment payment = paymentRepository.findByIdAndDeletedAtIsNull(paymentId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 정보를 찾을 수 없습니다."));
        payment.unknown();
        return PaymentResult.from(payment);
    }

    @Transactional
    public PaymentResult handleCallback(PaymentCallbackCommand command) {
        Payment payment = paymentRepository.findByTransactionKeyAndDeletedAtIsNull(command.transactionKey())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 정보를 찾을 수 없습니다."));

        if ("SUCCESS".equals(command.status())) {
            payment.approve();
        } else {
            payment.reject(command.reason());
        }

        return PaymentResult.from(payment);
    }
}
