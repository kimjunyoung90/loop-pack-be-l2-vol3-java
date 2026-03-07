package com.loopers.interfaces.api.coupon;

import com.loopers.application.coupon.CouponService;
import com.loopers.application.coupon.result.UserCouponResult;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.coupon.response.MyCouponListResponse;
import com.loopers.interfaces.api.coupon.response.CouponIssueResponse;
import com.loopers.support.auth.AuthUser;
import com.loopers.support.auth.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/coupons")
public class CouponV1Controller implements CouponV1ApiSpec {

    private final CouponService couponService;

    @PostMapping("/{couponId}/issues")
    @Override
    public ApiResponse<CouponIssueResponse> issueCoupon(
            @LoginUser AuthUser authUser,
            @PathVariable Long couponId
    ) {
        UserCouponResult result = couponService.issueCoupon(authUser.id(), couponId);
        return ApiResponse.success(CouponIssueResponse.from(result));
    }

    @GetMapping("/me")
    @Override
    public ApiResponse<List<MyCouponListResponse>> getMyCoupons(
            @LoginUser AuthUser authUser
    ) {
        List<UserCouponResult> results = couponService.getUserCoupons(authUser.id());
        List<MyCouponListResponse> responses = results.stream()
                .map(MyCouponListResponse::from)
                .toList();
        return ApiResponse.success(responses);
    }
}
