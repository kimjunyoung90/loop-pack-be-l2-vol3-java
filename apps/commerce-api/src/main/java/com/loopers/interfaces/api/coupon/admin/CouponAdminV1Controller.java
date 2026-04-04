package com.loopers.interfaces.api.coupon.admin;

import com.loopers.application.coupon.CouponService;
import com.loopers.application.coupon.command.CouponCreateCommand;
import com.loopers.application.coupon.command.CouponUpdateCommand;
import com.loopers.application.coupon.result.CouponResult;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.PageResponse;
import com.loopers.interfaces.api.coupon.admin.request.CouponCreateRequest;
import com.loopers.interfaces.api.coupon.admin.request.CouponUpdateRequest;
import com.loopers.interfaces.api.coupon.admin.response.CouponCreateResponse;
import com.loopers.interfaces.api.coupon.admin.response.CouponDetailResponse;
import com.loopers.interfaces.api.coupon.admin.response.IssuedCouponListResponse;
import com.loopers.interfaces.api.coupon.admin.response.CouponUpdateResponse;
import com.loopers.support.auth.AdminOnly;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    public ApiResponse<CouponCreateResponse> registerCoupon(
            @Valid @RequestBody CouponCreateRequest request
    ) {
        CouponResult couponResult = couponService.registerCoupon(
                new CouponCreateCommand(
                        request.name(),
                        request.discountType(),
                        request.discountValue(),
                        request.minOrderAmount(),
                        request.expiredAt(),
                        request.totalQuantity()
                )
        );
        return ApiResponse.success(CouponCreateResponse.from(couponResult));
    }

    @GetMapping
    @Override
    public ApiResponse<PageResponse<CouponDetailResponse>> getCoupons(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<CouponDetailResponse> coupons = couponService.getCoupons(pageable)
                .map(CouponDetailResponse::from);
        return ApiResponse.success(PageResponse.from(coupons));
    }

    @GetMapping("/{couponId}")
    @Override
    public ApiResponse<CouponDetailResponse> getCoupon(
            @PathVariable Long couponId
    ) {
        CouponResult couponResult = couponService.getCoupon(couponId);
        return ApiResponse.success(CouponDetailResponse.from(couponResult));
    }

    @PutMapping("/{couponId}")
    @Override
    public ApiResponse<CouponUpdateResponse> modifyCoupon(
            @PathVariable Long couponId,
            @Valid @RequestBody CouponUpdateRequest request
    ) {
        CouponResult couponResult = couponService.modifyCoupon(
                couponId,
                new CouponUpdateCommand(
                        request.name(),
                        request.discountType(),
                        request.discountValue(),
                        request.minOrderAmount(),
                        request.expiredAt(),
                        request.totalQuantity()
                )
        );
        return ApiResponse.success(CouponUpdateResponse.from(couponResult));
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
    public ApiResponse<PageResponse<IssuedCouponListResponse>> getIssuedCoupons(
            @PathVariable Long couponId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<IssuedCouponListResponse> issuedCoupons = couponService.getIssuedCoupons(couponId, pageable)
                .map(IssuedCouponListResponse::from);
        return ApiResponse.success(PageResponse.from(issuedCoupons));
    }
}
