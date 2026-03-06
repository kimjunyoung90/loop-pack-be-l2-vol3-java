package com.loopers.interfaces.api.coupon.admin;

import com.loopers.application.coupon.CouponInfo;
import com.loopers.application.coupon.CouponService;
import com.loopers.application.coupon.CreateCouponCommand;
import com.loopers.application.coupon.UpdateCouponCommand;
import com.loopers.interfaces.api.ApiResponse;
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
    public ApiResponse<CouponAdminV1Dto.CreateCouponResponse> createCoupon(
            @Valid @RequestBody CouponAdminV1Dto.CreateCouponRequest request
    ) {
        CouponInfo couponInfo = couponService.createCoupon(
                new CreateCouponCommand(
                        request.name(),
                        request.discountType(),
                        request.discountValue(),
                        request.minOrderAmount(),
                        request.expiredAt()
                )
        );
        return ApiResponse.success(CouponAdminV1Dto.CreateCouponResponse.from(couponInfo));
    }

    @GetMapping
    @Override
    public ApiResponse<Page<CouponAdminV1Dto.GetCouponResponse>> getCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<CouponAdminV1Dto.GetCouponResponse> coupons = couponService.getCoupons(PageRequest.of(page, size))
                .map(CouponAdminV1Dto.GetCouponResponse::from);
        return ApiResponse.success(coupons);
    }

    @GetMapping("/{couponId}")
    @Override
    public ApiResponse<CouponAdminV1Dto.GetCouponResponse> getCoupon(
            @PathVariable Long couponId
    ) {
        CouponInfo couponInfo = couponService.getCoupon(couponId);
        return ApiResponse.success(CouponAdminV1Dto.GetCouponResponse.from(couponInfo));
    }

    @PutMapping("/{couponId}")
    @Override
    public ApiResponse<CouponAdminV1Dto.UpdateCouponResponse> updateCoupon(
            @PathVariable Long couponId,
            @Valid @RequestBody CouponAdminV1Dto.UpdateCouponRequest request
    ) {
        CouponInfo couponInfo = couponService.updateCoupon(
                couponId,
                new UpdateCouponCommand(
                        request.name(),
                        request.discountType(),
                        request.discountValue(),
                        request.minOrderAmount(),
                        request.expiredAt()
                )
        );
        return ApiResponse.success(CouponAdminV1Dto.UpdateCouponResponse.from(couponInfo));
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
    public ApiResponse<Page<CouponAdminV1Dto.GetIssuedCouponResponse>> getIssuedCoupons(
            @PathVariable Long couponId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<CouponAdminV1Dto.GetIssuedCouponResponse> issuedCoupons = couponService.getUserCouponsByCouponId(couponId, PageRequest.of(page, size))
                .map(CouponAdminV1Dto.GetIssuedCouponResponse::from);
        return ApiResponse.success(issuedCoupons);
    }
}
