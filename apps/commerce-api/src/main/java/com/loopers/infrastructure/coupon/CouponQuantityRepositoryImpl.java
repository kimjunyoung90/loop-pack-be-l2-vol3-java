package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.CouponQuantityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CouponQuantityRepositoryImpl implements CouponQuantityRepository {

    private static final String KEY_PREFIX = "coupon:issued:";

    private final RedisTemplate<String, String> redisTemplate;

	@Override
	public boolean addIfAbsent(Long couponId, Long userId) {
		Long result = redisTemplate.opsForSet().add(KEY_PREFIX + couponId, String.valueOf(userId));
		return result != null && result > 0;
	}

	@Override
	public long count(Long couponId) {
		Long size = redisTemplate.opsForSet().size(KEY_PREFIX + couponId);
		return size != null ? size : 0;
	}

	@Override
	public void remove(Long couponId, Long userId) {
		redisTemplate.opsForSet().remove(KEY_PREFIX + couponId, String.valueOf(userId));
	}
}
