package com.loopers.interfaces.api.payment;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.payment.request.PaymentCallbackRequest;
import com.loopers.interfaces.api.payment.request.PaymentCreateRequest;
import com.loopers.interfaces.api.payment.response.PaymentCreateResponse;
import com.loopers.support.auth.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Payment V1 API", description = "결제 관련 API 입니다.")
public interface PaymentV1ApiSpec {

    @Operation(
        summary = "결제 요청",
        description = "PG사에 결제를 요청합니다. 서킷 브레이커가 적용되어 있습니다."
    )
    ApiResponse<PaymentCreateResponse> requestPayment(AuthUser authUser, PaymentCreateRequest request);

    @Operation(
        summary = "결제 콜백",
        description = "PG사로부터 결제 결과를 수신합니다."
    )
    ApiResponse<Object> handleCallback(PaymentCallbackRequest request);
}
