package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentFacade;
import com.loopers.application.payment.command.PaymentCallbackCommand;
import com.loopers.application.payment.command.PaymentCreateCommand;
import com.loopers.application.payment.result.PaymentResult;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.payment.request.PaymentCallbackRequest;
import com.loopers.interfaces.api.payment.request.PaymentCreateRequest;
import com.loopers.interfaces.api.payment.response.PaymentCreateResponse;
import com.loopers.support.auth.AuthUser;
import com.loopers.support.auth.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentV1Controller implements PaymentV1ApiSpec {

    private final PaymentFacade paymentFacade;

    @PostMapping
    @Override
    public ApiResponse<PaymentCreateResponse> requestPayment(
            @LoginUser AuthUser authUser,
            @Valid @RequestBody PaymentCreateRequest request
    ) {
        PaymentResult result = paymentFacade.requestPayment(
                new PaymentCreateCommand(
                        authUser.id(),
                        request.orderId(),
                        request.cardType(),
                        request.cardNo(),
                        request.amount()
                )
        );
        return ApiResponse.success(PaymentCreateResponse.from(result));
    }

    @PostMapping("/callback")
    @Override
    public ApiResponse<Object> handleCallback(@RequestBody PaymentCallbackRequest request) {
        paymentFacade.handleCallback(
                new PaymentCallbackCommand(
                        request.transactionKey(),
                        request.orderId(),
                        request.cardType(),
                        request.cardNo(),
                        request.amount(),
                        "SUCCESS".equals(request.status()) ? PaymentStatus.APPROVED : PaymentStatus.REJECTED,
                        request.reason()
                )
        );
        return ApiResponse.success();
    }
}
