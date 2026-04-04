package com.loopers.application.coupon;

import com.loopers.application.coupon.command.CouponCreateCommand;
import com.loopers.application.coupon.command.CouponUpdateCommand;
import com.loopers.application.coupon.result.CouponResult;
import com.loopers.application.coupon.result.UserCouponResult;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponQuantityRepository;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.UserCoupon;
import com.loopers.domain.coupon.UserCouponRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CouponService {

	private static final String COUPON_ISSUE_TOPIC = "coupon-issue.request";

	private final CouponRepository couponRepository;
	private final UserCouponRepository userCouponRepository;
	private final CouponQuantityRepository couponQuantityRepository;
	private final KafkaTemplate<Object, Object> kafkaTemplate;

	@Transactional
	public CouponResult registerCoupon(CouponCreateCommand command) {
		Coupon coupon = Coupon.builder()
				.name(command.name())
				.discountType(command.discountType())
				.discountValue(command.discountValue())
				.minOrderAmount(command.minOrderAmount())
				.expiredAt(command.expiredAt())
				.totalQuantity(command.totalQuantity())
				.build();

		return CouponResult.from(couponRepository.save(coupon));
	}

	@Transactional(readOnly = true)
	public CouponResult getCoupon(Long couponId) {
		Coupon coupon = couponRepository.findByIdAndDeletedAtIsNull(couponId)
				.orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

		return CouponResult.from(coupon);
	}

	@Transactional(readOnly = true)
	public Page<CouponResult> getCoupons(Pageable pageable) {
		return couponRepository.findAllByDeletedAtIsNull(pageable)
				.map(CouponResult::from);
	}

	@Transactional
	public CouponResult modifyCoupon(Long couponId, CouponUpdateCommand command) {
		Coupon coupon = couponRepository.findByIdAndDeletedAtIsNull(couponId)
				.orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

		coupon.changeInfo(
				command.name(),
				command.discountType(),
				command.discountValue(),
				command.minOrderAmount(),
				command.expiredAt(),
				command.totalQuantity()
		);

		return CouponResult.from(coupon);
	}

	@Transactional
	public void deleteCoupon(Long couponId) {
		Coupon coupon = couponRepository.findById(couponId)
				.orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

		coupon.delete();
	}

	public void requestIssueCoupon(Long userId, Long couponId) {
		Coupon coupon = couponRepository.findByIdAndDeletedAtIsNull(couponId)
				.orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

		if (!couponQuantityRepository.addIfAbsent(couponId, userId)) {
			throw new CoreException(ErrorType.CONFLICT, "이미 발급 요청한 쿠폰입니다.");
		}

		if (couponQuantityRepository.count(couponId) > coupon.getTotalQuantity()) {
			couponQuantityRepository.remove(couponId, userId);
			throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 발급 수량이 모두 소진되었습니다.");
		}

		String payload = "{\"userId\":" + userId + ",\"couponId\":" + couponId + "}";
		kafkaTemplate.send(COUPON_ISSUE_TOPIC, String.valueOf(couponId), payload);
	}

	@Transactional
	public void issueCoupon(Long userId, Long couponId) {
		if (userCouponRepository.existsByUserIdAndCouponIdAndDeletedAtIsNull(userId, couponId)) {
			return;
		}

		Coupon coupon = couponRepository.findByIdAndDeletedAtIsNull(couponId)
				.orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

		UserCoupon userCoupon = coupon.issue(userId);
		userCouponRepository.save(userCoupon);
	}

	@Transactional(readOnly = true)
	public List<UserCouponResult> getUserCoupons(Long userId) {
		return userCouponRepository.findAllByUserIdAndDeletedAtIsNull(userId).stream()
				.map(UserCouponResult::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public Page<UserCouponResult> getIssuedCoupons(Long couponId, Pageable pageable) {
		return userCouponRepository.findAllByCouponIdAndDeletedAtIsNull(couponId, pageable)
				.map(UserCouponResult::from);
	}

	@Transactional(readOnly = true)
	public int calculateDiscount(Long userCouponId, Long userId, int totalAmount) {
		UserCoupon userCoupon = userCouponRepository.findByIdAndDeletedAtIsNull(userCouponId)
				.orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자 쿠폰을 찾을 수 없습니다."));
		userCoupon.validateUsable(userId, totalAmount);
		return userCoupon.calculateDiscount(totalAmount);
	}

	@Transactional
	public int useCoupon(Long userCouponId, Long userId, int totalAmount) {
		UserCoupon userCoupon = userCouponRepository.findByIdWithLockAndDeletedAtIsNull(userCouponId)
				.orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자 쿠폰을 찾을 수 없습니다."));
		userCoupon.use(userId, totalAmount);
		return userCoupon.calculateDiscount(totalAmount);
	}

	@Transactional
	public void restoreCoupon(Long userCouponId) {
		UserCoupon userCoupon = userCouponRepository.findByIdAndDeletedAtIsNull(userCouponId)
				.orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자 쿠폰을 찾을 수 없습니다."));
		userCoupon.restore();
	}
}
