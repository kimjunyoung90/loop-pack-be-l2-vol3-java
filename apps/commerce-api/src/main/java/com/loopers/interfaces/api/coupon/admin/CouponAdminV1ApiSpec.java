package com.loopers.interfaces.api.coupon.admin;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.coupon.admin.request.CreateCouponRequest;
import com.loopers.interfaces.api.coupon.admin.request.UpdateCouponRequest;
import com.loopers.interfaces.api.coupon.admin.response.CreateCouponResponse;
import com.loopers.interfaces.api.coupon.admin.response.GetCouponResponse;
import com.loopers.interfaces.api.coupon.admin.response.GetIssuedCouponResponse;
import com.loopers.interfaces.api.coupon.admin.response.UpdateCouponResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;

@Tag(name = "Coupon Admin V1 API", description = "쿠폰 관련 관리자 API 입니다.")
public interface CouponAdminV1ApiSpec {

    @Operation(
            summary = "쿠폰 등록",
            description = "새로운 쿠폰을 등록합니다."
    )
    ApiResponse<CreateCouponResponse> createCoupon(CreateCouponRequest request);

    @Operation(
            summary = "쿠폰 목록 조회",
            description = "쿠폰 목록을 페이징하여 조회합니다."
    )
    ApiResponse<Page<GetCouponResponse>> getCoupons(int page, int size);

    @Operation(
            summary = "쿠폰 상세 조회",
            description = "쿠폰 상세 정보를 조회합니다."
    )
    ApiResponse<GetCouponResponse> getCoupon(Long couponId);

    @Operation(
            summary = "쿠폰 수정",
            description = "쿠폰 정보를 수정합니다."
    )
    ApiResponse<UpdateCouponResponse> updateCoupon(Long couponId, UpdateCouponRequest request);

    @Operation(
            summary = "쿠폰 삭제",
            description = "쿠폰을 삭제합니다."
    )
    ApiResponse<Object> deleteCoupon(Long couponId);

    @Operation(
            summary = "쿠폰 발급 내역 조회",
            description = "특정 쿠폰의 발급 내역을 페이징하여 조회합니다."
    )
    ApiResponse<Page<GetIssuedCouponResponse>> getIssuedCoupons(Long couponId, int page, int size);
}
