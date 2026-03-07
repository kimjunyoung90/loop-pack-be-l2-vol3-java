package com.loopers.interfaces.api.coupon.admin;

import com.loopers.application.coupon.CouponService;
import com.loopers.application.coupon.command.CouponCreateCommand;
import com.loopers.application.coupon.command.CouponUpdateCommand;
import com.loopers.application.coupon.result.CouponResult;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.coupon.admin.request.CouponCreateRequest;
import com.loopers.interfaces.api.coupon.admin.request.CouponUpdateRequest;
import com.loopers.interfaces.api.coupon.admin.response.CouponCreateResponse;
import com.loopers.interfaces.api.coupon.admin.response.CouponGetResponse;
import com.loopers.interfaces.api.coupon.admin.response.IssuedCouponGetResponse;
import com.loopers.interfaces.api.coupon.admin.response.CouponUpdateResponse;
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
    public ApiResponse<CouponCreateResponse> createCoupon(
            @Valid @RequestBody CouponCreateRequest request
    ) {
        CouponResult couponResult = couponService.createCoupon(
                new CouponCreateCommand(
                        request.name(),
                        request.discountType(),
                        request.discountValue(),
                        request.minOrderAmount(),
                        request.expiredAt()
                )
        );
        return ApiResponse.success(CouponCreateResponse.from(couponResult));
    }

    @GetMapping
    @Override
    public ApiResponse<Page<CouponGetResponse>> getCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<CouponGetResponse> coupons = couponService.getCoupons(PageRequest.of(page, size))
                .map(CouponGetResponse::from);
        return ApiResponse.success(coupons);
    }

    @GetMapping("/{couponId}")
    @Override
    public ApiResponse<CouponGetResponse> getCoupon(
            @PathVariable Long couponId
    ) {
        CouponResult couponResult = couponService.getCoupon(couponId);
        return ApiResponse.success(CouponGetResponse.from(couponResult));
    }

    @PutMapping("/{couponId}")
    @Override
    public ApiResponse<CouponUpdateResponse> updateCoupon(
            @PathVariable Long couponId,
            @Valid @RequestBody CouponUpdateRequest request
    ) {
        CouponResult couponResult = couponService.updateCoupon(
                couponId,
                new CouponUpdateCommand(
                        request.name(),
                        request.discountType(),
                        request.discountValue(),
                        request.minOrderAmount(),
                        request.expiredAt()
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
    public ApiResponse<Page<IssuedCouponGetResponse>> getIssuedCoupons(
            @PathVariable Long couponId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<IssuedCouponGetResponse> issuedCoupons = couponService.getUserCouponsByCouponId(couponId, PageRequest.of(page, size))
                .map(IssuedCouponGetResponse::from);
        return ApiResponse.success(issuedCoupons);
    }
}
