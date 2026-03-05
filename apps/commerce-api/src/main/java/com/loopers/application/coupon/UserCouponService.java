package com.loopers.application.coupon;

import com.loopers.domain.coupon.UserCoupon;
import com.loopers.domain.coupon.UserCouponRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserCouponService {

    private final UserCouponRepository userCouponRepository;

    @Transactional(readOnly = true)
    public boolean existsByCouponId(Long couponId) {
        return userCouponRepository.existsByCouponIdAndDeletedAtIsNull(couponId);
    }

    @Transactional
    public UserCouponInfo createUserCoupon(Long userId, CouponInfo couponInfo) {
        if (userCouponRepository.existsByUserIdAndCouponIdAndDeletedAtIsNull(userId, couponInfo.id())) {
            throw new CoreException(ErrorType.CONFLICT, "이미 발급받은 쿠폰입니다.");
        }

        UserCoupon userCoupon = UserCoupon.builder()
                .userId(userId)
                .couponId(couponInfo.id())
                .couponName(couponInfo.name())
                .discountType(couponInfo.discountType())
                .discountValue(couponInfo.discountValue())
                .minOrderAmount(couponInfo.minOrderAmount())
                .expiredAt(couponInfo.expiredAt())
                .build();

        return UserCouponInfo.from(userCouponRepository.save(userCoupon));
    }

    @Transactional(readOnly = true)
    public List<UserCouponInfo> getUserCoupons(Long userId) {
        return userCouponRepository.findAllByUserIdAndDeletedAtIsNull(userId).stream()
                .map(UserCouponInfo::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<UserCouponInfo> getUserCouponsByCouponId(Long couponId, Pageable pageable) {
        return userCouponRepository.findAllByCouponIdAndDeletedAtIsNull(couponId, pageable)
                .map(UserCouponInfo::from);
    }
}
