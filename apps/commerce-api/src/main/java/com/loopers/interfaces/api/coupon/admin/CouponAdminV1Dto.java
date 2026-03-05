package com.loopers.interfaces.api.coupon.admin;

import com.loopers.application.coupon.CouponInfo;
import com.loopers.domain.coupon.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public class CouponAdminV1Dto {

    public record CreateCouponRequest(
            @NotBlank
            String name,
            @NotNull
            DiscountType discountType,
            @NotNull
            Integer discountValue,
            Integer minOrderAmount,
            @NotNull
            LocalDate expiredAt
    ) {
    }

    public record CreateCouponResponse(
            Long id,
            String name,
            DiscountType discountType,
            int discountValue,
            Integer minOrderAmount,
            LocalDate expiredAt,
            ZonedDateTime createdAt,
            ZonedDateTime updatedAt
    ) {
        public static CreateCouponResponse from(CouponInfo info) {
            return new CreateCouponResponse(
                    info.id(),
                    info.name(),
                    info.discountType(),
                    info.discountValue(),
                    info.minOrderAmount(),
                    info.expiredAt(),
                    info.createdAt(),
                    info.updatedAt()
            );
        }
    }

    public record GetCouponResponse(
            Long id,
            String name,
            DiscountType discountType,
            int discountValue,
            Integer minOrderAmount,
            LocalDate expiredAt,
            ZonedDateTime createdAt,
            ZonedDateTime updatedAt
    ) {
        public static GetCouponResponse from(CouponInfo info) {
            return new GetCouponResponse(
                    info.id(),
                    info.name(),
                    info.discountType(),
                    info.discountValue(),
                    info.minOrderAmount(),
                    info.expiredAt(),
                    info.createdAt(),
                    info.updatedAt()
            );
        }
    }

    public record UpdateCouponRequest(
            @NotBlank
            String name,
            @NotNull
            DiscountType discountType,
            @NotNull
            Integer discountValue,
            Integer minOrderAmount,
            @NotNull
            LocalDate expiredAt
    ) {
    }

    public record UpdateCouponResponse(
            Long id,
            String name,
            DiscountType discountType,
            int discountValue,
            Integer minOrderAmount,
            LocalDate expiredAt,
            ZonedDateTime createdAt,
            ZonedDateTime updatedAt
    ) {
        public static UpdateCouponResponse from(CouponInfo info) {
            return new UpdateCouponResponse(
                    info.id(),
                    info.name(),
                    info.discountType(),
                    info.discountValue(),
                    info.minOrderAmount(),
                    info.expiredAt(),
                    info.createdAt(),
                    info.updatedAt()
            );
        }
    }
}
