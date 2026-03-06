package com.loopers.interfaces.api.coupon;

import com.loopers.application.coupon.CouponService;
import com.loopers.application.coupon.UserCouponInfo;
import com.loopers.interfaces.api.ApiResponse;
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
    public ApiResponse<CouponV1Dto.IssueCouponResponse> issueCoupon(
            @LoginUser AuthUser authUser,
            @PathVariable Long couponId
    ) {
        UserCouponInfo info = couponService.issueCoupon(authUser.id(), couponId);
        return ApiResponse.success(CouponV1Dto.IssueCouponResponse.from(info));
    }

    @GetMapping("/me")
    @Override
    public ApiResponse<List<CouponV1Dto.GetMyCouponResponse>> getMyCoupons(
            @LoginUser AuthUser authUser
    ) {
        List<UserCouponInfo> infos = couponService.getUserCoupons(authUser.id());
        List<CouponV1Dto.GetMyCouponResponse> responses = infos.stream()
                .map(CouponV1Dto.GetMyCouponResponse::from)
                .toList();
        return ApiResponse.success(responses);
    }
}
