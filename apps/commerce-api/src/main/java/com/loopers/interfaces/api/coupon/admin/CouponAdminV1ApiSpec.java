package com.loopers.interfaces.api.coupon.admin;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;

@Tag(name = "Coupon Admin V1 API", description = "쿠폰 관련 관리자 API 입니다.")
public interface CouponAdminV1ApiSpec {

    @Operation(
            summary = "쿠폰 등록",
            description = "새로운 쿠폰을 등록합니다."
    )
    ApiResponse<CouponAdminV1Dto.CreateCouponResponse> createCoupon(CouponAdminV1Dto.CreateCouponRequest request);

    @Operation(
            summary = "쿠폰 목록 조회",
            description = "쿠폰 목록을 페이징하여 조회합니다."
    )
    ApiResponse<Page<CouponAdminV1Dto.GetCouponResponse>> getCoupons(int page, int size);

    @Operation(
            summary = "쿠폰 상세 조회",
            description = "쿠폰 상세 정보를 조회합니다."
    )
    ApiResponse<CouponAdminV1Dto.GetCouponResponse> getCoupon(Long couponId);

    @Operation(
            summary = "쿠폰 수정",
            description = "쿠폰 정보를 수정합니다."
    )
    ApiResponse<CouponAdminV1Dto.UpdateCouponResponse> updateCoupon(Long couponId, CouponAdminV1Dto.UpdateCouponRequest request);

    @Operation(
            summary = "쿠폰 삭제",
            description = "쿠폰을 삭제합니다."
    )
    ApiResponse<Object> deleteCoupon(Long couponId);
}
