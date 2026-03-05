package com.loopers.application.coupon;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CouponService {

    private final CouponRepository couponRepository;

    @Transactional
    public CouponInfo createCoupon(CreateCouponCommand command) {
        Coupon coupon = Coupon.builder()
                .name(command.name())
                .discountType(command.discountType())
                .discountValue(command.discountValue())
                .minOrderAmount(command.minOrderAmount())
                .expiredAt(command.expiredAt())
                .build();

        return CouponInfo.from(couponRepository.save(coupon));
    }

    @Transactional(readOnly = true)
    public CouponInfo getCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

        return CouponInfo.from(coupon);
    }

    @Transactional(readOnly = true)
    public Page<CouponInfo> getCoupons(Pageable pageable) {
        return couponRepository.findAll(pageable)
                .map(CouponInfo::from);
    }

    @Transactional
    public CouponInfo updateCoupon(Long couponId, UpdateCouponCommand command) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

        coupon.modify(
                command.name(),
                command.discountType(),
                command.discountValue(),
                command.minOrderAmount(),
                command.expiredAt()
        );

        return CouponInfo.from(coupon);
    }

    @Transactional(readOnly = true)
    public CouponInfo getIssuableCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));
        coupon.validateIssuable();
        return CouponInfo.from(coupon);
    }

    @Transactional
    public void deleteCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

        coupon.delete();
    }
}
