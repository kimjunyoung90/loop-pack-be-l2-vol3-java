package com.loopers.concurrency;

import com.loopers.application.coupon.CouponService;
import com.loopers.application.coupon.command.CouponCreateCommand;
import com.loopers.application.coupon.result.CouponResult;
import com.loopers.domain.coupon.DiscountType;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.testcontainers.RedisTestContainersConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 선착순 쿠폰 발급 동시성 테스트
 *
 * 시나리오: 총 수량 100장인 쿠폰에 200명이 동시에 발급 요청
 * 핵심: Redis SET(SADD → SCARD)의 비원자적 수량 제어가 동시 요청에서 초과 발급을 허용하는지 검증
 * 참고: Lua 스크립트로 원자성을 보장하면 정확히 100명만 성공해야 함
 */
@SpringBootTest(properties = "spring.kafka.bootstrap-servers=localhost:19092")
@Import({MySqlTestContainersConfig.class, RedisTestContainersConfig.class})
class CouponIssueConcurrencyIntegrationTest {

	private static final int TOTAL_QUANTITY = 100;
	private static final int THREAD_COUNT = 200;

	@Autowired
    private CouponService couponService;

	@MockitoBean
	private org.springframework.kafka.core.KafkaTemplate<Object, Object> kafkaTemplate;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@BeforeEach
	void setUp() {
		redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
	}

	@Test
	void 동시에_200명이_선착순_100장_쿠폰을_요청하면_비원자적_수량제어로_초과_발급이_발생할_수_있다() throws InterruptedException {
		// given
		CouponResult coupon = couponService.registerCoupon(
				new CouponCreateCommand("선착순 테스트 쿠폰", DiscountType.FIXED, 1000, null, LocalDate.now().plusDays(7), TOTAL_QUANTITY)
		);

		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
		CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger failCount = new AtomicInteger();

		// when
		for (int i = 0; i < THREAD_COUNT; i++) {
			int finalI = i;
			executorService.submit(() -> {
				long userId = finalI + 1;
				try {
					couponService.requestIssueCoupon(userId, coupon.id());
					successCount.incrementAndGet();
				} catch (Exception e) {
					failCount.incrementAndGet();
				} finally {
					latch.countDown();
				}
			});
		}
		latch.await();
		executorService.shutdown();

		// then — SADD → SCARD 비원자적이므로 초과 발급 발생 가능
		assertThat(successCount.get()).isGreaterThan(TOTAL_QUANTITY);
		System.out.println("성공: " + successCount.get() + ", 실패: " + failCount.get() + " (초과 발급: " + (successCount.get() - TOTAL_QUANTITY) + "건)");
	}
}
