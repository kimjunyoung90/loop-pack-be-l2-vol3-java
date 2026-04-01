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
    public long increment(Long couponId) {
        return redisTemplate.opsForValue().increment(KEY_PREFIX + couponId);
    }

    @Override
    public void set(Long couponId, int quantity) {
        redisTemplate.opsForValue().set(KEY_PREFIX + couponId, String.valueOf(quantity));
    }
}
