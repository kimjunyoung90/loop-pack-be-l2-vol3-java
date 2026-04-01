package com.loopers.interfaces.api.coupon;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.coupon.response.MyCouponListResponse;
import com.loopers.support.auth.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Coupon V1 API", description = "사용자 쿠폰 관련 API 입니다.")
public interface CouponV1ApiSpec {

    @Operation(
            summary = "쿠폰 발급 요청",
            description = "선착순 쿠폰 발급을 요청합니다. 실제 발급은 비동기로 처리됩니다."
    )
    ApiResponse<Object> issueCoupon(AuthUser authUser, Long couponId);

    @Operation(
            summary = "내 쿠폰 목록 조회",
            description = "로그인한 사용자의 쿠폰 목록을 조회합니다."
    )
    ApiResponse<List<MyCouponListResponse>> getMyCoupons(AuthUser authUser);
}
