package com.loopers.interfaces.api.coupon.admin;

import com.loopers.application.coupon.CouponService;
import com.loopers.application.coupon.command.CreateCouponCommand;
import com.loopers.application.coupon.command.UpdateCouponCommand;
import com.loopers.application.coupon.result.CouponResult;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.coupon.admin.request.CreateCouponRequest;
import com.loopers.interfaces.api.coupon.admin.request.UpdateCouponRequest;
import com.loopers.interfaces.api.coupon.admin.response.CreateCouponResponse;
import com.loopers.interfaces.api.coupon.admin.response.GetCouponResponse;
import com.loopers.interfaces.api.coupon.admin.response.GetIssuedCouponResponse;
import com.loopers.interfaces.api.coupon.admin.response.UpdateCouponResponse;
import com.loopers.support.auth.AdminOnly;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AdminOnly
@RequiredArgsConstructor
@RestController
@RequestMapping("/api-admin/v1/coupons")
public class CouponAdminV1Controller implements CouponAdminV1ApiSpec {

    private final CouponService couponService;

    @PostMapping
    @Override
    public ApiResponse<CreateCouponResponse> createCoupon(
            @Valid @RequestBody CreateCouponRequest request
    ) {
        CouponResult couponResult = couponService.createCoupon(
                new CreateCouponCommand(
                        request.name(),
                        request.discountType(),
                        request.discountValue(),
                        request.minOrderAmount(),
                        request.expiredAt()
                )
        );
        return ApiResponse.success(CreateCouponResponse.from(couponResult));
    }

    @GetMapping
    @Override
    public ApiResponse<Page<GetCouponResponse>> getCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<GetCouponResponse> coupons = couponService.getCoupons(PageRequest.of(page, size))
                .map(GetCouponResponse::from);
        return ApiResponse.success(coupons);
    }

    @GetMapping("/{couponId}")
    @Override
    public ApiResponse<GetCouponResponse> getCoupon(
            @PathVariable Long couponId
    ) {
        CouponResult couponResult = couponService.getCoupon(couponId);
        return ApiResponse.success(GetCouponResponse.from(couponResult));
    }

    @PutMapping("/{couponId}")
    @Override
    public ApiResponse<UpdateCouponResponse> updateCoupon(
            @PathVariable Long couponId,
            @Valid @RequestBody UpdateCouponRequest request
    ) {
        CouponResult couponResult = couponService.updateCoupon(
                couponId,
                new UpdateCouponCommand(
                        request.name(),
                        request.discountType(),
                        request.discountValue(),
                        request.minOrderAmount(),
                        request.expiredAt()
                )
        );
        return ApiResponse.success(UpdateCouponResponse.from(couponResult));
    }

    @DeleteMapping("/{couponId}")
    @Override
    public ApiResponse<Object> deleteCoupon(
            @PathVariable Long couponId
    ) {
        couponService.deleteCoupon(couponId);
        return ApiResponse.success();
    }

    @GetMapping("/{couponId}/issues")
    @Override
    public ApiResponse<Page<GetIssuedCouponResponse>> getIssuedCoupons(
            @PathVariable Long couponId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<GetIssuedCouponResponse> issuedCoupons = couponService.getUserCouponsByCouponId(couponId, PageRequest.of(page, size))
                .map(GetIssuedCouponResponse::from);
        return ApiResponse.success(issuedCoupons);
    }
}
