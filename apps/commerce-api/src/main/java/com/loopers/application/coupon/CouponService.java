package com.loopers.application.coupon;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponRepository;
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
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;

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

    @Transactional
    public UserCouponInfo issueCoupon(Long userId, Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

        if (userCouponRepository.existsByUserIdAndCouponIdAndDeletedAtIsNull(userId, couponId)) {
            throw new CoreException(ErrorType.CONFLICT, "이미 발급받은 쿠폰입니다.");
        }

        UserCoupon userCoupon = coupon.issue(userId);
        return UserCouponInfo.from(userCouponRepository.save(userCoupon));
    }

    @Transactional
    public int useCoupon(Long userCouponId, Long userId, int totalAmount) {
        UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자 쿠폰을 찾을 수 없습니다."));
        userCoupon.validateUsable(userId, totalAmount);
		userCoupon.use();
        return userCoupon.calculateDiscount(totalAmount);
    }

    @Transactional
    public void restoreCoupon(Long userCouponId) {
        UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자 쿠폰을 찾을 수 없습니다."));
        userCoupon.restore();
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

    @Transactional
    public void deleteCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

        coupon.delete();
    }
}
